package org.avrong.boxfs.block

import org.avrong.boxfs.container.RangedSpace

class DirectoryBlock(rangedSpace: RangedSpace) : Block(BlockType.DIRECTORY, rangedSpace) {
    override fun initBlockData() {
        TODO("Not yet implemented")
    }

    var entryCount: Int
        get() = rangedSpace.getInt(ENTRY_COUNT_OFFSET)
        set(count) = rangedSpace.setInt(ENTRY_COUNT_OFFSET, count)

    // TODO: Implement getEntry, addEntry, block size limit checks

    companion object {
        const val ENTRY_COUNT_SIZE: Int = Int.SIZE_BYTES
        const val ENTRY_COUNT_OFFSET: Int = BLOCK_DATA_OFFSET

        const val ELEMENTS_OFFSET: Int = ENTRY_COUNT_OFFSET + ENTRY_COUNT_SIZE
    }
}