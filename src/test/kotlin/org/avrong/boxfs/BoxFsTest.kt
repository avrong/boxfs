package org.avrong.boxfs

import org.avrong.boxfs.block.Block
import org.avrong.boxfs.block.FirstBlock
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.io.path.fileSize
import kotlin.test.assertEquals

class BoxFsTest {
    @field:TempDir
    lateinit var tempDir: File

    @Test
    fun testInitialization() {
        val file = tempDir.toPath().resolve("boxfs.box")
        BoxFs.initialize(file)
        assertEquals(Block.BLOCK_HEADER_SIZE.toLong() + FirstBlock.BLOCK_DATA_SIZE, file.fileSize())
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