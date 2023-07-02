package org.avrong.boxfs.block

import org.avrong.boxfs.container.Container
import org.avrong.boxfs.container.Space
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.test.assertEquals

class DirectoryBlockTest {
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
    fun testCreateAndGet() {
        val blockDataSize = 42
        val blockOffset = container.createDirectoryBlock(blockDataSize).offset
        val directoryBlock = container.getDirectoryBlock(blockOffset)

        assertEquals(blockDataSize, directoryBlock.dataSize)
    }

    @Test
    fun testSetEntries() {
        val entryList = listOf(
            DirectoryBlock.DirectoryBlockEntry(1, 2),
            DirectoryBlock.DirectoryBlockEntry(3, 4)
        )
        val directoryBlock = container.createDirectoryBlock(DirectoryBlock.getInitialBlockDataSize(entryList))
        directoryBlock.entries = entryList

        assertEquals(entryList, directoryBlock.entries)
        assertEquals(entryList.size, directoryBlock.entryCount)
    }

    @Test
    fun testAppendEntries() {
        val entryList = listOf(
            DirectoryBlock.DirectoryBlockEntry(1, 2),
            DirectoryBlock.DirectoryBlockEntry(3, 4)
        )
        val directoryBlock = container.createDirectoryBlock(DirectoryBlock.getInitialBlockDataSize(listOf()))
        directoryBlock.appendEntries(listOf(entryList[0]))
        directoryBlock.appendEntries(listOf(entryList[1]))

        assertEquals(entryList, directoryBlock.entries)
        assertEquals(entryList.size, directoryBlock.entryCount)
    }

    @Test
    fun testInitialMinimalBlockDataSize() {
        val initialMinDataSize = DirectoryBlock.getInitialBlockDataSize(listOf())
        assertEquals(
            DirectoryBlock.NEXT_BLOCK_SIZE + DirectoryBlock.ENTRY_COUNT_SIZE +
                    DirectoryBlock.MIN_INITIAL_ENTRIES_COUNT * DirectoryBlock.SINGLE_ENTRY_SIZE,
            initialMinDataSize
        )

    }

    @Test
    fun testInitialContentBlockDataSize() {
        val entriesCount = 10
        val initialContentDataSize = DirectoryBlock.getInitialBlockDataSize(List(entriesCount) {
            DirectoryBlock.DirectoryBlockEntry(42, 37)
        })
        assertEquals(
            DirectoryBlock.NEXT_BLOCK_SIZE + DirectoryBlock.ENTRY_COUNT_SIZE +
                    entriesCount * DirectoryBlock.SINGLE_ENTRY_SIZE,
            initialContentDataSize
        )
    }

    @Test
    fun testAdditionalBlockDataSize() {
        val previousBlockInfoSize = DirectoryBlock.NEXT_BLOCK_SIZE + DirectoryBlock.ENTRY_COUNT_SIZE
        val previousBlockEntriesSize = 10
        val additionalContentDataSize = DirectoryBlock.getAdditionalBlockDataSize(previousBlockInfoSize + previousBlockEntriesSize)

        assertEquals(
            DirectoryBlock.NEXT_BLOCK_SIZE + DirectoryBlock.ENTRY_COUNT_SIZE + previousBlockEntriesSize * 2,
            additionalContentDataSize
        )
    }
}