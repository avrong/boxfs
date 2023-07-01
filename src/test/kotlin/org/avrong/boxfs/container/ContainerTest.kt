package org.avrong.boxfs.container

import org.avrong.boxfs.block.SymbolBlock
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.fileSize
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
    }

    @Test
    fun testFirstBlock() {
        container.firstBlock.rootDirectoryOffset = 42
        assertEquals(42, container.firstBlock.rootDirectoryOffset)
        val fileSize = tempFile.fileSize()

        // Reset block
        container.initFirstBlock()
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
    }
}