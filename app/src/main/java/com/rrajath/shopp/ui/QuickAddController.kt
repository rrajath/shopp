package com.rrajath.shopp.ui

import com.rrajath.shopp.AppContainer
import com.rrajath.shopp.data.db.ItemEntity
import com.rrajath.shopp.data.db.LabelEntity
import com.rrajath.shopp.domain.LabelRef
import com.rrajath.shopp.domain.foldForMatching
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SessionAddEntry(val id: String, val text: String, val labelId: String?)

data class QuickAddState(
    val open: Boolean = false,
    val draft: String = "",
    val stickyLabelId: String? = null, // null = Inbox
    val sessionAdds: List<SessionAddEntry> = emptyList(),
)

// A line containing only a token sets sticky and yields no item, so the
// same regex parseCapture uses to find the trailing token is reused here to
// detect "is the user mid-typing a label" (prototype's own approach: only
// the END of the draft is checked, not full caret-aware detection).
private val TRAILING_TOKEN_REGEX = Regex("@([\\p{L}\\p{N}_-]*)$")

// Pure, so the caller can compute it reactively (e.g. remember(draft, labels))
// instead of reading state snapshots outside of collectAsState.
fun quickAddSuggestions(draft: String, labels: List<LabelEntity>): List<LabelEntity> {
    val query = TRAILING_TOKEN_REGEX.find(draft)?.groupValues?.get(1) ?: return emptyList()
    val folded = query.lowercase()
    return labels.filter { it.nameFolded.startsWith(folded) }
}

private const val MIN_TITLE_SUGGESTION_LENGTH = 2
private const val MAX_TITLE_SUGGESTIONS = 5

// Autocomplete from purchase history (Recently Completed), so re-buying
// something typed the same way before is one tap. Matches the line
// currently being typed (the draft may hold several pasted lines) against
// distinct past titles, most-recently-completed first. Mutually exclusive
// with quickAddSuggestions -- skipped while mid-typing a `@label` token,
// since that popover takes the same slot in QuickAddOverlay.
fun itemTitleSuggestions(draft: String, completedItems: List<ItemEntity>): List<String> {
    if (TRAILING_TOKEN_REGEX.containsMatchIn(draft)) return emptyList()
    val currentLine = draft.substringAfterLast('\n').trim()
    if (currentLine.length < MIN_TITLE_SUGGESTION_LENGTH) return emptyList()

    val folded = currentLine.foldForMatching()
    val seen = HashSet<String>()
    val results = mutableListOf<String>()
    for (item in completedItems) {
        val itemFolded = item.title.foldForMatching()
        if (itemFolded == folded || !itemFolded.startsWith(folded)) continue
        if (!seen.add(itemFolded)) continue
        results += item.title
        if (results.size >= MAX_TITLE_SUGGESTIONS) break
    }
    return results
}

/**
 * Owns Quick Add's ephemeral state and the submit/suggestion logic. Shared
 * by [ShoppViewModel] (in-app FAB) and `CaptureViewModel` (Quick Settings
 * tile's [com.rrajath.shopp.capture.CaptureActivity]) so "the same component"
 * (TDD/PRD) is literally true rather than something that needs cross-
 * language fixture verification -- see TDD §1.2.
 */
class QuickAddController(
    private val scope: CoroutineScope,
    private val container: AppContainer,
    initiallyOpen: Boolean = false,
) {
    private val _state = MutableStateFlow(QuickAddState(open = initiallyOpen))
    val state: StateFlow<QuickAddState> = _state

    fun open() {
        _state.value = QuickAddState(open = true)
    }

    // Sticky resets to Inbox on close (TDD §5.2 -- lives only in this
    // ephemeral state, never persisted).
    fun close() {
        _state.value = QuickAddState(open = false)
    }

    fun updateDraft(text: String) {
        _state.value = _state.value.copy(draft = text)
    }

    fun selectStickyChip(labelId: String?) {
        _state.value = _state.value.copy(stickyLabelId = labelId)
    }

    fun acceptSuggestion(labelName: String) {
        val next = _state.value.draft.replace(TRAILING_TOKEN_REGEX, "@$labelName ")
        _state.value = _state.value.copy(draft = next)
    }

    // Replaces just the line currently being typed (the draft may hold
    // earlier pasted lines already) with the picked title.
    fun acceptTitleSuggestion(title: String) {
        val current = _state.value.draft
        val lastNewline = current.lastIndexOf('\n')
        val next = if (lastNewline == -1) title else current.substring(0, lastNewline + 1) + title
        _state.value = _state.value.copy(draft = next)
    }

    fun submit() {
        val current = _state.value
        if (current.draft.isBlank()) return
        scope.launch {
            val sticky = current.stickyLabelId?.let { LabelRef.Id(it) } ?: LabelRef.None
            val result = container.captureItems(current.draft, sticky)
            val newStickyId = (result.newSticky as? LabelRef.Id)?.labelId
            val newEntries = result.items.map { SessionAddEntry(it.id, it.title, it.labelId) }
            if (container.preferencesRepository.keepQuickAddOpen.first()) {
                _state.value = _state.value.copy(
                    draft = "",
                    stickyLabelId = newStickyId,
                    sessionAdds = (current.sessionAdds + newEntries).takeLast(3),
                )
            } else {
                _state.value = QuickAddState(open = false)
            }
        }
    }
}
