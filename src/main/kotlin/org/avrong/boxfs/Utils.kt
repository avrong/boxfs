package org.avrong.boxfs

import java.nio.ByteBuffer

fun ByteBuffer.toByteArray(): ByteArray {
    rewind()
    val buffer = ByteArray(remaining())
    get(buffer)
    return buffer
}
