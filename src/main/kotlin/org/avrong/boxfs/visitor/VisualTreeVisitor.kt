package org.avrong.boxfs.visitor

import org.avrong.boxfs.BoxPath

internal class VisualTreeVisitor : BoxFsVisitor {
    private val stringBuilder: StringBuilder = StringBuilder()
    private var level: Int = 0

    val visualTree: String
        get() = stringBuilder.toString()

    override fun preVisitDirectory(dir: BoxPath): BoxFsVisitResult {
        writeEntry(dir, true)
        level += 1
        return BoxFsVisitResult.CONTINUE
    }

    override fun visitFile(file: BoxPath): BoxFsVisitResult {
        writeEntry(file, false)
        return BoxFsVisitResult.CONTINUE
    }

    override fun postVisitDirectory(dir: BoxPath): BoxFsVisitResult {
        level -= 1
        return BoxFsVisitResult.CONTINUE
    }

    private fun writeEntry(path: BoxPath, isDirectory: Boolean) {
        val prefix = "  ".repeat(level)
        val type = if (isDirectory) "/" else ""
        val name = path.last()

        stringBuilder.appendLine("$prefix$name$type")
    }
}