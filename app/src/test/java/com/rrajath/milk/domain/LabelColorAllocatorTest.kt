package com.rrajath.milk.domain

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
        val allSix = listOf(0, 1, 2, 3, 4, 5)
        assertEquals(0, LabelColorAllocator.nextColorIndex(allSix)) // 6 % 6
        val sevenLive = allSix + 0
        assertEquals(1, LabelColorAllocator.nextColorIndex(sevenLive)) // 7 % 6
    }
}
