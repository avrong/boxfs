package org.avrong.boxfs.block

import org.avrong.boxfs.container.RangedSpace
import kotlin.math.max

class DirectoryBlock(rangedSpace: RangedSpace) : Block(BlockType.DIRECTORY, rangedSpace) {
    override fun initBlockData() {
        nextBlockOffset = 0
        entryCount = 0
    }

    var nextBlockOffset: Long
        get() = rangedSpace.getLong(NEXT_BLOCK_OFFSET)
        set(value) = rangedSpace.setLong(NEXT_BLOCK_OFFSET, value)

    var entryCount: Int
        get() = rangedSpace.getInt(ENTRY_COUNT_OFFSET)
        private set(count) = rangedSpace.setInt(ENTRY_COUNT_OFFSET, count)

    var entries: List<DirectoryBlockEntry>
        get() = getEntryList()
        set(value) = setEntryList(value)

    val maxEntryCount: Int
        get() = (dataSize - (NEXT_BLOCK_SIZE + ENTRY_COUNT_SIZE)) / SINGLE_ENTRY_SIZE

    val appendEntryCount: Int
        get() = maxEntryCount - entryCount

    fun appendEntries(entryList: List<DirectoryBlockEntry>) {
        val appendOffset = ENTRIES_OFFSET + entryCount * SINGLE_ENTRY_SIZE

        for ((index, entry) in entryList.withIndex()) {
            val symbolOffset = appendOffset + index * SINGLE_ENTRY_SIZE
            val fileOffset = symbolOffset + Long.SIZE_BYTES

            rangedSpace.setLong(symbolOffset, entry.nameOffset)
            rangedSpace.setLong(fileOffset, entry.fileOffset)
        }
        entryCount += entryList.size
    }

    private fun getEntryList(): List<DirectoryBlockEntry> {
        val entryList = mutableListOf<DirectoryBlockEntry>()

        for (i in 0 until entryCount) {
            val symbolOffset = ENTRIES_OFFSET + i * SINGLE_ENTRY_SIZE
            val fileOffset = symbolOffset + Long.SIZE_BYTES

            val entry = DirectoryBlockEntry(rangedSpace.getLong(symbolOffset), rangedSpace.getLong(fileOffset))
            entryList.add(entry)
        }

        return entryList
    }

    private fun setEntryList(entryList: List<DirectoryBlockEntry>) {
        for ((index, entry) in entryList.withIndex()) {
            val symbolOffset = ENTRIES_OFFSET + index * SINGLE_ENTRY_SIZE
            val fileOffset = symbolOffset + Long.SIZE_BYTES

            rangedSpace.setLong(symbolOffset, entry.nameOffset)
            rangedSpace.setLong(fileOffset, entry.fileOffset)
        }
        entryCount = entryList.size
    }

    data class DirectoryBlockEntry(val nameOffset: Long, val fileOffset: Long)

    companion object {
        const val NEXT_BLOCK_SIZE: Int = Long.SIZE_BYTES
        const val NEXT_BLOCK_OFFSET: Int = BLOCK_DATA_OFFSET

        const val ENTRY_COUNT_SIZE: Int = Int.SIZE_BYTES
        const val ENTRY_COUNT_OFFSET: Int = NEXT_BLOCK_OFFSET + NEXT_BLOCK_SIZE

        const val ENTRIES_OFFSET: Int = ENTRY_COUNT_OFFSET + ENTRY_COUNT_SIZE
        const val SINGLE_ENTRY_SIZE: Int = Long.SIZE_BYTES + Long.SIZE_BYTES

        const val MIN_INITIAL_ENTRIES_COUNT = 5

        fun getInitialBlockDataSize(entryList: List<DirectoryBlockEntry>): Int {
            return NEXT_BLOCK_SIZE + ENTRY_COUNT_SIZE + max(entryList.size * SINGLE_ENTRY_SIZE, MIN_INITIAL_ENTRIES_COUNT)
        }

        fun getPreemptiveBlockDataSize(previousBlockSize: Int): Int {
            if (previousBlockSize == 0) {
                return NEXT_BLOCK_SIZE + ENTRY_COUNT_SIZE + (5 * SINGLE_ENTRY_SIZE)
            }

            return (previousBlockSize - (NEXT_BLOCK_SIZE + ENTRY_COUNT_SIZE)) * 2
        }
    }
}