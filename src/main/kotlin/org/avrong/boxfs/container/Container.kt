package org.avrong.boxfs.container

import org.avrong.boxfs.block.*

internal class Container private constructor (private val space: Space) : AutoCloseable {
    val firstBlock: FirstBlock
        get() {
            val rangedSpace = rangedSpaceForBlock(0)
            return FirstBlock(rangedSpace)
        }

    fun initFirstBlock() {
        val rangedSpace = space.rangedSpace(0, Block.BLOCK_HEADER_SIZE + FirstBlock.BLOCK_DATA_SIZE)
        FirstBlock(rangedSpace).init()
    }

    fun getBlockType(offset: Long): BlockType {
        val typeByte = space.getByteAt(offset + Block.BLOCK_TYPE_OFFSET)
        return BlockType.getBlockType(typeByte)
    }

    fun getBlockSize(offset: Long): Int {
        return space.getIntAt(offset + Block.BLOCK_SIZE_OFFSET)
    }

    fun getDirectoryBlock(offset: Long): DirectoryBlock {
        if (getBlockType(offset) != BlockType.DIRECTORY) throw IllegalArgumentException("Wrong block type")

        val rangedSpace = rangedSpaceForBlock(offset)
        return DirectoryBlock(rangedSpace)
    }

    fun createDirectoryBlock(blockDataSize: Int): DirectoryBlock {
        val rangedSpace = allocateRangedSpaceForBlock(blockDataSize)

        val directoryBlock = DirectoryBlock(rangedSpace)
        directoryBlock.init()

        return DirectoryBlock(rangedSpace)
    }

    fun getFileBlock(offset: Long): FileBlock {
        if (getBlockType(offset) != BlockType.FILE) throw IllegalArgumentException("Wrong block type")

        val rangedSpace = rangedSpaceForBlock(offset)
        return FileBlock(rangedSpace)
    }

    fun createFileBlock(blockDataSize: Int): FileBlock {
        val rangedSpace = allocateRangedSpaceForBlock(blockDataSize)

        val fileBlock = FileBlock(rangedSpace)
        fileBlock.init()

        return fileBlock
    }

    fun getSymbolBlock(offset: Long): SymbolBlock {
        if (getBlockType(offset) != BlockType.SYMBOL) throw IllegalArgumentException("Wrong block type")

        val rangedSpace = rangedSpaceForBlock(offset)
        return SymbolBlock(rangedSpace)
    }

    fun createSymbolBlock(blockDataSize: Int): SymbolBlock {
        val rangedSpace = allocateRangedSpaceForBlock(blockDataSize)

        val symbolBlock = SymbolBlock(rangedSpace)
        symbolBlock.init()

        return symbolBlock
    }

    override fun close() {
        space.close()
    }

    private fun rangedSpaceForBlock(globalOffset: Long): RangedSpace {
        val blockDataSize = Block.getDataSize(space, globalOffset)
        return space.rangedSpace(globalOffset, Block.BLOCK_HEADER_SIZE + blockDataSize)
    }

    private fun allocateRangedSpaceForBlock(blockDataSize: Int): RangedSpace {
        val sizeWithMetadata = Block.BLOCK_HEADER_SIZE + blockDataSize
        return space.rangedSpaceFromEnd(sizeWithMetadata)
    }

    companion object {
        fun fromSpace(space: Space): Container {
            val container = Container(space)

            if (space.isEmpty) {
                container.initFirstBlock()
            }

            return Container(space)
        }
    }
}