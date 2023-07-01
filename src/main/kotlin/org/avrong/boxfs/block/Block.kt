package org.avrong.boxfs.block

import org.avrong.boxfs.container.RangedSpace
import org.avrong.boxfs.container.Space

interface Block {
    val rangedSpace: RangedSpace

    val size: Int
        get() = rangedSpace.getInt(SIZE_OFFSET)

    companion object {
        const val TYPE_SIZE: Int = Byte.SIZE_BYTES
        const val TYPE_OFFSET: Int = 0

        const val SIZE_SIZE: Int = Int.SIZE_BYTES
        const val SIZE_OFFSET: Int = TYPE_OFFSET + TYPE_SIZE

        fun getSize(space: Space, globalOffset: Long): Int {
            return space.getIntAt(globalOffset + SIZE_OFFSET)
        }
    }
}