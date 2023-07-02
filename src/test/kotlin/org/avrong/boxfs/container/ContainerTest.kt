package org.avrong.boxfs.container

import org.avrong.boxfs.block.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.fileSize
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ContainerTest {
    companion object {
        @field:TempDir
        lateinit var tempDir: File

        val tempFile: Path by lazy {
            val tempFile = tempDir.toPath().resolve("test")
            tempFile.createFile()
        }

        val space: Space by lazy {
            Space.fromPath(tempFile)
        }

        val container: Container by lazy {
            Container.fromSpace(space)
        }

        @JvmStatic
        @AfterAll
        fun close() {
            container.close()
        }
    }


    @Test
    fun testFirstBlock() {
        container.firstBlock.rootDirectoryOffset = 42
        assertEquals(42, container.firstBlock.rootDirectoryOffset)
        val fileSize = tempFile.fileSize()

        // Reset block
        container.initFirstBlock()
        assertEquals(BlockType.FIRST, container.getBlockType(container.firstBlock.offset))
        assertEquals(FirstBlock.BLOCK_DATA_SIZE, container.getBlockSize(container.firstBlock.offset))
        assertEquals(0, container.firstBlock.rootDirectoryOffset)
        assertEquals(fileSize, tempFile.fileSize())
    }

    @Test
    fun testSymbolBlock() {
        val symbolName = "hello"

        val blockDataSize = SymbolBlock.getBlockDataSize(symbolName)
        val symbolBlock = container.createSymbolBlock(blockDataSize)
        symbolBlock.string = symbolName

        assertEquals(blockDataSize, symbolBlock.dataSize)
        assertEquals(symbolName.length, symbolBlock.length)

        assertTrue(symbolBlock.checkStringFits("abc"))
        assertFalse(symbolBlock.checkStringFits("hello1"))

        assertEquals(symbolName, symbolBlock.string)
    }

    @Test
    fun testDirectoryBlock() {
        val entryList = listOf(DirectoryBlock.DirectoryBlockEntry(12, 24))
        val blockDataSize = DirectoryBlock.getInitialBlockDataSize(entryList)
        val directoryBlock = container.createDirectoryBlock(blockDataSize)
        directoryBlock.appendEntries(entryList)

        assertEquals(blockDataSize, directoryBlock.dataSize)
        assertEquals(entryList.size, directoryBlock.entryCount)
        assertEquals(entryList, directoryBlock.entries)
    }

    @Test
    fun testFileBlock() {
        val content = "hello".toByteArray()
        val blockDataSize = FileBlock.getInitialBlockDataSize(content)
        val fileBlock = container.createFileBlock(blockDataSize)
        fileBlock.appendContent(content)

        assertEquals(blockDataSize, fileBlock.dataSize)
        assertEquals(content.size, fileBlock.contentSize)
        assertContentEquals(content, fileBlock.content)
    }

    @Test
    fun getBlockWithWrongType() {
        val fileBlock = container.createFileBlock(32)
        assertThrows<IllegalArgumentException> { container.getDirectoryBlock(fileBlock.offset) }
    }
}