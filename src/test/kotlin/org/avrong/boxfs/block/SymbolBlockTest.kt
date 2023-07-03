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

class SymbolBlockTest {
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
        val blockOffset = container.createSymbolBlock(blockDataSize).offset
        val symbolBlock = container.getSymbolBlock(blockOffset)

        assertEquals(blockDataSize, symbolBlock.dataSize)
    }

    @Test
    fun testSetString() {
        val symbolBlock = container.createSymbolBlock(32)
        val str = "hello"
        symbolBlock.string = str

        assertEquals(str, symbolBlock.string)
        assertEquals(str.length, symbolBlock.stringBytesSize)
    }

    @Test
    fun testDataSizeCalculation() {
        val data = "something"
        val dataSize = SymbolBlock.getBlockDataSize(data)

        assertEquals(SymbolBlock.OCCUPIED_SIZE + data.toByteArray().size, dataSize)
    }
}