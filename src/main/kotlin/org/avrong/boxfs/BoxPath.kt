package org.avrong.boxfs

class BoxPath(val pathList: List<String>) {
    // TODO: There is probably need to forbid directory names . and .. as they are used in regular filesystems to
    //  navigate dir tree

    val depth: Int
        get() = pathList.size

    constructor(path: String) : this(path.trimStart('/').split('/').dropLastWhile { it == "" }) // Workaround so .split() doesn't create [""] on ""

    fun last(): String {
        return pathList.lastOrNull() ?: throw IndexOutOfBoundsException("Path has no elements")
    }

    fun withoutLast(): BoxPath {
        if (pathList.isEmpty()) throw IndexOutOfBoundsException("Path has no elements")

        return BoxPath(pathList.subList(0, pathList.size - 1))
    }

    fun with(dirName: String): BoxPath {
        assert(!dirName.contains("/")) { "Dir name cannot contain slashes"}
        return BoxPath(pathList + listOf(dirName))
    }

    fun withPath(path: String) = withPath(BoxPath(path))

    fun withPath(path: BoxPath): BoxPath {
        return BoxPath(pathList + path.pathList)
    }

    fun isEmpty(): Boolean = pathList.isEmpty()

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
