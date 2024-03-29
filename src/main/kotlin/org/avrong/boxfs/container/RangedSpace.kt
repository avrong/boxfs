package org.avrong.boxfs.container

internal class RangedSpace(private val space: Space, val rangeOffset: Long, val rangeSize: Int) {
    private val rangeEnd = rangeOffset + rangeSize

    fun getByte(offset: Int): Byte = withRangeCheck(offset, Byte.SIZE_BYTES) { globalOffset ->
        space.getByteAt(globalOffset)
    }

    fun setByte(offset: Int, value: Byte) = withRangeCheck(offset, Byte.SIZE_BYTES) { globalOffset ->
        space.setByteAt(globalOffset, value)
    }

    fun getInt(offset: Int): Int = withRangeCheck(offset, Int.SIZE_BYTES) { globalOffset ->
        space.getIntAt(globalOffset)
    }
    fun setInt(offset: Int, value: Int) = withRangeCheck(offset, Int.SIZE_BYTES) { globalOffset ->
        space.setIntAt(globalOffset, value)
    }

    fun getLong(offset: Int): Long = withRangeCheck(offset, Long.SIZE_BYTES) { globalOffset ->
        space.getLongAt(globalOffset)
    }

    fun setLong(offset: Int, value: Long) = withRangeCheck(offset, Long.SIZE_BYTES) { globalOffset ->
        space.setLongAt(globalOffset, value)
    }

    fun getString(offset: Int, bytesSize: Int): String = withRangeCheck(offset, bytesSize) { globalOffset ->
        space.getStringAt(globalOffset, bytesSize)
    }

    fun setString(offset: Int, value: String) = withRangeCheck(offset, value.toByteArray().size) { globalOffset ->
        space.setStringAt(globalOffset, value)
    }

    fun getBytes(offset: Int, size: Int): ByteArray = withRangeCheck(offset, size) { globalOffset ->
        space.getBytesAt(globalOffset, size)
    }

    fun setBytes(offset: Int, value: ByteArray) = withRangeCheck(offset, value.size) { globalOffset ->
        space.setBytesAt(globalOffset, value)
    }

    fun exceedsRange(offset: Int, size: Int): Boolean {
        val globalOffset = rangeOffset + offset

        if (globalOffset < rangeOffset || globalOffset > rangeEnd) {
            throw IndexOutOfBoundsException("Offset exceeds range space")
        } else if ((globalOffset + size) > rangeEnd) {
            return true
        }

        return false
    }

    private fun <T> withRangeCheck(offset: Int, valueSize: Int, block: (globalOffset: Long) -> T): T {
        if (exceedsRange(offset, valueSize)) {
            throw java.lang.IndexOutOfBoundsException("Value exceeds range space")
        }

        return block(rangeOffset + offset)
    }
}