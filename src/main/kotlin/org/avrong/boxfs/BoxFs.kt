package org.avrong.boxfs

import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.writeBytes

class BoxFs private constructor(
    val path: Path,
    val fileTable: FileTable,
    private val randomAccessFile: RandomAccessFile,
    private var fileOffset: Long
) {
    fun open(path: String): BoxFile? {
        val entry = fileTable.findEntryByPath(path) ?: return null
        return BoxFile(path, entry.offset, entry.size, randomAccessFile)
    }

    fun write(path: String, content: ByteArray): BoxFile {
        val offset = fileOffset
        val size = content.size

        randomAccessFile.seek(fileOffset)
        randomAccessFile.write(content)
        fileOffset += size

        val entry = fileTable.addEntry(path, offset, size)
        return BoxFile(path, offset, size, randomAccessFile)
    }

    fun move(pathFrom: String, pathTo: String) {
        fileTable.changeEntryPath(pathFrom, pathTo)
    }

    companion object {
        fun initialize(path: Path, initialTableSize: Int): BoxFs {
            path.createFile()

            val allocationSize = Int.SIZE_BYTES + initialTableSize

            val byteBuffer = ByteBuffer.allocate(allocationSize)
            byteBuffer.putInt(initialTableSize)

            path.writeBytes(byteBuffer.toByteArray())

            return open(path)
        }

        fun open(path: Path): BoxFs {
            val file = FileChannel.open(path)

            val fileTable: FileTable
            val size: Long

            FileChannel.open(path).use {
                // Read size
                var byteBuffer = ByteBuffer.allocate(4)
                file.read(byteBuffer)
                val tableSize = byteBuffer.rewind().getInt()

                byteBuffer = ByteBuffer.allocate(tableSize)
                file.read(byteBuffer)

                fileTable = FileTable.fromByteBuffer(byteBuffer)
                size = file.size()
            }

            val randomAccessFile = RandomAccessFile(path.toString(), "rws")
            return BoxFs(path, fileTable, randomAccessFile, size)
        }
    }
}

fun ByteBuffer.toByteArray(): ByteArray {
    rewind()
    val buffer = ByteArray(remaining())
    get(buffer)
    return buffer
}
