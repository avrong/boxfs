package org.avrong.boxfs

import java.nio.ByteBuffer

data class FileTable(val files: List<FileEntry>) {
    fun toBytes(): ByteArray {
        val bytes = mutableListOf<Byte>()

        for (fileEntry in files) {
            bytes.addAll(fileEntry.toBytes().toList())
        }

        return bytes.toByteArray()
    }

    companion object {
        fun fromByteBuffer(byteBuffer: ByteBuffer): FileTable {
            val files = mutableListOf<FileEntry>()

            while (byteBuffer.remaining() > 0) {
                val fileEntry = FileEntry.fromByteBuffer(byteBuffer) ?: break
                files.add(fileEntry)
            }

            return FileTable(files)
        }
    }
}