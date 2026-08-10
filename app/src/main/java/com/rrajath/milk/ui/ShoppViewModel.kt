package com.rrajath.milk.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rrajath.milk.AppContainer
import com.rrajath.milk.data.db.ItemEntity
import com.rrajath.milk.data.db.LabelEntity
import com.rrajath.milk.domain.LabelRef
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val UNDO_WINDOW_MS = 4_000L

data class ListSection(
    val labelId: String?, // null = Inbox
    val name: String,
    val colorIndex: Int?, // null = Inbox tint, not a palette entry
    val items: List<ItemEntity>,
)

data class UndoState(val itemId: String, val title: String)

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
// instead of reading ViewModel state snapshots outside of collectAsState.
fun quickAddSuggestions(draft: String, labels: List<LabelEntity>): List<LabelEntity> {
    val query = TRAILING_TOKEN_REGEX.find(draft)?.groupValues?.get(1) ?: return emptyList()
    val folded = query.lowercase()
    return labels.filter { it.nameFolded.startsWith(folded) }
}

class ShoppViewModel(private val container: AppContainer) : ViewModel() {

    val sections: StateFlow<List<ListSection>> =
        combine(
            container.itemRepository.observeActiveItems(),
            container.labelRepository.observeLabels(),
        ) { items, labels -> buildSections(items, labels) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val labels: StateFlow<List<LabelEntity>> =
        container.labelRepository.observeLabels()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var undoJob: Job? = null
    private val _undo = MutableStateFlow<UndoState?>(null)
    val undo: StateFlow<UndoState?> = _undo

    private val _quickAdd = MutableStateFlow(QuickAddState())
    val quickAdd: StateFlow<QuickAddState> = _quickAdd

    fun completeItem(item: ItemEntity) {
        undoJob?.cancel()
        viewModelScope.launch {
            container.completeItem(item.id)
            _undo.value = UndoState(item.id, item.title)
            undoJob = launch {
                delay(UNDO_WINDOW_MS)
                _undo.value = null
            }
        }
    }

    fun undoLastComplete() {
        val state = _undo.value ?: return
        undoJob?.cancel()
        _undo.value = null
        viewModelScope.launch { container.undoComplete(state.itemId) }
    }

    fun editTitle(itemId: String, newTitle: String) {
        viewModelScope.launch { container.editTitle(itemId, newTitle) }
    }

    fun openQuickAdd() {
        _quickAdd.value = QuickAddState(open = true)
    }

    // Sticky resets to Inbox on close (TDD §5.2 -- lives only in this
    // ephemeral state, never persisted).
    fun closeQuickAdd() {
        _quickAdd.value = QuickAddState(open = false)
    }

    fun updateQuickAddDraft(text: String) {
        _quickAdd.value = _quickAdd.value.copy(draft = text)
    }

    fun selectStickyChip(labelId: String?) {
        _quickAdd.value = _quickAdd.value.copy(stickyLabelId = labelId)
    }

    fun acceptSuggestion(labelName: String) {
        val next = _quickAdd.value.draft.replace(TRAILING_TOKEN_REGEX, "@$labelName ")
        _quickAdd.value = _quickAdd.value.copy(draft = next)
    }

    fun submitQuickAdd() {
        val state = _quickAdd.value
        if (state.draft.isBlank()) return
        viewModelScope.launch {
            val sticky = state.stickyLabelId?.let { LabelRef.Id(it) } ?: LabelRef.None
            val result = container.captureItems(state.draft, sticky)
            val newStickyId = (result.newSticky as? LabelRef.Id)?.labelId
            val newEntries = result.items.map { SessionAddEntry(it.id, it.title, it.labelId) }
            _quickAdd.value = _quickAdd.value.copy(
                draft = "",
                stickyLabelId = newStickyId,
                sessionAdds = (state.sessionAdds + newEntries).takeLast(3),
            )
        }
    }

    // Inbox is always pinned first, even when empty (PRD §7.3) -- a
    // deliberate deviation from the prototype's mockup logic, which only
    // renders a section once it has at least one item; every other section
    // requires >=1 active item, matching both the prototype and the PRD.
    private fun buildSections(items: List<ItemEntity>, labels: List<LabelEntity>): List<ListSection> {
        val byLabel = items.groupBy { it.labelId }
        val sections = mutableListOf(ListSection(null, "Inbox", null, byLabel[null].orEmpty()))
        for (label in labels) {
            val labelItems = byLabel[label.id]
            if (!labelItems.isNullOrEmpty()) {
                sections += ListSection(label.id, label.name, label.colorIndex, labelItems)
            }
        }
        return sections
    }
}

class ShoppViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ShoppViewModel(container) as T
    }
}
