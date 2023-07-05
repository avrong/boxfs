package org.avrong.boxfs

import org.avrong.boxfs.visitor.BoxFsVisitResult
import org.avrong.boxfs.visitor.BoxFsVisitor
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
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

        // TODO: Currently there are a lot of small git files, maybe ignore .git completely?
        // Store all project trees
        val rootPath = BoxPath("/")
        boxFs.populate(workingDir, rootPath)

        val fileCollector = FileCollector()
        boxFs.visitFileTree(rootPath, fileCollector)
        val files = fileCollector.allFiles
        val countFilesToRemove = (files.size * 0.7).toInt()
        files.shuffled().take(countFilesToRemove).forEach { boxFs.delete(it) }

        boxFs.populate(workingDir, rootPath)
        val expectedTree = boxFs.getVisualTree(rootPath)

        // Compact
        boxFs.compact()

        assertEquals(expectedTree, boxFs.getVisualTree(rootPath))
    }

    class FileCollector : BoxFsVisitor {
        val allFiles = mutableListOf<BoxPath>()

        override fun preVisitDirectory(dir: BoxPath): BoxFsVisitResult {
            return BoxFsVisitResult.CONTINUE
        }

        override fun visitFile(file: BoxPath): BoxFsVisitResult {
            allFiles.add(file)
            return BoxFsVisitResult.CONTINUE
        }

        override fun postVisitDirectory(dir: BoxPath): BoxFsVisitResult {
            return BoxFsVisitResult.CONTINUE
        }
    }

}