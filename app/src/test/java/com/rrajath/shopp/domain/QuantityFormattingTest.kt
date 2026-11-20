package com.rrajath.shopp.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class QuantityFormattingTest {

    @Test
    fun `quantity of 1 shows the bare title`() {
        assertEquals("Milk", titleWithQuantity("Milk", 1))
    }

    @Test
    fun `quantity above 1 is shown in brackets`() {
        assertEquals("Eggs (3)", titleWithQuantity("Eggs", 3))
    }

    @Test
    fun `single duplicate hint names the item and the count on the list`() {
        assertEquals("Eggs x3 already added", duplicateHint(listOf("Eggs" to 3)))
    }

    @Test
    fun `duplicate hint with quantity 1 still shows the count`() {
        assertEquals("Milk x1 already added", duplicateHint(listOf("Milk" to 1)))
    }

    @Test
    fun `several duplicates name the first and count the rest`() {
        assertEquals(
            "Eggs x3 and 2 more already added",
            duplicateHint(listOf("Eggs" to 3, "Milk" to 1, "Bread" to 2)),
        )
    }

    @Test
    fun `no duplicates gives no hint`() {
        assertEquals(null, duplicateHint(emptyList()))
    }

    @Test
    fun `quantity is clamped to the allowed range`() {
        assertEquals(1, clampQuantity(0))
        assertEquals(MAX_QUANTITY, clampQuantity(MAX_QUANTITY + 1))
        assertEquals(5, clampQuantity(5))
    }
}
