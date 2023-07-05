package org.avrong.boxfs

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class BoxPathTest {
    @Test
    fun testPathList() {
        assertEquals(listOf(), BoxPath("").pathList)
        assertEquals(listOf(), BoxPath("/").pathList)
        assertEquals(listOf("hello", "world"), BoxPath("/hello/world").pathList)
        assertEquals(listOf("hello", "world"), BoxPath("hello/world/").pathList)
        assertEquals(listOf("hello", "world"), BoxPath("hello/world").pathList)
        assertThrows<IllegalArgumentException> { BoxPath("/hello/../") }
        assertThrows<IllegalArgumentException> { BoxPath("./hello/") }
    }

    @Test
    fun testCreateFromWindowsPath() {
        assertEquals(listOf(), BoxPath("\\").pathList)
        assertEquals(listOf("hello", "world"), BoxPath("hello\\world\\").pathList)
    }

    @Test
    fun testWith() {
        assertEquals(listOf("hello"), BoxPath("").with("hello").pathList)
        assertThrows<IllegalArgumentException> { BoxPath("").with("/hello") }
    }

    @Test
    fun testWithPath() {
        assertEquals(listOf("hello", "world"), BoxPath("hello/").withPath("world/").pathList)
        assertEquals(listOf("hello", "world"), BoxPath("hello").withPath("/world").pathList)
    }

    @Test
    fun testRemovePrefix() {
        assertEquals(listOf("world"), BoxPath("/hello/world").removePrefix(BoxPath("/hello")).pathList)
        assertEquals(listOf("hello", "world"), BoxPath("/hello/world").removePrefix(BoxPath("")).pathList)
        assertEquals(listOf(), BoxPath("").removePrefix(BoxPath("/hello/world")).pathList)
        assertEquals(
            listOf("another"),
            BoxPath("/hello/world/another").removePrefix(BoxPath("/hello/world/something")).pathList
        )
    }
}