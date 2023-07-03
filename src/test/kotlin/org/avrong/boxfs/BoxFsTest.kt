package org.avrong.boxfs

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
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
        assertEquals(emptyList(), boxFs.list(rootPath))
    }

    @Test
    fun testCreateDirectory() {
        val boxFs = BoxFs.create(tempDir.resolve("create_dir"))

        val rootPath = BoxPath("/")
        val srcPath = BoxPath("/src")
        boxFs.createDirectory(srcPath)

        assertEquals(emptyList(), boxFs.list(srcPath))
        assertEquals(listOf(srcPath), boxFs.list(rootPath))
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

        assertEquals(dirs, boxFs.list(BoxPath("/")))
        assertEquals(emptyList(), boxFs.list(BoxPath("/10")))
    }

    @Test
    fun testCreateDeepDirectories() {
        val boxFs = BoxFs.create(tempDir.resolve("create_deep_dirs"))

        var path = BoxPath("/")
        for (i in 1..10) {
            path = path.with(i.toString())
            boxFs.createDirectory(path)
        }

        assertEquals(listOf(BoxPath("/1")), boxFs.list(BoxPath("/")))
        assertEquals(listOf(BoxPath("/1/2/3/4/5/6/7/8")), boxFs.list(BoxPath("/1/2/3/4/5/6/7")))
        assertEquals(emptyList(), boxFs.list(path))
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