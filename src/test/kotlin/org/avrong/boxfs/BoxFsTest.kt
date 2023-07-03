package org.avrong.boxfs

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class BoxFsTest {

    companion object {
        @field:TempDir
        lateinit var _tempDir: File
        val tempDir: Path
            get() = _tempDir.toPath()
    }

    @Test
    fun testInitialization() {
        val boxFs = BoxFs.create(tempDir.resolve("init"))
        val rootPath = BoxPath("/")
        assertEquals(emptyList(), boxFs.listDirectory(rootPath))
    }

    @Test
    fun testCreateDirectory() {
        val boxFs = BoxFs.create(tempDir.resolve("create_dir"))

        val rootPath = BoxPath("/")
        val srcPath = BoxPath("/src")
        val createDirResult = boxFs.createDirectory(srcPath)

        assertTrue(createDirResult)
        assertEquals(emptyList(), boxFs.listDirectory(srcPath))
        assertEquals(listOf(srcPath), boxFs.listDirectory(rootPath))
    }

    @Test
    fun testCreateManyDirectories() {
        val boxFs = BoxFs.create(tempDir.resolve("create_many_dirs"))

        val dirs = mutableListOf<BoxPath>()
        for (i in 1..10) {
            val path = BoxPath("/$i")
            boxFs.createDirectory(path)
            dirs.add(path)
        }

        assertEquals(dirs, boxFs.listDirectory(BoxPath("/")))
        assertEquals(emptyList(), boxFs.listDirectory(BoxPath("/10")))
    }

    @Test
    fun testCreateDeepDirectories() {
        val boxFs = BoxFs.create(tempDir.resolve("create_deep_dirs"))

        var path = BoxPath("/")
        for (i in 1..10) {
            path = path.with(i.toString())
            boxFs.createDirectory(path)
        }

        assertEquals(listOf(BoxPath("/1")), boxFs.listDirectory(BoxPath("/")))
        assertEquals(listOf(BoxPath("/1/2/3/4/5/6/7/8")), boxFs.listDirectory(BoxPath("/1/2/3/4/5/6/7")))
        assertEquals(emptyList(), boxFs.listDirectory(path))
    }

    @Test
    fun testCreateDirectoriesSameNames() {
        val boxFs = BoxFs.create(tempDir.resolve("create_dirs_same_names"))

        val dirName = BoxPath("/hello")
        val childDirName = dirName.with("world")
        val firstResult = boxFs.createDirectory(dirName)
        boxFs.createDirectory(childDirName)
        val secondResult = boxFs.createDirectory(dirName)

        assertTrue(firstResult)
        assertFalse(secondResult) // check second time dir was not created
        assertEquals(listOf(childDirName), boxFs.listDirectory(dirName))
    }

    @Test
    fun testCreateFile() {
        val boxFs = BoxFs.create(tempDir.resolve("create_file"))

        val filePath = BoxPath("/.gitignore")
        val createFileResult = boxFs.createFile(filePath)

        assertTrue(createFileResult)
        assertEquals(0, boxFs.getFileSize(filePath))
        assertContentEquals(ByteArray(0), boxFs.readFile(filePath))
    }

    @Test
    fun testWriteReadSmallFile() {
        val boxFs = BoxFs.create(tempDir.resolve("read_small_file"))

        val filePath = BoxPath("/README.md")
        boxFs.createFile(filePath)

        val content = "Hello World!".toByteArray()
        boxFs.writeFile(filePath, content)

        assertEquals(content.size, boxFs.getFileSize(filePath))
        assertContentEquals(content, boxFs.readFile(filePath))
    }

    @Test
    fun testWriteReadLargeFile() {
        val boxFs = BoxFs.create(tempDir.resolve("read_large_file"))

        val filePath = BoxPath("/README.md")
        boxFs.createFile(filePath)

        val content = "quick brown fox jumps over the lazy dog, and then ".repeat(5).trim().toByteArray()
        boxFs.writeFile(filePath, content)

        assertEquals(content.size, boxFs.getFileSize(filePath))
        assertContentEquals(content, boxFs.readFile(filePath))
    }

    /*
    TODO: Complete functional test
     - store all project trees
     - erase 70% of files
     - compact containers
     - add all files back into new dirs
     - close container
     - reopen
     - verify all content
    */
}