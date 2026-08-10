package com.rrajath.milk.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rrajath.milk.AppContainer
import com.rrajath.milk.data.db.ItemEntity
import com.rrajath.milk.data.db.LabelEntity
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

class ShoppViewModel(private val container: AppContainer) : ViewModel() {

    val sections: StateFlow<List<ListSection>> =
        combine(
            container.itemRepository.observeActiveItems(),
            container.labelRepository.observeLabels(),
        ) { items, labels -> buildSections(items, labels) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var undoJob: Job? = null
    private val _undo = MutableStateFlow<UndoState?>(null)
    val undo: StateFlow<UndoState?> = _undo

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
