package com.rrajath.shopp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rrajath.shopp.AppContainer
import com.rrajath.shopp.data.db.ItemEntity
import com.rrajath.shopp.data.db.LabelEntity
import com.rrajath.shopp.ui.theme.ThemeMode
import com.rrajath.shopp.usecases.RenameLabel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val UNDO_WINDOW_MS = 4_000L

enum class Screen { LIST, RECENTLY_COMPLETED, LABELS, SETTINGS }

data class ListSection(
    val labelId: String?, // null = Inbox (or "All items" when ungrouped)
    val name: String,
    val colorIndex: Int?, // null = Inbox tint, not a palette entry
    val items: List<ItemEntity>,
)

data class UndoState(val itemId: String, val text: String)

class ShoppViewModel(private val container: AppContainer) : ViewModel() {

    // --- Navigation ---

    private val _screen = MutableStateFlow(Screen.LIST)
    val screen: StateFlow<Screen> = _screen

    private val _drawerOpen = MutableStateFlow(false)
    val drawerOpen: StateFlow<Boolean> = _drawerOpen

    private val _filterLabelId = MutableStateFlow<String?>(null)
    val filterLabelId: StateFlow<String?> = _filterLabelId

    fun openDrawer() {
        _drawerOpen.value = true
    }

    fun closeDrawer() {
        _drawerOpen.value = false
    }

    fun navigateTo(target: Screen) {
        _screen.value = target
        _drawerOpen.value = false
    }

    // Back arrow: sub-screen -> List, or a filtered List -> unfiltered List.
    fun goBack() {
        if (_screen.value != Screen.LIST) {
            _screen.value = Screen.LIST
        } else {
            _filterLabelId.value = null
        }
    }

    fun filterByLabel(labelId: String) {
        _filterLabelId.value = labelId
        _screen.value = Screen.LIST
        _drawerOpen.value = false
    }

    // Drawer's "All items" -- always clears any active filter, regardless
    // of the current screen (matches the prototype's explicit filter:null).
    fun goToAllItems() {
        _filterLabelId.value = null
        _screen.value = Screen.LIST
        _drawerOpen.value = false
    }

    // --- Preferences ---

    val themeMode: StateFlow<ThemeMode> = container.preferencesRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)
    val groupByLabel: StateFlow<Boolean> = container.preferencesRepository.groupByLabel
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)
    val keepQuickAddOpen: StateFlow<Boolean> = container.preferencesRepository.keepQuickAddOpen
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)
    val confirmBeforeClearing: StateFlow<Boolean> = container.preferencesRepository.confirmBeforeClearing
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { container.preferencesRepository.setThemeMode(mode) }
    }

    fun setGroupByLabel(value: Boolean) {
        viewModelScope.launch { container.preferencesRepository.setGroupByLabel(value) }
    }

    fun setKeepQuickAddOpen(value: Boolean) {
        viewModelScope.launch { container.preferencesRepository.setKeepQuickAddOpen(value) }
    }

    fun setConfirmBeforeClearing(value: Boolean) {
        viewModelScope.launch { container.preferencesRepository.setConfirmBeforeClearing(value) }
    }

    // --- List screen ---

    val sections: StateFlow<List<ListSection>> = combine(
        container.itemRepository.observeActiveItems(),
        container.labelRepository.observeLabels(),
        groupByLabel,
        _filterLabelId,
    ) { items, labels, grouped, filterLabelId ->
        buildSections(items, labels, grouped, filterLabelId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val labels: StateFlow<List<LabelEntity>> =
        container.labelRepository.observeLabels()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Raw/unfiltered/ungrouped, for the drawer's total count and the Labels
    // screen's per-label active counts, both independent of the List
    // screen's own filter/group-by state.
    val activeItems: StateFlow<List<ItemEntity>> =
        container.itemRepository.observeActiveItems()
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

    // --- Quick Add (shared controller -- see QuickAddController for why) ---

    private val quickAddController = QuickAddController(viewModelScope, container)
    val quickAdd: StateFlow<QuickAddState> = quickAddController.state

    fun openQuickAdd() = quickAddController.open()
    fun closeQuickAdd() = quickAddController.close()
    fun updateQuickAddDraft(text: String) = quickAddController.updateDraft(text)
    fun selectStickyChip(labelId: String?) = quickAddController.selectStickyChip(labelId)
    fun acceptSuggestion(labelName: String) = quickAddController.acceptSuggestion(labelName)
    fun acceptTitleSuggestion(title: String) = quickAddController.acceptTitleSuggestion(title)
    fun submitQuickAdd() = quickAddController.submit()

    // --- Recently Completed ---

    val completedItems: StateFlow<List<ItemEntity>> =
        container.itemRepository.observeCompletedItems()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var readdUndoJob: Job? = null
    private val _readdUndo = MutableStateFlow<UndoState?>(null)
    val readdUndo: StateFlow<UndoState?> = _readdUndo

    fun readdCompleted(item: ItemEntity) {
        readdUndoJob?.cancel()
        viewModelScope.launch {
            val groupName = item.labelId?.let { id -> labels.value.find { it.id == id }?.name } ?: "Inbox"
            container.readdCompleted(item.id)
            _readdUndo.value = UndoState(item.id, "Added to $groupName")
            readdUndoJob = launch {
                delay(UNDO_WINDOW_MS)
                _readdUndo.value = null
            }
        }
    }

    // Undoing a readd puts the item back into Recently Completed via the
    // normal complete flow -- it re-enters at "just now" rather than its
    // original completedAt, an acceptable simplification within the short
    // undo window.
    fun undoReadd() {
        val state = _readdUndo.value ?: return
        readdUndoJob?.cancel()
        _readdUndo.value = null
        viewModelScope.launch { container.completeItem(state.itemId) }
    }

    fun clearAllCompleted() {
        viewModelScope.launch { container.itemRepository.tombstoneAllCompleted() }
    }

    // --- Labels management ---

    fun renameLabel(labelId: String, newName: String, onResult: (RenameLabel.Result) -> Unit) {
        viewModelScope.launch { onResult(container.renameLabel(labelId, newName)) }
    }

    fun setLabelColor(labelId: String, colorIndex: Int) {
        viewModelScope.launch { container.setLabelColor(labelId, colorIndex) }
    }

    fun mergeLabels(sourceLabelId: String, targetLabelId: String) {
        viewModelScope.launch { container.mergeLabels(sourceLabelId, targetLabelId) }
    }

    fun deleteLabel(labelId: String) {
        viewModelScope.launch { container.deleteLabel(labelId) }
    }

    // Inbox is always pinned first, even when empty (PRD §7.3) -- a
    // deliberate deviation from the prototype's mockup logic, which only
    // renders a section once it has at least one item. When ungrouped, a
    // single "All items" (or the filtered label's name) section is used
    // instead, matching the prototype's `group` toggle.
    private fun buildSections(
        items: List<ItemEntity>,
        labels: List<LabelEntity>,
        groupByLabel: Boolean,
        filterLabelId: String?,
    ): List<ListSection> {
        val filtered = if (filterLabelId != null) items.filter { it.labelId == filterLabelId } else items

        if (!groupByLabel) {
            if (filtered.isEmpty()) return emptyList()
            val filterLabel = filterLabelId?.let { id -> labels.find { it.id == id } }
            val name = filterLabel?.name ?: "All items"
            return listOf(ListSection(filterLabelId, name, filterLabel?.colorIndex, filtered))
        }

        val byLabel = filtered.groupBy { it.labelId }
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
