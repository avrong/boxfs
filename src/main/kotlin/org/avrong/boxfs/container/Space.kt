package org.avrong.boxfs.container

import java.io.RandomAccessFile
import java.nio.file.Path
import kotlin.io.path.exists

class Space private constructor (private val randomAccessFile: RandomAccessFile) : AutoCloseable {
    private var position: Long = 0
    private var length: Long = randomAccessFile.length()

    val isEmpty: Boolean = randomAccessFile.length() == 0L

    init {
        position = randomAccessFile.filePointer
    }

    fun rangedSpace(globalOffset: Long, size: Int): RangedSpace = RangedSpace(this, globalOffset, size)
    fun rangedSpaceFromEnd(size: Int): RangedSpace {
        length += size
        randomAccessFile.setLength(length)
        return RangedSpace(this, length, size)
    }

    fun getByteAt(offset: Long): Byte = withPositionChange(offset, Byte.SIZE_BYTES) {
        randomAccessFile.readByte()
    }

    fun setByteAt(offset: Long, byte: Byte) = withPositionChange(offset, Byte.SIZE_BYTES) {
        randomAccessFile.writeByte(byte.toInt())
    }

    fun getIntAt(offset: Long): Int = withPositionChange(offset, Byte.SIZE_BYTES) {
        randomAccessFile.readInt()
    }

    fun setIntAt(offset: Long, value: Int) = withPositionChange(offset, Int.SIZE_BYTES) {
        randomAccessFile.writeInt(value)
    }

    fun getLongAt(offset: Long): Long = withPositionChange(offset, Long.SIZE_BYTES) {
        randomAccessFile.readLong()
    }

    fun setLongAt(offset: Long, value: Long) = withPositionChange(offset, Long.SIZE_BYTES) {
        randomAccessFile.writeLong(value)
    }

    fun getStringAt(offset: Long, size: Int): String = withPositionChange(offset, size * Char.SIZE_BYTES) {
        val byteArray = ByteArray(size)
        randomAccessFile.readFully(byteArray)

        String(byteArray)
    }

    fun setStringAt(offset: Long, value: String) = withPositionChange(offset, value.length * Char.SIZE_BYTES) {
        randomAccessFile.write(value.toByteArray())
    }

    override fun close() {
        randomAccessFile.close()
    }

    private fun <T> withPositionChange(offset: Long, change: Int, action: () -> T): T {
        if (position != offset) {
            randomAccessFile.seek(offset)
            position = offset
        }

        val result = action()
        position += change
        if (position > length) {
            length = position
        }

        return result
    }

    companion object {
        fun fromPath(path: Path): Space {
            if (!path.exists()) throw RuntimeException("File does not exist")
            val randomAccessFile = RandomAccessFile(path.toFile(), "rws")
            randomAccessFile.seek(0)
            return Space(randomAccessFile)
        }
    }
}