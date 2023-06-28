package org.avrong.boxfs

import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.writeBytes

class BoxFs private constructor(val fileTable: FileTable) {
    companion object {
        fun initialize(path: Path, initialTableSize: Int): BoxFs {
            path.createFile()

            val allocationSize = initialTableSize + (initialTableSize * 4)

            val byteBuffer = ByteBuffer.allocate(allocationSize)
            byteBuffer.putInt(initialTableSize)

            path.writeBytes(byteBuffer.toByteArray())

            return open(path)
        }

        fun open(path: Path): BoxFs {
            val file = FileChannel.open(path)

            // Read size
            var byteBuffer = ByteBuffer.allocate(4)
            file.read(byteBuffer)
            val tableSize = byteBuffer.rewind().getInt()

            byteBuffer = ByteBuffer.allocate(tableSize)
            file.read(byteBuffer)
            val fileTable = FileTable.fromByteBuffer(byteBuffer)

            return BoxFs(fileTable)
        }
    }
}

fun ByteBuffer.toByteArray(): ByteArray {
    rewind()
    val buffer = ByteArray(remaining())
    get(buffer)
    return buffer
}
