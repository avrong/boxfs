package org.avrong.boxfs.container

import org.avrong.boxfs.block.*

class Container(private val space: Space) : AutoCloseable {
    val firstBlock: FirstBlock
        get() {
            val rangedSpace = RangedSpace(space, 0, FirstBlock.BLOCK_SIZE)
            return FirstBlock(rangedSpace)
        }

    fun getBlockType(offset: Long): BlockType {
        val typeByte = space.getByteAt(offset + Block.TYPE_OFFSET)
        return BlockType.getBlockType(typeByte)
    }

    fun getBlockSize(offset: Long): Int {
        return space.getIntAt(offset + Block.SIZE_OFFSET)
    }

    fun getDirectoryBlock(offset: Long): DirectoryBlock {
        val rangedSpace = rangedSpaceForBlock(offset)
        return DirectoryBlock(rangedSpace)
    }

    fun getFileBlock(offset: Long): FileBlock {
        val rangedSpace = rangedSpaceForBlock(offset)
        return FileBlock(rangedSpace)
    }

    fun getSymbolBlock(offset: Long): SymbolBlock {
        val rangedSpace = rangedSpaceForBlock(offset)
        return SymbolBlock(rangedSpace)
    }

    fun createFirstBlock() {
        firstBlock.writeHeader()
    }

    override fun close() {
        space.close()
    }

    private fun rangedSpaceForBlock(globalOffset: Long): RangedSpace {
        val blockSize = Block.getSize(space, globalOffset)
        return RangedSpace(space, globalOffset, blockSize)
    }
}