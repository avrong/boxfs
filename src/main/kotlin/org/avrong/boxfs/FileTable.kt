package org.avrong.boxfs

import java.nio.ByteBuffer

data class FileTable(val files: MutableList<FileEntry>) {
    fun addEntry(path: String, offset: Long, size: Int) {
        // TODO: Rewrite file is there is one already
        files.add(FileEntry(path, offset, size))
    }

    fun findEntryByPath(path: String): FileEntry? {
        return files.find { it.path == path }
    }

    fun changeEntryPath(pathFrom: String, pathTo: String) {
        files.forEach { if (it.path == pathFrom) it.path = pathTo }
    }

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