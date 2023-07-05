package org.avrong.boxfs.visitor

import org.avrong.boxfs.BoxPath


interface BoxFsVisitor {
    fun preVisitDirectory(dir: BoxPath): BoxFsVisitResult

    fun visitFile(file: BoxPath): BoxFsVisitResult

    fun postVisitDirectory(dir: BoxPath): BoxFsVisitResult
}