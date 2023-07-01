package org.avrong.boxfs

import org.avrong.boxfs.container.Container
import org.avrong.boxfs.container.Space
import java.nio.file.Path
import kotlin.io.path.createFile

class BoxFs private constructor(
    val path: Path,
    val container: Container
) : AutoCloseable {
    companion object {
        fun initialize(path: Path): BoxFs {
            path.createFile()

            val space = Space.fromPath(path)
            val container = Container.fromSpace(space)

            return BoxFs(path, container)
        }

        fun open(path: Path): BoxFs {
            val space = Space.fromPath(path)
            val container = Container.fromSpace(space)
            return BoxFs(path, container)
        }
    }

    override fun close() {
        container.close()
    }
}
