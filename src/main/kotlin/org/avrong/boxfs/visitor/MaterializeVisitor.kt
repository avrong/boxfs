package org.avrong.boxfs.visitor

import org.avrong.boxfs.BoxFs
import org.avrong.boxfs.BoxPath
import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.writeBytes

internal class MaterializeVisitor(private val boxFs: BoxFs, private val targetDir: Path) : AbstractBoxFsVisitor() {
    override fun preVisitDirectory(dir: BoxPath): BoxFsVisitResult {
        targetDir.resolve(dir.toPath()).createDirectory()
        return BoxFsVisitResult.CONTINUE
    }

    override fun visitFile(file: BoxPath): BoxFsVisitResult {
        val externalPath = targetDir.resolve(file.toPath())
        val content = boxFs.readFile(file)!!

        externalPath.createFile()
        externalPath.writeBytes(content)

        return BoxFsVisitResult.CONTINUE
    }
}