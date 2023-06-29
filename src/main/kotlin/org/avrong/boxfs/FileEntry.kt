package org.avrong.boxfs

import java.nio.ByteBuffer


data class FileEntry(var path: String, val offset: Long, val size: Int) {
    fun toBytes(): ByteArray {
        val pathBytes = path.toByteArray()
        val pathSize = pathBytes.size
        val byteSize = Int.SIZE_BYTES + pathSize + Long.SIZE_BYTES + Int.SIZE_BYTES
        val byteBuffer = ByteBuffer.allocate(byteSize)
        byteBuffer.putInt(pathSize)
        byteBuffer.put(pathBytes)
        byteBuffer.putLong(offset)
        byteBuffer.putInt(size)

        return byteBuffer.toByteArray()
    }

    companion object {
        fun fromByteBuffer(byteBuffer: ByteBuffer): FileEntry? {
            val pathSize = byteBuffer.getInt()
            if (pathSize == 0) return null

            val buffer = ByteArray(pathSize)
            byteBuffer.get(buffer)
            val path = String(buffer)

            val offset = byteBuffer.getLong()
            val size = byteBuffer.getInt()

            return FileEntry(path, offset, size)
        }
    }
}
