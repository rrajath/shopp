package com.rrajath.shopp.domain

import java.text.Normalizer
import java.util.Locale

// NFC + full casefold, so "@Café" and "@CAFÉ" resolve to the same label
// (SQLite's COLLATE NOCASE is ASCII-only, hence folding in code — TDD §3.1).
fun String.foldForMatching(): String =
    Normalizer.normalize(this, Normalizer.Form.NFC).lowercase(Locale.ROOT)
