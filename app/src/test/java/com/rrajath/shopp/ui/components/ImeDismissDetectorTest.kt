package com.rrajath.shopp.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class ImeDismissDetectorTest {

    private fun run(vararg readings: Boolean): List<Boolean> {
        val detector = ImeDismissDetector()
        return readings.map { detector.onImeVisibility(it) }
    }

    @Test
    fun `hidden reading before the keyboard first opens is not a dismissal`() {
        assertEquals(listOf(false), run(false))
    }

    @Test
    fun `keyboard opening then hiding is a dismissal`() {
        assertEquals(listOf(false, false, true), run(false, true, false))
    }

    @Test
    fun `keyboard already visible when editing starts then hiding is a dismissal`() {
        assertEquals(listOf(false, true), run(true, false))
    }

    @Test
    fun `keyboard that never shows never dismisses`() {
        assertEquals(listOf(false, false, false), run(false, false, false))
    }

    @Test
    fun `repeated visible readings do not dismiss`() {
        assertEquals(listOf(false, false, false), run(false, true, true))
    }

    @Test
    fun `a second hidden reading after a dismissal does not dismiss again`() {
        assertEquals(listOf(false, true, false), run(true, false, false))
    }

    @Test
    fun `reopening after a dismissal arms the next dismissal`() {
        assertEquals(listOf(false, true, false, true), run(true, false, true, false))
    }
}
