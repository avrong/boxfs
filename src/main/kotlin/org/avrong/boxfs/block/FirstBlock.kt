package org.avrong.boxfs.block

import org.avrong.boxfs.container.RangedSpace

class FirstBlock (override val rangedSpace: RangedSpace) : Block {
    var rootDirectoryOffset: Long
        get() = rangedSpace.getLong(ROOT_DIRECTORY_OFFSET)
        set(value) = rangedSpace.setLong(ROOT_DIRECTORY_OFFSET, value)

    fun writeHeader() {
        rangedSpace.setByte(Block.TYPE_OFFSET, BlockType.FIRST.byte)
        rangedSpace.setInt(Block.SIZE_OFFSET, ROOT_DIRECTORY_OFFSET + ROOT_DIRECTORY_SIZE)
        rangedSpace.setLong(ROOT_DIRECTORY_OFFSET, 0)
    }

    companion object {
        const val ROOT_DIRECTORY_SIZE: Int = Long.SIZE_BYTES
        const val ROOT_DIRECTORY_OFFSET: Int = Block.SIZE_OFFSET + Block.SIZE_SIZE

        const val BLOCK_SIZE: Int = ROOT_DIRECTORY_OFFSET + ROOT_DIRECTORY_SIZE
    }
}