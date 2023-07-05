package org.avrong.boxfs.block

import org.avrong.boxfs.container.Container
import org.avrong.boxfs.container.Space
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class FileBlockTest {
    companion object {
        @field:TempDir
        lateinit var tempDir: File

        private val tempFile: Path by lazy {
            val tempFile = tempDir.toPath().resolve("test")
            tempFile.createFile()
        }

        private val space: Space by lazy {
            Space.fromPath(tempFile)
        }

        private val container: Container by lazy {
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
        val blockOffset = container.createFileBlock(blockDataSize).offset
        val fileBlock = container.getFileBlock(blockOffset)

        assertEquals(blockDataSize, fileBlock.dataSize)
    }

    @Test
    fun testSetContent() {
        val fileBlock = container.createFileBlock(32)
        val content = listOf<Byte>(1, 2, 3, 4).toByteArray()
        fileBlock.content = content

        assertContentEquals(content, fileBlock.content)
        assertEquals(content.size, fileBlock.contentSize)
    }

    @Test
    fun testAppendContent() {
        val content = listOf<Byte>(1, 2, 3, 4).toByteArray()
        val fileBlock = container.createFileBlock(FileBlock.getInitialBlockDataSize(content))
        fileBlock.appendContent(content.sliceArray(0..1))
        fileBlock.appendContent(content.sliceArray(2..3))

        assertContentEquals(content, fileBlock.content)
        assertEquals(content.size, fileBlock.contentSize)
    }

    @Test
    fun testInitialMinimalBlockDataSize() {
        val initialMinDataSize = FileBlock.getInitialBlockDataSize(ByteArray(0))
        assertEquals(
            FileBlock.NEXT_BLOCK_SIZE + FileBlock.CONTENT_SIZE_SIZE + FileBlock.MIN_INITIAL_BLOCK_CONTENT_SIZE,
            initialMinDataSize
        )
    }

    @Test
    fun testInitialContentBlockDataSize() {
        val content = ByteArray(100)
        val initialContentDataSize = FileBlock.getInitialBlockDataSize(content)
        assertEquals(
            FileBlock.NEXT_BLOCK_SIZE + FileBlock.CONTENT_SIZE_SIZE +
                    content.size,
            initialContentDataSize
        )
    }

    @Test
    fun testAdditionalBlockDataSize() {
        val content = ByteArray(100)
        val previousBlockInfoSize = FileBlock.NEXT_BLOCK_SIZE + FileBlock.CONTENT_SIZE_SIZE
        val previousBlockContentSize = 64
        val additionalContentDataSize = FileBlock.getAdditionalBlockDataSize(content.size, previousBlockInfoSize + previousBlockContentSize)

        assertEquals(
            FileBlock.NEXT_BLOCK_SIZE + FileBlock.CONTENT_SIZE_SIZE + content.size + (previousBlockContentSize * 1.5).toInt(),
            additionalContentDataSize
        )
    }
}