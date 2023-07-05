package org.avrong.boxfs.visitor

import org.avrong.boxfs.BoxPath

abstract class AbstractBoxFsVisitor : BoxFsVisitor {
    override fun preVisitDirectory(dir: BoxPath): BoxFsVisitResult {
        return BoxFsVisitResult.CONTINUE
    }
    override fun visitFile(file: BoxPath): BoxFsVisitResult {
        return BoxFsVisitResult.CONTINUE
    }

    override fun postVisitDirectory(dir: BoxPath): BoxFsVisitResult {
        return BoxFsVisitResult.CONTINUE
    }

}