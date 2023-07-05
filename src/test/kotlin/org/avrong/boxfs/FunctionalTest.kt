package org.avrong.boxfs

import org.avrong.boxfs.visitor.AbstractBoxFsVisitor
import org.avrong.boxfs.visitor.BoxFsVisitResult
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
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
        val ignoredDirs = setOf(".git", ".gradle").map { workingDir.resolve(it) }
        val dirsToPopulate = workingDir.listDirectoryEntries().filterNot { ignoredDirs.contains(it) }
        populateSelectedDirs(boxFs, dirsToPopulate)

        // Remove 70% of files
        val fileCollector = FileCollector()
        boxFs.visitFileTree(rootPath, fileCollector)
        val files = fileCollector.allFiles
        val countFilesToRemove = (files.size * 0.7).toInt()
        files.shuffled().take(countFilesToRemove).forEach { boxFs.delete(it) }

        // Populate once again
        populateSelectedDirs(boxFs, dirsToPopulate)

        val expectedTree = boxFs.getVisualTree(rootPath)

        // Compact
        boxFs.compact()

        assertEquals(expectedTree, boxFs.getVisualTree(rootPath))
    }

    fun populateSelectedDirs(boxFs: BoxFs, dirs: List<Path>) {
        for (localDir in dirs) {
            val dirName = "/".toBoxPath().with(localDir.name)
            boxFs.createDirectory(dirName)
            boxFs.populate(localDir, dirName)
        }
    }

    class FileCollector : AbstractBoxFsVisitor() {
        val allFiles = mutableListOf<BoxPath>()
        override fun visitFile(file: BoxPath): BoxFsVisitResult {
            allFiles.add(file)
            return BoxFsVisitResult.CONTINUE
        }
    }

}