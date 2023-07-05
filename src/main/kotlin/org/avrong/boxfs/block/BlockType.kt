package org.avrong.boxfs.block

internal enum class BlockType {
    FIRST, SYMBOL, DIRECTORY, FILE;

    val byte: Byte get() = ordinal.toByte()

    companion object {
        fun getBlockType(code: Byte): BlockType {
            return BlockType.values().find { it.byte == code } ?: throw RuntimeException("No such block type '$code'")
        }
    }
}