package org.avrong.boxfs.visitor

import org.avrong.boxfs.BoxFs
import org.avrong.boxfs.BoxPath

internal class RecursiveCopyVisitor(private val boxFs: BoxFs, private val copyFromDir: BoxPath, private val copyToDir: BoxPath) : AbstractBoxFsVisitor() {
    override fun preVisitDirectory(dir: BoxPath): BoxFsVisitResult {
        val dirPath = convertPath(dir)
        boxFs.createDirectory(dirPath)
        return super.preVisitDirectory(dirPath)
    }

    override fun visitFile(file: BoxPath): BoxFsVisitResult {
        val filePath = convertPath(file)

        boxFs.createFile(filePath)
        val content = boxFs.readFile(filePath)!!
        boxFs.writeFile(filePath, content)

        return super.visitFile(file)
    }

    private fun convertPath(path: BoxPath): BoxPath {
        return copyToDir.withPath(path.removePrefix(copyFromDir))
    }
}