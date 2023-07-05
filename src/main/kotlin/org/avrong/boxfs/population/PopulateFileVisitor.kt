package org.avrong.boxfs.population

import org.avrong.boxfs.BoxFs
import org.avrong.boxfs.BoxPath
import org.avrong.boxfs.toBoxPath
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.pathString
import kotlin.io.path.readBytes

internal class PopulateFileVisitor(private val boxFs: BoxFs, private val prefix: Path, private val internalPath: BoxPath) : SimpleFileVisitor<Path>() {
    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
        val convertedPath = convertPath(file)

        boxFs.createFile(convertedPath)

        try {
            val bytes = file.readBytes()
            boxFs.writeFile(convertedPath, bytes)
        } catch (e: IOException) {
            println("Can't populate file ${file.pathString}")
            println(e)
        }

        return super.visitFile(file, attrs)
    }

    override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
        val convertedPath = convertPath(dir)
        if (!convertedPath.isEmpty()) {
            boxFs.createDirectory(convertedPath)
        }

        return super.preVisitDirectory(dir, attrs)
    }

    private fun convertPath(path: Path): BoxPath {
        val withoutPrefix = path.toString().removePrefix(prefix.toString()).toBoxPath()
        return internalPath.withPath(withoutPrefix)
    }
}
