package org.avrong.boxfs.block

import org.avrong.boxfs.container.RangedSpace

internal class SymbolBlock(rangedSpace: RangedSpace) : Block(BlockType.SYMBOL, rangedSpace) {
    override fun initBlockData() {
        stringBytesSize = 0
    }

    var stringBytesSize: Int
        get() = rangedSpace.getInt(OCCUPIED_OFFSET)
        private set(value) = rangedSpace.setInt(OCCUPIED_OFFSET, value)

    var string: String
        get() = rangedSpace.getString(STRING_OFFSET, stringBytesSize)
        set(value) {
            rangedSpace.setString(STRING_OFFSET, value)
            stringBytesSize = value.toByteArray().size
        }

    fun checkStringFits(value: String): Boolean {
        val stringLength = value.toByteArray().size

        return !rangedSpace.exceedsRange(STRING_OFFSET, stringLength)
    }

    companion object {
        const val OCCUPIED_SIZE: Int = Int.SIZE_BYTES
        const val OCCUPIED_OFFSET: Int = BLOCK_DATA_OFFSET

        const val STRING_OFFSET: Int = OCCUPIED_OFFSET + OCCUPIED_SIZE

        fun getBlockDataSize(string: String): Int {
            return OCCUPIED_SIZE + string.toByteArray().size
        }
    }
}