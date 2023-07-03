package org.avrong.boxfs

class BoxPath(val pathList: List<String>) {
    val depth: Int
        get() = pathList.size

    constructor(path: String) : this(path.trimStart('/').split('/').dropLastWhile { it == "" }) // Workaround so .split() doesn't create [""] on ""

    fun last(): String {
        return pathList.lastOrNull() ?: throw IndexOutOfBoundsException("Path has no elements")
    }

    fun withoutLast(): BoxPath = BoxPath(pathList.subList(0, pathList.size - 1))

    fun with(dirName: String): BoxPath {
        assert(!dirName.contains("/")) { "Dir name cannot contain slashes"}
        return BoxPath(pathList + listOf(dirName))
    }

    override fun toString(): String = pathList.joinToString("/", "/")

    override fun equals(other: Any?): Boolean {
        if (other is BoxPath) {
            return pathList == other.pathList
        }

        return false
    }

    override fun hashCode(): Int {
        return pathList.hashCode()
    }
}