package de.kimmlingen.util

import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class CustomFunctionsTest {
    @Test
    fun testCheckNotEmpty() {
        assertEquals("hello", checkNotEmpty("hello", { -> "error" }))

        assertThrows<IllegalStateException> { checkNotEmpty("", { -> "error" }) }
    }

    @Test
    fun testCheckNotBlank() {
        assertEquals("hello", checkNotBlank("hello", { -> "error" }))

        assertThrows<IllegalStateException> { checkNotBlank("  ", { -> "error" }) }
    }

    @Test
    fun testNullToEmpty() {
        assertEquals("", nullToEmpty(null))

        assertEquals("hello", nullToEmpty("hello"))
    }
}
