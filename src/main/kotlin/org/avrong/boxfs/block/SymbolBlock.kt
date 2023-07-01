package org.avrong.boxfs.block

import org.avrong.boxfs.container.RangedSpace

class SymbolBlock(rangedSpace: RangedSpace) : Block(BlockType.SYMBOL, rangedSpace) {
    override fun initBlockData() {
        length = 0
    }

    var length: Int
        get() = rangedSpace.getInt(OCCUPIED_OFFSET)
        private set(value) = rangedSpace.setInt(OCCUPIED_OFFSET, value)

    var string: String
        get() = rangedSpace.getString(STRING_OFFSET, length)
        set(value) {
            rangedSpace.setString(STRING_OFFSET, value)
            length = value.length
        }

    fun checkStringFits(value: String): Boolean {
        val stringLength = value.length
        val stringBytesSize = stringLength * Char.SIZE_BYTES

        return !rangedSpace.exceedsRange(STRING_OFFSET, stringBytesSize)
    }

    companion object {
        const val OCCUPIED_SIZE: Int = Int.SIZE_BYTES
        const val OCCUPIED_OFFSET: Int = BLOCK_DATA_OFFSET

        const val STRING_OFFSET: Int = OCCUPIED_OFFSET + OCCUPIED_SIZE

        fun getBlockDataSize(string: String): Int {
            return OCCUPIED_SIZE + string.length * Char.SIZE_BYTES
        }
    }
}