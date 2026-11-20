package com.rrajath.shopp.ui

import com.rrajath.shopp.AppContainer
import com.rrajath.shopp.data.db.ItemEntity
import com.rrajath.shopp.data.db.LabelEntity
import com.rrajath.shopp.domain.LabelRef
import com.rrajath.shopp.domain.MIN_QUANTITY
import com.rrajath.shopp.domain.clampQuantity
import com.rrajath.shopp.domain.duplicateHint
import com.rrajath.shopp.domain.foldForMatching
import com.rrajath.shopp.domain.titleWithQuantity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SessionAddEntry(val id: String, val text: String, val labelId: String?)

// A purchase-history suggestion, carrying the label it was last bought under
// so picking it can file the new item there too.
data class TitleSuggestion(val title: String, val labelId: String?)

data class QuickAddState(
    val open: Boolean = false,
    val draft: String = "",
    val stickyLabelId: String? = null, // null = Inbox
    val sessionAdds: List<SessionAddEntry> = emptyList(),
    // Stepper value; applies to every line on submit, then resets to 1.
    val quantity: Int = MIN_QUANTITY,
    // Replaces the "Add an item" placeholder after a submit dropped
    // duplicates; cleared as soon as the user types again.
    val hint: String? = null,
    // Folded title -> label of each history suggestion picked for this
    // draft. Applied per line on submit (see CaptureItems.labelOverrides),
    // so it never touches the sticky chip.
    val suggestionLabels: Map<String, String?> = emptyMap(),
)

// A line containing only a token sets sticky and yields no item, so the
// same regex parseCapture uses to find the trailing token is reused here to
// detect "is the user mid-typing a label" (only the END of the draft is
// checked, not full caret-aware detection).
private val TRAILING_TOKEN_REGEX = Regex("@([\\p{L}\\p{N}_-]*)$")

// Pure, so the caller can compute it reactively (e.g. remember(draft, labels))
// instead of reading state snapshots outside of collectAsState.
fun quickAddSuggestions(draft: String, labels: List<LabelEntity>): List<LabelEntity> {
    val query = TRAILING_TOKEN_REGEX.find(draft)?.groupValues?.get(1) ?: return emptyList()
    val folded = query.lowercase()
    return labels.filter { it.nameFolded.startsWith(folded) }
}

private const val MAX_TITLE_SUGGESTIONS = 5

// Autocomplete from purchase history (Recently Completed), so re-buying
// something typed the same way before is one tap. Matches the line
// currently being typed (the draft may hold several pasted lines) against
// distinct past titles, most-recently-completed first. Mutually exclusive
// with quickAddSuggestions -- skipped while mid-typing a `@label` token,
// since that popover takes the same slot in QuickAddOverlay.
fun itemTitleSuggestions(draft: String, completedItems: List<ItemEntity>): List<TitleSuggestion> {
    if (TRAILING_TOKEN_REGEX.containsMatchIn(draft)) return emptyList()
    val currentLine = draft.substringAfterLast('\n').trim()
    if (currentLine.isEmpty()) return emptyList()

    val folded = currentLine.foldForMatching()
    val seen = HashSet<String>()
    val results = mutableListOf<TitleSuggestion>()
    for (item in completedItems) {
        val itemFolded = item.title.foldForMatching()
        if (itemFolded == folded || !itemFolded.startsWith(folded)) continue
        if (!seen.add(itemFolded)) continue
        // completedItems is most-recent first, so this is the label it was
        // last bought under. Label delete/merge reassign completed items
        // too, so this id always points at a live label (or Inbox).
        results += TitleSuggestion(item.title, item.labelId)
        if (results.size >= MAX_TITLE_SUGGESTIONS) break
    }
    return results
}

/**
 * Owns Quick Add's ephemeral state and the submit/suggestion logic. Shared
 * by [ShoppViewModel] (in-app FAB) and `CaptureViewModel` (Quick Settings
 * tile's [com.rrajath.shopp.capture.CaptureActivity]) so "the same component"
 * is literally true rather than something that needs cross-language
 * fixture verification.
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

    // Sticky resets to Inbox on close (it lives only in this
    // ephemeral state, never persisted).
    fun close() {
        _state.value = QuickAddState(open = false)
    }

    fun updateDraft(text: String) {
        val current = _state.value
        _state.value = current.copy(draft = text, hint = if (text.isEmpty()) current.hint else null)
    }

    fun setQuantity(quantity: Int) {
        _state.value = _state.value.copy(quantity = clampQuantity(quantity))
    }

    fun selectStickyChip(labelId: String?) {
        _state.value = _state.value.copy(stickyLabelId = labelId)
    }

    fun acceptSuggestion(labelName: String) {
        val next = _state.value.draft.replace(TRAILING_TOKEN_REGEX, "@$labelName ")
        _state.value = _state.value.copy(draft = next)
    }

    // Replaces just the line currently being typed (the draft may hold
    // earlier pasted lines already) with the picked title, and remembers the
    // suggestion's label for that title only.
    fun acceptTitleSuggestion(suggestion: TitleSuggestion) {
        val current = _state.value
        val lastNewline = current.draft.lastIndexOf('\n')
        val next = if (lastNewline == -1) {
            suggestion.title
        } else {
            current.draft.substring(0, lastNewline + 1) + suggestion.title
        }
        _state.value = current.copy(
            draft = next,
            hint = null,
            suggestionLabels = current.suggestionLabels +
                (suggestion.title.foldForMatching() to suggestion.labelId),
        )
    }

    fun submit() {
        val current = _state.value
        if (current.draft.isBlank()) return
        scope.launch {
            val sticky = current.stickyLabelId?.let { LabelRef.Id(it) } ?: LabelRef.None
            val result = container.captureItems(
                current.draft,
                sticky,
                quantity = current.quantity,
                labelOverrides = current.suggestionLabels,
            )
            val newStickyId = (result.newSticky as? LabelRef.Id)?.labelId
            val newEntries = result.items.map {
                SessionAddEntry(it.id, titleWithQuantity(it.title, it.quantity), it.labelId)
            }
            val hint = duplicateHint(result.duplicates.map { it.title to it.quantity })
            // Stay open when something was rejected, even if the setting
            // says close: otherwise the "already added" hint is never seen.
            if (hint != null || container.preferencesRepository.keepQuickAddOpen.first()) {
                _state.value = _state.value.copy(
                    draft = "",
                    stickyLabelId = newStickyId,
                    sessionAdds = (current.sessionAdds + newEntries).takeLast(3),
                    quantity = MIN_QUANTITY,
                    hint = hint,
                    suggestionLabels = emptyMap(),
                )
            } else {
                _state.value = QuickAddState(open = false)
            }
        }
    }
}
