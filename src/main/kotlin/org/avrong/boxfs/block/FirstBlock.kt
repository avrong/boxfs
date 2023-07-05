package org.avrong.boxfs.block

import org.avrong.boxfs.container.RangedSpace

internal class FirstBlock(rangedSpace: RangedSpace) : Block(BlockType.FIRST, rangedSpace) {
    override fun initBlockData() {
        rootDirectoryOffset = 0
    }

    var rootDirectoryOffset: Long
        get() = rangedSpace.getLong(ROOT_DIRECTORY_OFFSET)
        set(value) = rangedSpace.setLong(ROOT_DIRECTORY_OFFSET, value)

    companion object {
        private const val ROOT_DIRECTORY_SIZE: Int = Long.SIZE_BYTES
        const val ROOT_DIRECTORY_OFFSET: Int = BLOCK_DATA_OFFSET

        const val BLOCK_DATA_SIZE: Int = ROOT_DIRECTORY_SIZE
    }
}