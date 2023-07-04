package org.avrong.boxfs

interface Box {
    // Both files and dirs
    fun exists(path: BoxPath): Boolean
    fun move(path: BoxPath): Boolean
    fun delete(path: BoxPath): Boolean

    // Dirs only
    fun createDirectory(path: BoxPath): Boolean
    fun listDirectory(path: BoxPath): List<BoxPath>
    fun copyDirectory(pathFrom: BoxPath, pathTo: BoxPath): Boolean
    fun walkDirectory()

    // Files only
    fun createFile(path: BoxPath): Boolean
    fun writeFile(path: BoxPath): Boolean
    fun appendFile(path: BoxPath): Boolean
    fun copyFile(pathFrom: BoxPath, pathTo: BoxPath): Boolean
    fun getFileSize(path: BoxPath)

    // Additional
    fun search(): List<BoxPath>
}