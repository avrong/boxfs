package org.avrong.boxfs.block

import org.avrong.boxfs.container.Container
import org.avrong.boxfs.container.Space
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.test.assertEquals

class FirstBlockTest {
    @field:TempDir
    lateinit var tempDir: File

    val tempFile: Path by lazy {
        val tempFile = tempDir.toPath().resolve("test")
        tempFile.createFile()
    }

    val container: Container by lazy {
        val space = Space.fromPath(tempFile)
        Container.fromSpace(space)
    }

    @Test
    fun testBlockCreation() {
        val firstBlock = container.firstBlock

        assertEquals(BlockType.FIRST, container.getBlockType(0))

        assertEquals(Block.BLOCK_HEADER_SIZE + FirstBlock.BLOCK_DATA_SIZE, firstBlock.spaceSize)
        assertEquals(0, firstBlock.rootDirectoryOffset)
    }
}