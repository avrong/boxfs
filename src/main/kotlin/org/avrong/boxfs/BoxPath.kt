package org.avrong.boxfs

import java.nio.file.Path

class BoxPath(val pathList: List<String>) {
    // TODO: There is probably need to forbid directory names . and .. as they are used in regular filesystems to
    //  navigate dir tree

    val depth: Int
        get() = pathList.size

    constructor(path: String) : this(toPathList(path))

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

    fun removePrefix(prefix: BoxPath): BoxPath {
        val path = pathList.withIndex().dropWhile { (i, v) -> v == prefix.pathList.getOrNull(i) }.map { it.value }
        return BoxPath(path)
    }

    fun isEmpty(): Boolean = pathList.isEmpty()

    fun toPath(): Path = Path.of(pathList.joinToString("/"))
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

    companion object {
        private fun toPathList(path: String): List<String> {
            return path.replace("\\", "/")
                .trimStart('/') // Workaround so .split() doesn't create [""] on ""
                .split('/')
                .dropLastWhile { it == "" }
        }
    }
}
