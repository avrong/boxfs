package org.avrong.boxfs

import org.avrong.boxfs.visitor.AbstractBoxFsVisitor
import org.avrong.boxfs.visitor.BoxFsVisitResult
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readBytes
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

/**
 * Complete functional test
 * - store all project trees
 * - erase 70% of files
 * - compact containers
 * - add all files back into new dirs
 * - close container
 * - reopen
 * - verify all content
 */
class FunctionalTest {
    companion object {
        lateinit var tempDir: Path

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            tempDir = Files.createTempDirectory(null)
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            tempDir.toFile().delete()
        }
    }

    @Test
    fun testWithProjectDir() {
        val boxFs = BoxFs.create(tempDir.resolve("project"))
        val workingDir = Path.of(System.getProperty("user.dir"))

        // Store all project trees
        val rootPath = BoxPath("/")

        // Populate first time. Ignore .gradle because of file lock, and .git because of big number
        // small files
        val ignoredEntries = setOf(".git", ".gradle").map { workingDir.resolve(it) }
        val entriesToPopulate = workingDir.listDirectoryEntries().filterNot { ignoredEntries.contains(it) }
        populateSelectedEntries(boxFs, entriesToPopulate)

        // Remove 70% of files
        val fileCollector = FileCollector()
        boxFs.visitFileTree(rootPath, fileCollector)
        val files = fileCollector.allFiles
        val countFilesToRemove = (files.size * 0.7).toInt()
        files.shuffled().take(countFilesToRemove).forEach { boxFs.delete(it) }

        // Compact
        boxFs.compact()

        // Populate once again
        populateSelectedEntries(boxFs, entriesToPopulate)

        // Verify tree
        val expectedTree = boxFs.getVisualTree(rootPath)
        assertEquals(expectedTree, boxFs.getVisualTree(rootPath))

        // Verify files
        val externalFileVerifier = ExternalFileVerifier(boxFs, rootPath, workingDir, ignoredEntries)
        Files.walkFileTree(workingDir, externalFileVerifier)
    }

    private fun populateSelectedEntries(boxFs: BoxFs, entries: List<Path>) {
        for (entry in entries) {
            val entryPath = "/".toBoxPath().with(entry.name)

            if (Files.isDirectory(entry)) {
                boxFs.createDirectory(entryPath)
            } else if (Files.isRegularFile(entry)) {
                boxFs.createFile(entryPath)

                val content = entry.readBytes()
                boxFs.writeFile(entryPath, content)
            }

            boxFs.populate(entry, entryPath)
        }
    }

    class FileCollector : AbstractBoxFsVisitor() {
        val allFiles = mutableListOf<BoxPath>()
        override fun visitFile(file: BoxPath): BoxFsVisitResult {
            allFiles.add(file)
            return BoxFsVisitResult.CONTINUE
        }
    }

    class ExternalFileVerifier(
        private val boxFs: BoxFs,
        private val internalDirPath: BoxPath,
        private val realDirPath: Path,
        private val ignoreDirs: List<Path>
    ) : SimpleFileVisitor<Path>() {
        override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
            if (ignoreDirs.contains(dir)) return FileVisitResult.SKIP_SUBTREE

            val internalPath = convertPath(dir)
            assertTrue(boxFs.isDirectory(internalPath))

            return super.preVisitDirectory(dir, attrs)
        }

        override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
            val internalPath = convertPath(file)

            assertTrue(boxFs.isFile(internalPath))

            val content = file.readBytes()
            val internalContent = boxFs.readFile(internalPath)
            assertContentEquals(internalContent, content)

            return super.visitFile(file, attrs)
        }

        private fun convertPath(path: Path): BoxPath {
            val withoutPrefix = path.toString().removePrefix(realDirPath.toString()).toBoxPath()
            return internalDirPath.withPath(withoutPrefix)
        }
    }
}