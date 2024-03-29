package org.avrong.boxfs

import org.avrong.boxfs.visitor.BoxFsVisitor
import java.nio.file.Path

// Fs methods

interface Box {
    // Both files and dirs
    fun exists(path: BoxPath): Boolean
    fun move(pathFrom: BoxPath, pathTo: BoxPath): Boolean
    fun delete(path: BoxPath): Boolean
    fun rename(pathFrom: BoxPath, pathTo: BoxPath): Boolean
    fun copy(pathFrom: BoxPath, pathTo: BoxPath): Boolean
    fun isDirectory(path: BoxPath): Boolean
    fun isFile(path: BoxPath): Boolean

    // Dirs only
    fun createDirectory(path: BoxPath): Boolean
    fun createDirectories(path: BoxPath): Boolean
    fun listDirectory(path: BoxPath): List<BoxPath>?
    fun visitFileTree(dirPath: BoxPath, visitor: BoxFsVisitor)

    // Files only
    fun createFile(path: BoxPath): Boolean
    fun writeFile(path: BoxPath, byteArray: ByteArray): Boolean
    fun appendFile(path: BoxPath, byteArray: ByteArray): Boolean
    fun readFile(path: BoxPath): ByteArray?
    fun getFileSize(path: BoxPath): Int?

    // Service
    fun populate(path: Path, internalPath: BoxPath)
    fun materialize(internalDirPath: BoxPath, outputDirPath: Path)
}