package org.avrong.boxfs.visitor

import org.avrong.boxfs.BoxFs
import org.avrong.boxfs.BoxPath

internal class BasicCompactionVisitor(private val currentBoxFs: BoxFs, private val newBoxFs: BoxFs) : AbstractBoxFsVisitor() {
    override fun preVisitDirectory(dir: BoxPath): BoxFsVisitResult {
        newBoxFs.createDirectory(dir)
        return BoxFsVisitResult.CONTINUE
    }

    override fun visitFile(file: BoxPath): BoxFsVisitResult {
        newBoxFs.createFile(file)

        val content = currentBoxFs.readFile(file)!!
        newBoxFs.writeFile(file, content)

        return BoxFsVisitResult.CONTINUE
    }
}