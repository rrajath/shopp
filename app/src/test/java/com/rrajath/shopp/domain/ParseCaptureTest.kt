package com.rrajath.shopp.domain

import org.junit.Assert.assertEquals
import org.junit.Test

// Golden fixtures ported from TDD §9.4 packages/fixtures/parser.json — this
// is the contract the parser must satisfy exactly. `label(...)` folds a raw
// Token the same way label resolution would (TDD §4.2's resolveLabel), since
// the fixtures express expected labels in their resolved/folded form.
class ParseCaptureTest {

    private fun label(text: String?): String? = text?.foldForMatching()

    private fun LabelRef.name(): String? = when (this) {
        is LabelRef.None -> null
        is LabelRef.Id -> labelId
        is LabelRef.Token -> label(text)
    }

    @Test
    fun `multiline splits`() {
        val result = parseCapture("carrots\ntomatoes\nonions", LabelRef.None)
        assertEquals(listOf("carrots", "tomatoes", "onions"), result.lines.map { it.title })
        assertEquals(listOf(null, null, null), result.lines.map { it.label.name() })
        assertEquals(null, result.sticky.name())
    }

    @Test
    fun `commas are not separators`() {
        val result = parseCapture("carrots, 2lb organic bag", LabelRef.None)
        assertEquals(listOf("carrots, 2lb organic bag"), result.lines.map { it.title })
        assertEquals(listOf(null), result.lines.map { it.label.name() })
        assertEquals(null, result.sticky.name())
    }

    @Test
    fun `token tags only its own line`() {
        val result = parseCapture("milk @costco\neggs", LabelRef.None)
        assertEquals(listOf("milk", "eggs"), result.lines.map { it.title })
        assertEquals(listOf("costco", "costco"), result.lines.map { it.label.name() })
        assertEquals("costco", result.sticky.name())
    }

    @Test
    fun `second token stays literal`() {
        val result = parseCapture("milk @costco @qfc", LabelRef.None)
        assertEquals(listOf("milk @qfc"), result.lines.map { it.title })
        assertEquals(listOf("costco"), result.lines.map { it.label.name() })
        assertEquals("costco", result.sticky.name())
    }

    @Test
    fun `email is not a token`() {
        val result = parseCapture("order from shop@costco.com", LabelRef.None)
        assertEquals(listOf("order from shop@costco.com"), result.lines.map { it.title })
        assertEquals(listOf(null), result.lines.map { it.label.name() })
        assertEquals(null, result.sticky.name())
    }

    @Test
    fun `trailing period stripped`() {
        val result = parseCapture("milk @costco.", LabelRef.None)
        assertEquals(listOf("milk"), result.lines.map { it.title })
        assertEquals(listOf("costco"), result.lines.map { it.label.name() })
        assertEquals("costco", result.sticky.name())
    }

    @Test
    fun `bare token line sets sticky`() {
        val result = parseCapture("@costco\nmilk\neggs", LabelRef.None)
        assertEquals(listOf("milk", "eggs"), result.lines.map { it.title })
        assertEquals(listOf("costco", "costco"), result.lines.map { it.label.name() })
        assertEquals("costco", result.sticky.name())
    }

    @Test
    fun `unicode case folding`() {
        val result = parseCapture("pastries @CAFÉ", LabelRef.None)
        assertEquals(listOf("pastries"), result.lines.map { it.title })
        assertEquals(listOf("café"), result.lines.map { it.label.name() })
        assertEquals("café", result.sticky.name())
    }

    // --- Supplementary cases for branch coverage beyond the 8 fixtures ---

    @Test
    fun `CRLF and lone CR are normalised to LF`() {
        val result = parseCapture("milk\r\neggs\rbread", LabelRef.None)
        assertEquals(listOf("milk", "eggs", "bread"), result.lines.map { it.title })
    }

    @Test
    fun `blank lines are skipped without affecting sticky`() {
        val result = parseCapture("milk @costco\n\n\neggs", LabelRef.None)
        assertEquals(listOf("milk", "eggs"), result.lines.map { it.title })
        assertEquals("costco", result.sticky.name())
    }

    @Test
    fun `multiple trailing punctuation characters are all stripped`() {
        val result = parseCapture("milk @costco?!", LabelRef.None)
        assertEquals(listOf("milk"), result.lines.map { it.title })
        assertEquals(listOf("costco"), result.lines.map { it.label.name() })
    }

    @Test
    fun `interior punctuation survives, only trailing is shed`() {
        val result = parseCapture("milk @trader-joes.local", LabelRef.None)
        assertEquals(listOf("milk"), result.lines.map { it.title })
        assertEquals(listOf("trader-joes.local"), result.lines.map { it.label.name() })
    }

    @Test
    fun `at-only token is not a real token`() {
        val result = parseCapture("milk @.", LabelRef.None)
        assertEquals(listOf("milk @."), result.lines.map { it.title })
        assertEquals(listOf(null), result.lines.map { it.label.name() })
    }

    @Test
    fun `submit with no non-empty lines is a no-op`() {
        val result = parseCapture("   \n\n  ", LabelRef.None)
        assertEquals(emptyList<String>(), result.lines.map { it.title })
        assertEquals(null, result.sticky.name())
    }

    @Test
    fun `an already-resolved sticky id passes through untouched lines`() {
        val result = parseCapture("eggs\nbread", LabelRef.Id("label-123"))
        assertEquals(listOf("label-123", "label-123"), result.lines.map { it.label.name() })
        assertEquals("label-123", result.sticky.name())
    }

    @Test
    fun `a token later in the submit overrides a resolved initial sticky`() {
        val result = parseCapture("eggs\nmilk @costco", LabelRef.Id("label-123"))
        assertEquals(listOf("label-123", "costco"), result.lines.map { it.label.name() })
        assertEquals("costco", result.sticky.name())
    }
}
