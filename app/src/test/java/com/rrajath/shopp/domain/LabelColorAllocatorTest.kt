package com.rrajath.shopp.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class LabelColorAllocatorTest {

    @Test
    fun `first label gets index 0`() {
        assertEquals(0, LabelColorAllocator.nextColorIndex(emptyList()))
    }

    @Test
    fun `picks lowest unused index`() {
        assertEquals(1, LabelColorAllocator.nextColorIndex(listOf(0)))
        assertEquals(2, LabelColorAllocator.nextColorIndex(listOf(0, 1)))
        assertEquals(0, LabelColorAllocator.nextColorIndex(listOf(1, 2)))
    }

    @Test
    fun `cycles by count mod palette size once exhausted`() {
        val allOfPalette = (0 until LabelColorAllocator.PALETTE_SIZE).toList()
        assertEquals(0, LabelColorAllocator.nextColorIndex(allOfPalette)) // size % size
        val oneMoreThanPalette = allOfPalette + 0
        assertEquals(1, LabelColorAllocator.nextColorIndex(oneMoreThanPalette)) // (size+1) % size
    }
}
