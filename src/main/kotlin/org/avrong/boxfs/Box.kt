package org.avrong.boxfs

import org.avrong.boxfs.visitor.BoxFsVisitor
import java.nio.file.Path

interface Box {
    // Both files and dirs
    fun exists(path: BoxPath): Boolean
    fun move(pathFrom: BoxPath, pathTo: BoxPath): Boolean
    fun delete(path: BoxPath): Boolean
    fun rename(pathFrom: BoxPath, pathTo: BoxPath): Boolean

    // Dirs only
    fun createDirectory(path: BoxPath): Boolean
    fun createDirectories(path: BoxPath): Boolean
    fun listDirectory(path: BoxPath): List<BoxPath>?
    // fun copyDirectory(pathFrom: BoxPath, pathTo: BoxPath): Boolean
    fun visitFileTree(dirPath: BoxPath, visitor: BoxFsVisitor)

    // Files only
    fun createFile(path: BoxPath): Boolean
    fun writeFile(path: BoxPath, bytes: ByteArray): Boolean
    fun appendFile(path: BoxPath, bytes: ByteArray): Boolean
    // fun copyFile(pathFrom: BoxPath, pathTo: BoxPath): Boolean
    fun readFile(path: BoxPath): ByteArray?
    fun getFileSize(path: BoxPath): Int?

    // Service
    fun populate(path: Path, internalPath: BoxPath)
    fun materialize(internalDirPath: BoxPath, outputDirPath: Path)

    // fun search(): List<BoxPath>
}