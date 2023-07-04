package org.avrong.boxfs.visitor

enum class BoxFsVisitResult{
    CONTINUE, TERMINATE, SKIP_SUBTREE, SKIP_SIBLINGS;
}