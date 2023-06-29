package org.avrong.boxfs

import java.io.RandomAccessFile

data class BoxFile(
    val path: String,
    private val offset: Long,
    private val size: Int,
    private val randomAccessFile: RandomAccessFile
) {
    val isDirectory: Boolean get() = offset == -1L

    fun getContent(): ByteArray {
        val content = ByteArray(size)
        randomAccessFile.seek(offset)
        randomAccessFile.read(content, 0, size)
        return content
    }
}