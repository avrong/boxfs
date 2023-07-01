package org.avrong.boxfs.block

import org.avrong.boxfs.container.RangedSpace
import org.avrong.boxfs.container.Space

abstract class Block(val type: BlockType, protected val rangedSpace: RangedSpace) {
    val spaceSize: Int
        get() = rangedSpace.rangeSize

    val headerSize: Int = BLOCK_HEADER_SIZE
    val dataSize: Int = spaceSize - headerSize

    protected abstract fun initBlockData()

    fun init() {
        initBlockHeader()
        initBlockData()
    }

    private fun initBlockHeader() {
        rangedSpace.setByte(BLOCK_TYPE_OFFSET, type.byte)
        rangedSpace.setInt(BLOCK_SIZE_OFFSET, spaceSize - BLOCK_HEADER_SIZE)
    }

    companion object {
        const val BLOCK_TYPE_SIZE: Int = Byte.SIZE_BYTES
        const val BLOCK_TYPE_OFFSET: Int = 0

        const val BLOCK_SIZE_SIZE: Int = Int.SIZE_BYTES
        const val BLOCK_SIZE_OFFSET: Int = BLOCK_TYPE_OFFSET + BLOCK_TYPE_SIZE

        const val BLOCK_HEADER_SIZE: Int = BLOCK_TYPE_SIZE + BLOCK_SIZE_SIZE
        const val BLOCK_DATA_OFFSET: Int = BLOCK_SIZE_OFFSET + BLOCK_SIZE_SIZE

        fun getDataSize(space: Space, globalOffset: Long): Int {
            return space.getIntAt(globalOffset + BLOCK_SIZE_OFFSET)
        }
    }
}