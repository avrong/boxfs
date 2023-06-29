package org.avrong.boxfs

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.ByteBuffer
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class BoxFsTest {
    @TempDir
    @JvmField
    var tempDir: File? = null

    @Test
    fun testInitialization() {
        val file = tempDir!!.toPath().resolve("boxfs.box")
        val boxFs = BoxFs.initialize(file, 10)
        assertEquals(emptyList(), boxFs.fileTable.files)
    }

    @Test
    fun fileEntryTest() {
        val fileEntry = FileEntry("/hello/world", 10, 20)
        val bytes = fileEntry.toBytes()
        val byteBuffer = ByteBuffer.wrap(bytes)

        assertEquals(fileEntry, FileEntry.fromByteBuffer(byteBuffer))
    }

    @Test
    fun fileTableTest() {
        val fileTable = FileTable(mutableListOf(
            FileEntry("/hello", 0, 1),
            FileEntry("/world", 2, 3)
        ))

        val bytes = fileTable.toBytes()
        val byteBuffer = ByteBuffer.wrap(bytes)
        val parsedFileTable = FileTable.fromByteBuffer(byteBuffer)

        assertEquals(fileTable, parsedFileTable)
    }

    @Test
    fun writeAndOpenFileTest() {
        val file = tempDir!!.toPath().resolve("boxfs.box")
        val boxFs = BoxFs.initialize(file, 4)
        val byteArray = byteArrayOf(1, 2, 3, 4)
        boxFs.write("hello.txt", byteArray)
        val openedFile = boxFs.open("hello.txt")!!

        assertEquals("hello.txt", openedFile.path)
        assertEquals(false, openedFile.isDirectory)
        assertContentEquals(byteArray, openedFile.getContent())
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