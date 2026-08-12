package com.rrajath.shopp.domain

import java.text.Normalizer

// Opaque label reference threaded through a parse: either nothing (Inbox),
// an already-resolved id (the sticky value the caller passed in), or a raw
// token extracted during this parse that the caller still has to
// resolve/create (TDD §4.2 — the parser stays pure and I/O-free).
sealed class LabelRef {
    data object None : LabelRef()
    data class Id(val labelId: String) : LabelRef()
    data class Token(val text: String) : LabelRef()
}

data class ParsedLine(val title: String, val label: LabelRef)

data class ParseCaptureResult(val lines: List<ParsedLine>, val sticky: LabelRef)

private val TRAILING_PUNCTUATION = setOf('.', ',', ';', ':', '!', '?')

// '@' at the start of a line or preceded by whitespace, followed by one or
// more non-whitespace, non-'@' characters.
private val TOKEN_REGEX = Regex("(?:^|\\s)@([^\\s@]+)")

/**
 * TDD §4.1. Pure and I/O-free: label tokens are returned as raw text
 * ([LabelRef.Token]) for the caller to resolve/create; only the initial
 * sticky value may already be a resolved [LabelRef.Id].
 */
fun parseCapture(input: String, initialSticky: LabelRef): ParseCaptureResult {
    val normalized = Normalizer.normalize(
        input.replace("\r\n", "\n").replace('\r', '\n'),
        Normalizer.Form.NFC,
    )

    var sticky = initialSticky
    val lines = mutableListOf<ParsedLine>()

    for (rawLine in normalized.split('\n')) {
        val trimmed = rawLine.trim { it.isWhitespace() }
        if (trimmed.isEmpty()) continue // blank line: no item, sticky unchanged

        val token = findLabelToken(trimmed)
        val title = if (token != null) {
            sticky = LabelRef.Token(token.text)
            (trimmed.substring(0, token.atIndex) + trimmed.substring(token.removalEnd))
                .replace(Regex(" {2,}"), " ")
                .trim { it.isWhitespace() }
        } else {
            trimmed
        }

        if (title.isEmpty()) continue // bare token line: sticky updated, no item
        lines.add(ParsedLine(title = title, label = sticky))
    }

    return ParseCaptureResult(lines = lines, sticky = sticky)
}

private class TokenMatch(val atIndex: Int, val removalEnd: Int, val text: String)

private fun findLabelToken(line: String): TokenMatch? {
    val match = TOKEN_REGEX.find(line) ?: return null
    val body = match.groups[1]!!
    val atIndex = body.range.first - 1 // '@' always sits directly before the body
    val removalEnd = body.range.last + 1 // exclusive; includes trailing punctuation

    var text = body.value
    while (text.isNotEmpty() && text.last() in TRAILING_PUNCTUATION) {
        text = text.dropLast(1)
    }
    if (text.isEmpty()) return null // '@' followed only by punctuation isn't a real token

    return TokenMatch(atIndex = atIndex, removalEnd = removalEnd, text = text)
}
