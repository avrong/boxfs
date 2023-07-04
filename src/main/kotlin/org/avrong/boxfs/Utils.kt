package org.avrong.boxfs

import java.nio.file.Path

fun String.toBoxPath() = BoxPath(this)

fun Path.toBoxPath() = BoxPath(this.toString())