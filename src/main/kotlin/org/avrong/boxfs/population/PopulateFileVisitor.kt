package org.avrong.boxfs.population

import org.avrong.boxfs.BoxFs
import org.avrong.boxfs.BoxPath
import org.avrong.boxfs.toBoxPath
import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.readBytes

class PopulateFileVisitor(val boxFs: BoxFs, val prefix: Path, val internalPath: BoxPath) : SimpleFileVisitor<Path>() {
    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
        val convertedPath = convertPath(file)

        boxFs.createFile(convertedPath)
        boxFs.writeFile(convertedPath, file.readBytes())

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
