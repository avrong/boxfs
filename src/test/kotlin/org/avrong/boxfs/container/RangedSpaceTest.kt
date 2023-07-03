package org.avrong.boxfs.container

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.test.assertTrue

class RangedSpaceTest {
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

        val rangedSpace: RangedSpace by lazy {
            RangedSpace(space, 10, 10)
        }

        @JvmStatic
        @AfterAll
        fun close() {
            space.close()
        }
    }

    @Test
    fun testWriteOutOfRange() {
        assertThrows<IndexOutOfBoundsException> { rangedSpace.setByte(10, 1) }
        assertThrows<IndexOutOfBoundsException> { rangedSpace.setInt(9, 12) }
        assertThrows<IndexOutOfBoundsException> { rangedSpace.setLong(5, 42) }
        assertThrows<IndexOutOfBoundsException> { rangedSpace.setString(1, "hello world") }
        assertThrows<IndexOutOfBoundsException> { rangedSpace.setBytes(5, ByteArray(6)) }

        assertTrue(rangedSpace.exceedsRange(9, 4))
        assertFalse(rangedSpace.exceedsRange(5, 1))
        assertThrows<IndexOutOfBoundsException> { rangedSpace.exceedsRange(-1, 4) }
    }
}