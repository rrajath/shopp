package com.rrajath.shopp.ui.components

/**
 * Turns a stream of IME visibility readings into a "keyboard was dismissed"
 * signal. Only a visible to hidden transition counts, so the hidden reading
 * that arrives before the keyboard first opens (the field has just requested
 * focus) is ignored, as is a session where the keyboard never shows (for
 * example with a hardware keyboard).
 */
internal class ImeDismissDetector {
    private var seenVisible = false

    /** Returns true when [visible] is false after a previous true reading. */
    fun onImeVisibility(visible: Boolean): Boolean {
        if (visible) {
            seenVisible = true
            return false
        }
        val dismissed = seenVisible
        seenVisible = false
        return dismissed
    }
}
