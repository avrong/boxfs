package org.avrong.boxfs

import org.avrong.boxfs.block.BlockType
import org.avrong.boxfs.block.DirectoryBlock
import org.avrong.boxfs.block.FileBlock
import org.avrong.boxfs.block.SymbolBlock
import org.avrong.boxfs.container.Container
import org.avrong.boxfs.container.Space
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.math.min

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

        // TODO: Make smth like createSymbolBlockWithData
        val symbol = container.createSymbolBlock(SymbolBlock.getBlockDataSize(newDirectoryName))
        symbol.string = newDirectoryName
        val newDirectoryBlock = container.createDirectoryBlock(DirectoryBlock.getInitialBlockDataSize(emptyList()))
        val directoryBlockEntry = DirectoryBlock.DirectoryBlockEntry(symbol.offset, newDirectoryBlock.offset)

        val availableBlock = findAvailableDirectoryBlock(directoryBlock)
        availableBlock.appendEntry(directoryBlockEntry)

        return true
    }

    fun createDirectories(path: BoxPath): Boolean {
        var currentPath = BoxPath("/")
        var isSuccess = false
        for (part in path.pathList) {
            currentPath = currentPath.with(part)

            if (!exists(currentPath)) {
                isSuccess = createDirectory(currentPath)
            }
        }

        return isSuccess
    }

    fun listDirectory(path: BoxPath): List<BoxPath>? {
        val directoryBlock = getDirectoryBlockByPath(path) ?: return null
        return getAllDirectoryEntries(directoryBlock).map { (nameOffset, _) ->
            val name = container.getSymbolBlock(nameOffset).string
            path.with(name)
        }
    }

    fun createFile(path: BoxPath): Boolean {
        val directoryPath = path.withoutLast()
        val fileName = path.last()
        val directoryBlock = getDirectoryBlockByPath(directoryPath) ?: return false
        val directoryEntries = getAllDirectoryEntries(directoryBlock).map { (nameOffset, _) ->
            val symbolBlock = container.getSymbolBlock(nameOffset)
            symbolBlock.string
        }

        // TODO: Introduce smth like checkNameUnique
        if (directoryEntries.contains(fileName)) return false

        val symbolBlock = container.createSymbolBlock(SymbolBlock.getBlockDataSize(fileName))
        symbolBlock.string = fileName
        val fileBlock = container.createFileBlock(FileBlock.getInitialBlockDataSize(ByteArray(0)))
        val directoryEntry = DirectoryBlock.DirectoryBlockEntry(symbolBlock.offset, fileBlock.offset)
        val availableDirectoryBlock = findAvailableDirectoryBlock(directoryBlock)
        availableDirectoryBlock.appendEntry(directoryEntry)

        return true
    }

    fun writeFile(path: BoxPath, byteArray: ByteArray): Boolean {
        val targetFileBlock = getFileBlockByPath(path) ?: return false

        val byteArrayStream = ByteArrayInputStream(byteArray)
        var contentSizeLeft = byteArray.size
        var currentBlock: FileBlock = targetFileBlock
        // TODO: Try to get rid of while(true) blocks
        while (true) {
            val chunk = ByteArray(min(currentBlock.maxContentSize, contentSizeLeft))
            val bytesRead = byteArrayStream.read(chunk)

            if (bytesRead == -1) {
                contentSizeLeft = 0
            } else {
                contentSizeLeft -= bytesRead
            }

            currentBlock.content = chunk

            if (currentBlock.hasNext) {
                currentBlock = container.getFileBlock(currentBlock.nextBlockOffset)
            } else if (contentSizeLeft > 0) {
                val newBlock = container.createFileBlock(FileBlock.getAdditionalBlockDataSize(contentSizeLeft, currentBlock.dataSize))
                currentBlock.nextBlockOffset = newBlock.offset
                currentBlock = newBlock
            } else {
                break
            }
        }

        return true
    }

    fun appendFile(path: BoxPath, byteArray: ByteArray): Boolean {
        val targetFileBlock = getFileBlockByPath(path) ?: return false

        val byteArrayStream = ByteArrayInputStream(byteArray)
        var contentSizeLeft = byteArray.size
        var currentBlock: FileBlock = targetFileBlock
        while (true) {
            // TODO: If there are non-full blocks in the middle of file (which ideally should not ever happen),
            //  then it will write to the middle block where there is space. This will corrupt file!
            if (currentBlock.appendContentSize > 0) {
                val chunk = ByteArray(min(currentBlock.appendContentSize, contentSizeLeft))
                val bytesRead = byteArrayStream.read(chunk)
                contentSizeLeft -= bytesRead

                val newContentStream = ByteArrayOutputStream()
                newContentStream.writeBytes(currentBlock.content)
                newContentStream.writeBytes(chunk)
                currentBlock.content = newContentStream.toByteArray()
            }

            if (contentSizeLeft == 0) break

            if (currentBlock.hasNext) {
                currentBlock = container.getFileBlock(currentBlock.nextBlockOffset)
            } else {
                val newBlock = container.createFileBlock(FileBlock.getAdditionalBlockDataSize(contentSizeLeft, currentBlock.dataSize))
                currentBlock.nextBlockOffset = newBlock.offset
                currentBlock = newBlock
            }
        }

        return true
    }

    fun readFile(path: BoxPath): ByteArray? {
        val targetFileBlock = getFileBlockByPath(path) ?: return null

        val byteStream = ByteArrayOutputStream()
        var currentBlock = targetFileBlock
        while (true) {
            byteStream.writeBytes(currentBlock.content)

            if (currentBlock.hasNext) {
                currentBlock = container.getFileBlock(currentBlock.nextBlockOffset)
            } else {
                break
            }
        }

        return byteStream.toByteArray()
    }

    fun getFileSize(path: BoxPath): Int? {
        val targetFileBlock = getFileBlockByPath(path) ?: return null

        var size = 0
        var currentBlock = targetFileBlock
        while (true) {
            size += currentBlock.contentSize

            if (currentBlock.hasNext) {
                currentBlock = container.getFileBlock(currentBlock.nextBlockOffset)
            } else {
                break
            }
        }

        return size
    }

    fun exists(path: BoxPath): Boolean {
        if (path.pathList.isEmpty()) return true

        val directoryPath = path.withoutLast()
        val entryName = path.last()

        val directoryBlock = getDirectoryBlockByPath(directoryPath) ?: return false
        val directoryEntries = getAllDirectoryEntries(directoryBlock)

        for ((symbolOffset, _) in directoryEntries) {
            val name = container.getSymbolBlock(symbolOffset).string

            if (name == entryName) {
                return true
            }
        }

        return false
    }

    fun delete(path: BoxPath): Boolean {
        val directoryPath = path.withoutLast()
        val elementName = path.last()

        val directoryBlock = getDirectoryBlockByPath(directoryPath) ?: return false
        val directoryEntries = getAllDirectoryEntries(directoryBlock)

        // TODO: Writes can be optimized here, we should swap the deleted entry with last existing

        val updatedDirectoryEntries = directoryEntries.filterNot {
            val name = container.getSymbolBlock(it.nameOffset).string
            name == elementName
        }
        if (directoryEntries.size == updatedDirectoryEntries.size) return false

        writeDirectoryEntries(directoryBlock, updatedDirectoryEntries)
        return true
    }

    fun move(pathFrom: BoxPath, pathTo: BoxPath): Boolean {
        val fromDirectoryPath = pathFrom.withoutLast()
        val fromElementName = pathFrom.last()
        val toDirectoryPath = pathTo.withoutLast()
        val toElementName = pathTo.last()

        val fromDirectoryBlock = getDirectoryBlockByPath(fromDirectoryPath) ?: return false
        val toDirectoryBlock = getDirectoryBlockByPath(toDirectoryPath) ?: return false

        val fromDirectoryBlockEntries = getAllDirectoryEntries(fromDirectoryBlock)
        val fromDirectoryBlockEntriesNames = fromDirectoryBlockEntries.map { container.getSymbolBlock(it.nameOffset).string }

        val directoryEntryIndex = fromDirectoryBlockEntriesNames.indexOf(fromElementName)
        if (directoryEntryIndex == -1) return false

        val elementToMove = fromDirectoryBlockEntries[directoryEntryIndex]

        // TODO: We might need to merge dirs here if directory is moved. For now, let's just reject move if there is
        //  already dir/file with the same name
        val toDirectoryBlockEntries = getAllDirectoryEntries(toDirectoryBlock)
        val toDirectoryBlockEntriesNames = toDirectoryBlockEntries.map { container.getSymbolBlock(it.nameOffset).string }
        if (toDirectoryBlockEntriesNames.contains(toElementName)) return false

        val updatedFromDirectoryEntries = fromDirectoryBlockEntries.filterIndexed { idx, _ -> idx != directoryEntryIndex }
        writeDirectoryEntries(fromDirectoryBlock, updatedFromDirectoryEntries)

        val availableBlock = findAvailableDirectoryBlock(toDirectoryBlock)
        val symbolBlock = container.getSymbolBlock(elementToMove.nameOffset)
        val updatedSymbol = updateSymbol(symbolBlock, toElementName)
        availableBlock.appendEntry(elementToMove.copy(nameOffset = updatedSymbol.offset))

        return true
    }

    fun rename(pathFrom: BoxPath, pathTo: BoxPath): Boolean {
        val fromDirectoryPath = pathFrom.withoutLast()
        val toDirectoryPath = pathTo.withoutLast()

        // Do not rename if entries aren't in the same directory
        if (fromDirectoryPath != toDirectoryPath) return false

        val fromName = pathFrom.last()
        val toName = pathTo.last()

        val directoryBlock = getDirectoryBlockByPath(fromDirectoryPath) ?: return false
        val directoryEntries = getAllDirectoryEntries(directoryBlock)

        // TODO: Can be optimized if we would have ability to edit list entry individually
        var found = false
        val updatedDirectoryEntries = directoryEntries.map {
            val symbol = container.getSymbolBlock(it.nameOffset)
            if (symbol.string == fromName) {
                found = true

                val updatedSymbol = updateSymbol(symbol, toName)
                it.copy(nameOffset = updatedSymbol.offset)
            } else {
                it
            }
        }
        if (!found) return false

        writeDirectoryEntries(directoryBlock, updatedDirectoryEntries)
        return true
    }

    fun populate(path: Path, internalPath: BoxPath) {
        // Takes children of `path` and puts into `internalPath`
        val visitor = PopulateFileVisitor(this, path)
        Files.walkFileTree(path, visitor)
    }

    private fun updateSymbol(symbolBlock: SymbolBlock, name: String): SymbolBlock {
        return if (symbolBlock.checkStringFits(name)) {
            symbolBlock.string = name
            symbolBlock
        } else {
            val newSymbolBlock = container.createSymbolBlock(SymbolBlock.getBlockDataSize(name))
            newSymbolBlock.string = name
            newSymbolBlock
        }
    }

    private fun writeDirectoryEntries(firstDirectoryBlock: DirectoryBlock, entries: List<DirectoryBlock.DirectoryBlockEntry>) {
        var reader = entries
        var currentBlock: DirectoryBlock = firstDirectoryBlock
        while (true) {
            val count = min(currentBlock.maxEntryCount, entries.size)
            val currentEntries = reader.take(count)
            reader = reader.drop(count)
            currentBlock.entries = currentEntries

            if (currentBlock.hasNext) {
                currentBlock = container.getDirectoryBlock(currentBlock.nextBlockOffset)
            } else if (reader.isNotEmpty()) {
                val newBlock = container.createDirectoryBlock(DirectoryBlock.getAdditionalBlockDataSize(currentBlock.dataSize))
                currentBlock.nextBlockOffset = newBlock.offset
                currentBlock = newBlock
            } else {
                break
            }
        }
    }

    private fun findAvailableDirectoryBlock(directoryBlock: DirectoryBlock): DirectoryBlock {
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

        return availableBlock
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

    fun getFileBlockByPath(path: BoxPath): FileBlock? {
        // TODO: Search can be optimized, return as soon as we found the needed file
        //  Smth like directory iterator?
        val directoryPath = path.withoutLast()
        val fileName = path.last()
        val directoryBlock = getDirectoryBlockByPath(directoryPath) ?: return null
        val directoryEntries = getAllDirectoryEntries(directoryBlock)

        for ((symbolOffset, blockOffset) in directoryEntries) {
            val name = container.getSymbolBlock(symbolOffset).string

            if (name == fileName) {
                return if (container.getBlockType(blockOffset) == BlockType.FILE) {
                    container.getFileBlock(blockOffset)
                } else {
                    null
                }
            }
        }

        return null
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
