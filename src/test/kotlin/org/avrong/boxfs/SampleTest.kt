package org.avrong.boxfs

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SampleTest {
    @Test
    fun testSomething() {
        assertEquals(2, 1 + 1)
    }

    @Test
    fun testFails() {
        assertNotEquals(true, false)
    }
}