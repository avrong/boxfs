package org.avrong.boxfs.block

import org.avrong.boxfs.container.RangedSpace

class SymbolBlock(override val rangedSpace: RangedSpace) : Block {
    var occupied: Int
        get() = rangedSpace.getInt(OCCUPIED_OFFSET)
        set(value) = rangedSpace.setInt(OCCUPIED_OFFSET, value)

    var string: String
        get() = rangedSpace.getString(STRING_OFFSET, occupied)
        set(value) = rangedSpace.setString(STRING_OFFSET, value)

    fun checkStringFits(value: String): Boolean {
        val stringLength = value.length
        val stringBytesSize = stringLength * Char.SIZE_BYTES

        return !rangedSpace.exceedsRange(STRING_OFFSET, stringBytesSize)
    }

    companion object {
        const val OCCUPIED_SIZE: Int = Int.SIZE_BYTES
        const val OCCUPIED_OFFSET: Int = Block.SIZE_OFFSET

        const val STRING_OFFSET: Int = OCCUPIED_OFFSET + OCCUPIED_SIZE
    }
}