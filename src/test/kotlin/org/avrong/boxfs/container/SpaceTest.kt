package org.avrong.boxfs.container

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class SpaceTest {
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
    }


    @Test
    fun testInt() {
        val value = Int.MAX_VALUE
        space.setIntAt(0, value)

        assertEquals(value, space.getIntAt(0))
    }

    @Test
    fun testLong() {
        val value = Long.MAX_VALUE
        space.setLongAt(0, value)
        assertEquals(value, space.getLongAt(0))
    }

    @Test
    fun testByte() {
        val value = Byte.MAX_VALUE
        space.setByteAt(0, value)
        assertEquals(value, space.getByteAt(0))
    }

    @Test
    fun testString() {
        val str = "Hello World"
        space.setStringAt(0, str)
        assertEquals(str, space.getStringAt(0, str.length))
    }

    @Test
    fun testBytes() {
        val bytes = listOf<Byte>(1, 2, 3, 4, 5, 127).toByteArray()
        space.setBytesAt(0, bytes)
        assertContentEquals(bytes, space.getBytesAt(0, bytes.size))
    }

    @Test
    fun testComplex() {
        val integer = 2
        val integerOffset = 0L

        val str = "something"
        val strOffset = integerOffset + Int.SIZE_BYTES

        val long = 24L
        val longOffset = strOffset + str.length

        space.setIntAt(0, integer)
        space.setStringAt(strOffset, str)
        space.setLongAt(longOffset, long)

        assertEquals(integer, space.getIntAt(0))
        assertEquals(str, space.getStringAt(strOffset, str.length))
        assertEquals(long, space.getLongAt(longOffset))
    }
}