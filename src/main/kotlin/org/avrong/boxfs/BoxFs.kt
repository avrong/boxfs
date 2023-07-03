package org.avrong.boxfs

import org.avrong.boxfs.block.BlockType
import org.avrong.boxfs.block.DirectoryBlock
import org.avrong.boxfs.block.SymbolBlock
import org.avrong.boxfs.container.Container
import org.avrong.boxfs.container.Space
import java.nio.file.Path
import kotlin.io.path.createFile

class BoxFs private constructor(
    val path: Path,
    val container: Container
) : AutoCloseable {

    fun createDirectory(path: BoxPath): Boolean {
        // TODO: Make a Directory abstraction so that it handles blocks, expansion and data gathering
        val directoryBlock = getDirectoryBlockByPath(path.withoutLast()) ?: return false
        val directoryEntries = getAllDirectoryEntries(directoryBlock).map { (nameOffset, _) ->
            container.getSymbolBlock(nameOffset).string
        }
        val newDirectoryName = path.last()

        if (directoryEntries.contains(newDirectoryName)) return false

        // Try to find an available block
        var availableBlock: DirectoryBlock
        var currentBlock = directoryBlock
        while (true) {
            if (currentBlock.appendEntryCount > 0) {
                // Use this block
                availableBlock = currentBlock
                break
            }

            if (currentBlock.hasNext) {
                // Move to next block
                currentBlock = container.getDirectoryBlock(currentBlock.nextBlockOffset)
            } else {
                // Create new block
                availableBlock = container.createDirectoryBlock(DirectoryBlock.getAdditionalBlockDataSize(currentBlock.dataSize))
                currentBlock.nextBlockOffset = availableBlock.offset
                break
            }
        }

        // TODO: Make smth like createSymbolBlockWithData
        val symbol = container.createSymbolBlock(SymbolBlock.getBlockDataSize(newDirectoryName))
        symbol.string = newDirectoryName
        val newDirectoryBlock = container.createDirectoryBlock(DirectoryBlock.getInitialBlockDataSize(emptyList()))
        val directoryBlockEntry = DirectoryBlock.DirectoryBlockEntry(symbol.offset, newDirectoryBlock.offset)

        availableBlock.appendEntries(listOf(directoryBlockEntry))

        return true
    }

    fun list(path: BoxPath): List<BoxPath>? {
        val directoryBlock = getDirectoryBlockByPath(path) ?: return null
        return getAllDirectoryEntries(directoryBlock).map { (nameOffset, _) ->
            val name = container.getSymbolBlock(nameOffset).string
            path.with(name)
        }
    }

    private fun getDirectoryBlockByPath(path: BoxPath): DirectoryBlock? {
        var currentBlock = container.getDirectoryBlock(container.firstBlock.rootDirectoryOffset)

        for (dirName in path.pathList) {
            var nextDir: DirectoryBlock? = null

            for ((nameOffset, valueOffset) in getAllDirectoryEntries(currentBlock)) {
                val name = container.getSymbolBlock(nameOffset).string
                val blockType = container.getBlockType(valueOffset)

                if (name == dirName && blockType == BlockType.DIRECTORY) {
                    nextDir = container.getDirectoryBlock(valueOffset)
                }
            }

            if (nextDir == null) {
                return null
            } else {
                currentBlock = nextDir
            }
        }

        return currentBlock
    }

    private fun getAllDirectoryEntries(firstDirectoryBlock: DirectoryBlock): List<DirectoryBlock.DirectoryBlockEntry> {
        val entriesList = mutableListOf<DirectoryBlock.DirectoryBlockEntry>()

        var currentBlock = firstDirectoryBlock
        while (true) {
            entriesList.addAll(currentBlock.entries)

            if (currentBlock.hasNext) {
                currentBlock = container.getDirectoryBlock(currentBlock.nextBlockOffset)
            } else {
                break
            }
        }

        return entriesList
    }

    override fun close() {
        container.close()
    }

    companion object {
        fun create(path: Path): BoxFs {
            path.createFile()

            val space = Space.fromPath(path)
            val container = Container.fromSpace(space)
            createRootDir(container)

            return BoxFs(path, container)
        }

        fun open(path: Path): BoxFs {
            val space = Space.fromPath(path)
            val container = Container.fromSpace(space)

            // Init root dir if does not exists
            if (container.firstBlock.rootDirectoryOffset == 0L) {
                createRootDir(container)
            }

            return BoxFs(path, container)
        }

        private fun createRootDir(container: Container) {
            val rootDirBlock = container.createDirectoryBlock(DirectoryBlock.getInitialBlockDataSize(emptyList()))
            container.firstBlock.rootDirectoryOffset = rootDirBlock.offset
        }
    }
}
