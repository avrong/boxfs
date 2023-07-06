package org.avrong.boxfs.block

import org.avrong.boxfs.container.RangedSpace
import kotlin.math.max

internal class FileBlock(rangedSpace: RangedSpace) : Block(BlockType.FILE, rangedSpace) {
    override fun initBlockData() {
        nextBlockOffset = 0
        contentSize = 0
    }

    var nextBlockOffset: Long
        get() = rangedSpace.getLong(NEXT_BLOCK_OFFSET)
        set(value) = rangedSpace.setLong(NEXT_BLOCK_OFFSET, value)

    var contentSize: Int
        get() = rangedSpace.getInt(CONTENT_SIZE_OFFSET)
        private set(value) = rangedSpace.setInt(CONTENT_SIZE_OFFSET, value)

    var content: ByteArray
        get() = rangedSpace.getBytes(CONTENT_OFFSET, contentSize)
        set(value) {
            rangedSpace.setBytes(CONTENT_OFFSET, value)
            contentSize = value.size
        }

    val maxContentSize: Int
        get() = dataSize - (NEXT_BLOCK_SIZE + CONTENT_SIZE_SIZE)

    val appendContentSize: Int
        get() = maxContentSize - contentSize

    val hasNext: Boolean
        get() = nextBlockOffset != 0L

    fun appendContent(value: ByteArray) {
        rangedSpace.setBytes(CONTENT_OFFSET + contentSize, value)
        contentSize += value.size
    }

    companion object {
        const val NEXT_BLOCK_SIZE: Int = Long.SIZE_BYTES
        const val NEXT_BLOCK_OFFSET: Int = BLOCK_DATA_OFFSET

        const val CONTENT_SIZE_SIZE: Int = Int.SIZE_BYTES
        const val CONTENT_SIZE_OFFSET: Int = NEXT_BLOCK_OFFSET + NEXT_BLOCK_SIZE

        const val CONTENT_OFFSET: Int = CONTENT_SIZE_OFFSET + CONTENT_SIZE_SIZE

        const val MIN_INITIAL_BLOCK_CONTENT_SIZE: Int = 64

        fun getInitialBlockDataSize(contentSize: Int): Int {
            return NEXT_BLOCK_SIZE + CONTENT_SIZE_SIZE + max(contentSize, MIN_INITIAL_BLOCK_CONTENT_SIZE)
        }

        fun getAdditionalBlockDataSize(contentSize: Int, previousBlockSize: Int): Int {
            val previousContentSize = previousBlockSize - (NEXT_BLOCK_SIZE + CONTENT_SIZE_SIZE)
            return NEXT_BLOCK_SIZE + CONTENT_SIZE_SIZE + contentSize + (previousContentSize * 1.5).toInt()
        }
    }
}