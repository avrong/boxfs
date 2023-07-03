package org.avrong.boxfs.container

import java.io.RandomAccessFile
import java.nio.file.Path
import kotlin.io.path.exists

class Space private constructor (private val randomAccessFile: RandomAccessFile) : AutoCloseable {
    var position: Long = 0
        private set

    var length: Long = randomAccessFile.length()
        private set

    val isEmpty: Boolean
        get() = randomAccessFile.length() == 0L

    init {
        position = randomAccessFile.filePointer
    }

    fun rangedSpace(globalOffset: Long, size: Int): RangedSpace = RangedSpace(this, globalOffset, size)
    fun rangedSpaceFromEnd(size: Int): RangedSpace {
        val rangedSpace = RangedSpace(this, length, size)
        length += size
        randomAccessFile.setLength(length)
        return rangedSpace
    }

    fun getByteAt(offset: Long): Byte = withPositionChange(offset, Byte.SIZE_BYTES) {
        randomAccessFile.readByte()
    }

    fun setByteAt(offset: Long, byte: Byte) = withPositionChange(offset, Byte.SIZE_BYTES) {
        randomAccessFile.writeByte(byte.toInt())
    }

    fun getIntAt(offset: Long): Int = withPositionChange(offset, Int.SIZE_BYTES) {
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

    fun getStringAt(offset: Long, byteSize: Int): String = withPositionChange(offset, byteSize) {
        val byteArray = ByteArray(byteSize)
        randomAccessFile.readFully(byteArray)

        String(byteArray)
    }

    fun setStringAt(offset: Long, value: String) = withPositionChange(offset, value.toByteArray().size) {
        randomAccessFile.write(value.toByteArray())
    }

    fun getBytesAt(offset: Long, size: Int): ByteArray = withPositionChange(offset, size) {
        val byteArray = ByteArray(size)
        randomAccessFile.read(byteArray)
        byteArray
    }

    fun setBytesAt(offset: Long, value: ByteArray) = withPositionChange(offset, value.size) {
        randomAccessFile.write(value)
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