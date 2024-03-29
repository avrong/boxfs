package org.avrong.boxfs

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.readBytes
import kotlin.io.path.readText
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class BoxFsTest {
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
    fun testInitialization() {
        val boxFs = BoxFs.create(tempDir.resolve("init"))
        val rootPath = BoxPath("/")
        assertEquals(emptyList(), boxFs.listDirectory(rootPath))
    }

    @Test
    fun testInitOpen() {
        val containerPath = tempDir.resolve("init_open_test")
        val boxFs = BoxFs.create(containerPath)
        val rootPath = BoxPath("/")
        boxFs.close()

        val openedBoxFs = BoxFs.open(containerPath)
        assertTrue(openedBoxFs.exists(rootPath))
        assertEquals(emptyList(), openedBoxFs.listDirectory(rootPath))
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
    fun testCreateDirectories() {
        val boxFs = BoxFs.create(tempDir.resolve("create_dirs"))

        boxFs.createDirectory(BoxPath("/1"))
        boxFs.createDirectory(BoxPath("/1/2"))

        val dirPath = BoxPath("/1/2/3/4/5/6/7/8/9/10")
        boxFs.createDirectories(dirPath)

        assertTrue(boxFs.exists(dirPath))
        assertEquals(listOf(dirPath), boxFs.listDirectory(dirPath.withoutLast()))
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

    @Test
    fun testRewriteFile() {
        val boxFs = BoxFs.create(tempDir.resolve("rewrite_file"))

        val filePath = BoxPath("/README.md")
        boxFs.createFile(filePath)

        val content = "Shall I compare thee to the summer's day? Thou art art more lovely and more temperate."
            .toByteArray()
        boxFs.writeFile(filePath, content)
        assertEquals(content.size, boxFs.getFileSize(filePath))
        assertContentEquals(content, boxFs.readFile(filePath))

        val newContent = "Rough winds do shake the darling buds of May"
            .toByteArray()
        boxFs.writeFile(filePath, newContent)

        assertEquals(newContent.size, boxFs.getFileSize(filePath))
        assertContentEquals(newContent, boxFs.readFile(filePath))
    }

    @Test
    fun testAppendFile() {
        val boxFs = BoxFs.create(tempDir.resolve("append_file"))

        val content = listOf("Let me not to the marriage of true minds. ", "Admit impediments. ", "Love is not love")

        val filePath = BoxPath("/README.md")
        boxFs.createFile(filePath)
        boxFs.appendFile(filePath, content[0].toByteArray())
        boxFs.appendFile(filePath, content[1].toByteArray())
        assertContentEquals((content[0] + content[1]).toByteArray(), boxFs.readFile(filePath))

        // Empty file
        boxFs.writeFile(filePath, ByteArray(0))

        boxFs.appendFile(filePath, content[2].toByteArray())
        assertContentEquals(content[2].toByteArray(), boxFs.readFile(filePath))
    }

    @Test
    fun testExists() {
        val boxFs = BoxFs.create(tempDir.resolve("exists"))

        boxFs.createDirectory(BoxPath("/1"))
        boxFs.createDirectory(BoxPath("/1/2"))
        boxFs.createDirectory(BoxPath("/1/2/3"))

        boxFs.createFile(BoxPath("/1/5"))

        assertTrue(boxFs.exists(BoxPath("/1/2/3")))
        assertTrue(boxFs.exists(BoxPath("/1/5")))
        assertFalse(boxFs.exists(BoxPath("/1/3")))
    }

    @Test
    fun testDelete() {
        val boxFs = BoxFs.create(tempDir.resolve("delete"))

        val dirs = listOf("/1", "/2", "/2/3", "/2/4", "/5", "/5/6", "/5/6/7").map { BoxPath(it) }
        val files = listOf("/1/a", "/2/b", "/c", "/5/6/d").map { BoxPath(it) }

        dirs.forEach { boxFs.createDirectory(it) }
        files.forEach { boxFs.createFile(it) }

        assertContentEquals(setOf("/2/3", "/2/4", "/2/b").map { BoxPath(it)}, boxFs.listDirectory(BoxPath("/2")))
        assertTrue(boxFs.delete(BoxPath("/2/4")))
        assertContentEquals(setOf("/2/3", "/2/b").map { BoxPath(it)}, boxFs.listDirectory(BoxPath("/2")))
        assertTrue(boxFs.delete(BoxPath("/2/b")))
        assertContentEquals(setOf("/2/3").map { BoxPath(it)}, boxFs.listDirectory(BoxPath("/2")))
    }

    @Test
    fun testRename() {
        val boxFs = BoxFs.create(tempDir.resolve("rename"))

        val dir = BoxPath("/hello/java")
        val newDir = BoxPath("/hello/kotlin")

        boxFs.createDirectory(BoxPath("/hello"))
        boxFs.createDirectory(dir)

        // Check old dir exists
        assertTrue(boxFs.exists(dir))

        // Rename
        assertTrue(boxFs.rename(dir, newDir))

        // Check new dir
        assertFalse(boxFs.exists(dir))
        assertTrue(boxFs.exists(newDir))

        assertEquals(listOf(newDir), boxFs.listDirectory(BoxPath("/hello")))
    }

    @Test
    fun testRenameNotSameDir() {
        val boxFs = BoxFs.create(tempDir.resolve("rename_not_same_dir"))

        val dir = BoxPath("/hello/java")
        val newDir = BoxPath("/another/kotlin")

        boxFs.createDirectory(BoxPath("/hello"))
        boxFs.createDirectory(dir)

        // Check that rename did not happen
        assertFalse(boxFs.rename(dir, newDir))
        assertEquals(listOf(dir), boxFs.listDirectory(BoxPath("/hello")))
        assertEquals(listOf(BoxPath("/hello")), boxFs.listDirectory(BoxPath("/")))
    }

    @Test
    fun testMove() {
        val boxFs = BoxFs.create(tempDir.resolve("move"))

        val dirs = listOf("/1", "/2", "/2/3", "/2/4", "/5", "/5/6", "/5/6/7").map { BoxPath(it) }
        val files = listOf("/1/a", "/2/b", "/c", "/5/6/d").map { BoxPath(it) }

        dirs.forEach { boxFs.createDirectory(it) }
        files.forEach { boxFs.createFile(it) }

        assertTrue(boxFs.exists(BoxPath("/2")))

        // Move
        boxFs.move(BoxPath("/2"), BoxPath("/5/2"))

        // Check moved dir
        assertFalse(boxFs.exists(BoxPath("/2")))
        assertTrue(boxFs.exists(BoxPath("/5/2")))
        assertTrue(boxFs.exists(BoxPath("/5/2/3")))
    }

    @Test
    fun testCopy() {
        val boxFs = BoxFs.create(tempDir.resolve("copy"))

        val dirs = listOf("/1", "/2", "/2/3", "/2/4", "/5", "/5/6", "/5/6/7").map { BoxPath(it) }
        val files = listOf("/1/a", "/2/b", "/c", "/5/6/d").map { BoxPath(it) }

        boxFs.writeFile(BoxPath("/5/6/d"), "hello".toByteArray())

        dirs.forEach { boxFs.createDirectory(it) }
        files.forEach { boxFs.createFile(it) }

        // Copy dir
        boxFs.copy(BoxPath("/2"), BoxPath("/5/42"))

        // Check copied dir
        assertTrue(boxFs.exists(BoxPath("/2")))
        assertTrue(boxFs.isDirectory(BoxPath("/5/42")))
        assertTrue(boxFs.exists(BoxPath("/5/42")))
        assertTrue(boxFs.exists(BoxPath("/5/42/3")))
        assertTrue(boxFs.exists(BoxPath("/5/42/4")))
        assertTrue(boxFs.exists(BoxPath("/5/42/b")))

        // Copy file
        val copyFromPath = BoxPath("/5/6/d")
        val copyToPath = BoxPath("/5/6/7/k")
        boxFs.copy(copyFromPath, copyToPath)

        // Check copied file
        assertTrue(boxFs.exists(copyFromPath))
        assertTrue(boxFs.exists(copyToPath))
        assertTrue(boxFs.isFile(copyToPath))
        assertContentEquals(boxFs.readFile(copyFromPath), boxFs.readFile(copyToPath))
    }

    @Test
    fun testPopulation() {
        val boxFs = BoxFs.create(tempDir.resolve("population"))
        val localPath = getResource("/test/bands")
        boxFs.populate(localPath, BoxPath("/"))
        assertTrue(boxFs.exists("/rock".toBoxPath()))
        assertTrue(boxFs.exists("/rock/punk/paramore.txt".toBoxPath()))
        assertEquals(
            "It's something unpredictable, but in the end it's right. I hope you had the time of your life",
            String(boxFs.readFile("/rock/punk/greenday.txt".toBoxPath())!!)
        )
    }

    @Test
    fun testPopulationAndTreeVisitor() {
        val boxFs = BoxFs.create(tempDir.resolve("population_tree"))
        val testResourcesPath = getResource("/test")
        val localPath = testResourcesPath.resolve("bands/")
        val expectedTree = testResourcesPath.resolve("bands-tree.txt").readText()

        val rootPath = BoxPath("/")
        boxFs.populate(localPath, rootPath)

        val visualTree = boxFs.getVisualTree(rootPath)

        // Make set out of lines so that directory entries order doesn't affect assertion
        assertEquals(expectedTree.lines().toSet(), visualTree.lines().toSet())
    }

    @Test
    fun testMaterialize() {
        val boxFs = BoxFs.create(tempDir.resolve("populate_and_materialize"))
        val outputPath = tempDir.resolve("materialize/")
        outputPath.createDirectory()

        // Populate into container
        val testBandsPath = getResource("/test/bands")
        boxFs.populate(testBandsPath, "/".toBoxPath())

        // Materialize it back
        boxFs.materialize("/".toBoxPath(), outputPath)

        assertTrue(Files.exists(outputPath.resolve("rock/punk/paramore.txt")))
        assertTrue(Files.exists(outputPath.resolve("folk/simon and garfunkel.txt")))

        val bonIverPath = "folk/BonIver.txt"
        assertContentEquals(boxFs.readFile(bonIverPath.toBoxPath()), outputPath.resolve(bonIverPath).readBytes())
    }

    @Test
    fun testCompaction() {
        val boxFs = BoxFs.create(tempDir.resolve("compaction"))
        val localPath = getResource("/test/bands")

        val rootPath = BoxPath("/")
        boxFs.populate(localPath, rootPath)
        val expectedTree = boxFs.getVisualTree(rootPath)

        // Compact
        boxFs.compact()

        assertTrue(boxFs.exists("/rock".toBoxPath()))
        assertTrue(boxFs.exists("/rock/punk/paramore.txt".toBoxPath()))
        assertEquals(
            "It's something unpredictable, but in the end it's right. I hope you had the time of your life",
            String(boxFs.readFile("/rock/punk/greenday.txt".toBoxPath())!!)
        )

        assertEquals(expectedTree, boxFs.getVisualTree(rootPath))
    }

    /**
     * Get resource (with Windows path workaround)
     */
    private fun getResource(path: String): Path = File(BoxFsTest::class.java.getResource(path)!!.file).toPath()
}