package com.rrajath.shopp.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rrajath.shopp.AppContainer
import com.rrajath.shopp.data.db.ItemEntity
import com.rrajath.shopp.data.db.LabelEntity
import com.rrajath.shopp.ui.QuickAddController
import com.rrajath.shopp.ui.QuickAddState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * CaptureActivity's whole job is Quick Add, opened immediately -- this is a
 * thin wrapper around the exact same [QuickAddController] the in-app FAB
 * uses, reading off the same [AppContainer] (same Room instance, same
 * process) so there is no separate "kernel" to keep in sync (TDD §1.2/§7.1).
 */
class CaptureViewModel(container: AppContainer) : ViewModel() {

    val labels: StateFlow<List<LabelEntity>> =
        container.labelRepository.observeLabels()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val completedItems: StateFlow<List<ItemEntity>> =
        container.itemRepository.observeCompletedItems()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val controller = QuickAddController(viewModelScope, container, initiallyOpen = true)
    val quickAdd: StateFlow<QuickAddState> = controller.state

    fun updateDraft(text: String) = controller.updateDraft(text)
    fun selectStickyChip(labelId: String?) = controller.selectStickyChip(labelId)
    fun acceptSuggestion(labelName: String) = controller.acceptSuggestion(labelName)
    fun acceptTitleSuggestion(title: String) = controller.acceptTitleSuggestion(title)
    fun submit() = controller.submit()
    fun close() = controller.close()

    // Re-opens Quick Add on a fresh tile tap that gets delivered to this
    // same (singleTask) instance via onNewIntent -- see CaptureActivity.
    fun reopen() = controller.open()
}

class CaptureViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CaptureViewModel(container) as T
    }
}
