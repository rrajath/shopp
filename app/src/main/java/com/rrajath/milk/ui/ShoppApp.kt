package com.rrajath.milk.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.rrajath.milk.ui.components.ConfirmDialog
import com.rrajath.milk.ui.components.DrawerCounts
import com.rrajath.milk.ui.components.DrawerMenu
import com.rrajath.milk.ui.components.HeaderRow
import com.rrajath.milk.ui.components.QuickAddOverlay
import com.rrajath.milk.ui.screens.LabelsScreen
import com.rrajath.milk.ui.screens.ListScreen
import com.rrajath.milk.ui.screens.RecentlyCompletedScreen
import com.rrajath.milk.ui.screens.SettingsScreen
import com.rrajath.milk.ui.theme.ShoppTheme

@Composable
fun ShoppApp(viewModel: ShoppViewModel) {
    val screen by viewModel.screen.collectAsState()
    val drawerOpen by viewModel.drawerOpen.collectAsState()
    val filterLabelId by viewModel.filterLabelId.collectAsState()
    val sections by viewModel.sections.collectAsState()
    val labels by viewModel.labels.collectAsState()
    val activeItems by viewModel.activeItems.collectAsState()
    val undo by viewModel.undo.collectAsState()
    val readdUndo by viewModel.readdUndo.collectAsState()
    val quickAdd by viewModel.quickAdd.collectAsState()
    val completedItems by viewModel.completedItems.collectAsState()
    val groupByLabel by viewModel.groupByLabel.collectAsState()
    val keepQuickAddOpen by viewModel.keepQuickAddOpen.collectAsState()
    val confirmBeforeClearing by viewModel.confirmBeforeClearing.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    var showClearConfirm by remember { mutableStateOf(false) }

    val filterLabel = filterLabelId?.let { id -> labels.find { it.id == id } }
    val title = when (screen) {
        Screen.LIST -> filterLabel?.name ?: "Shopp"
        Screen.RECENTLY_COMPLETED -> "Recently completed"
        Screen.LABELS -> "Labels"
        Screen.SETTINGS -> "Settings"
    }
    val showBack = screen != Screen.LIST || filterLabelId != null
    val hasCompleted = completedItems.isNotEmpty()

    BackHandler(enabled = quickAdd.open) { viewModel.closeQuickAdd() }
    BackHandler(enabled = !quickAdd.open && drawerOpen) { viewModel.closeDrawer() }
    BackHandler(enabled = !quickAdd.open && !drawerOpen && showBack) { viewModel.goBack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ShoppTheme.colors.background)
            .systemBarsPadding(),
    ) {
        Column(Modifier.fillMaxSize()) {
            HeaderRow(
                title = title,
                showBack = showBack,
                trailingLabel = if (screen == Screen.RECENTLY_COMPLETED && hasCompleted) "Clear" else null,
                onLeadTap = { if (showBack) viewModel.goBack() else viewModel.openDrawer() },
                onTrailTap = {
                    if (confirmBeforeClearing) showClearConfirm = true else viewModel.clearAllCompleted()
                },
            )
            Box(Modifier.weight(1f)) {
                when (screen) {
                    Screen.LIST -> ListScreen(
                        sections = sections,
                        undo = undo,
                        emptyTitle = if (filterLabel != null) "Nothing on this list" else "Nothing to buy",
                        emptyBody = if (filterLabel != null) {
                            "Add something with the chip already set to ${filterLabel.name}."
                        } else {
                            "Tap Add, or hold the Quick Settings tile to jot something down without unlocking."
                        },
                        onCompleteItem = viewModel::completeItem,
                        onCommitEdit = viewModel::editTitle,
                        onUndo = viewModel::undoLastComplete,
                        onAddClick = viewModel::openQuickAdd,
                        onSwipeRightOpenDrawer = viewModel::openDrawer,
                    )

                    Screen.RECENTLY_COMPLETED -> RecentlyCompletedScreen(
                        completedItems = completedItems,
                        labels = labels,
                        readdUndo = readdUndo,
                        onReadd = viewModel::readdCompleted,
                        onUndoReadd = viewModel::undoReadd,
                    )

                    Screen.LABELS -> {
                        val activeCounts = remember(activeItems) {
                            activeItems.mapNotNull { it.labelId }.groupingBy { it }.eachCount()
                        }
                        LabelsScreen(
                            labels = labels,
                            activeCounts = activeCounts,
                            onFilter = viewModel::filterByLabel,
                            onRename = viewModel::renameLabel,
                            onColorChange = viewModel::setLabelColor,
                            onMerge = viewModel::mergeLabels,
                            onDelete = viewModel::deleteLabel,
                        )
                    }

                    Screen.SETTINGS -> SettingsScreen(
                        themeMode = themeMode,
                        groupByLabel = groupByLabel,
                        keepQuickAddOpen = keepQuickAddOpen,
                        confirmBeforeClearing = confirmBeforeClearing,
                        onThemeModeChange = viewModel::setThemeMode,
                        onGroupByLabelChange = viewModel::setGroupByLabel,
                        onKeepQuickAddOpenChange = viewModel::setKeepQuickAddOpen,
                        onConfirmBeforeClearingChange = viewModel::setConfirmBeforeClearing,
                    )
                }
            }
        }

        if (quickAdd.open) {
            val suggestions = remember(quickAdd.draft, labels) { quickAddSuggestions(quickAdd.draft, labels) }
            QuickAddOverlay(
                draft = quickAdd.draft,
                stickyLabelId = quickAdd.stickyLabelId,
                labels = labels,
                suggestions = suggestions,
                sessionAdds = quickAdd.sessionAdds,
                onDraftChange = viewModel::updateQuickAddDraft,
                onSelectSticky = viewModel::selectStickyChip,
                onAcceptSuggestion = viewModel::acceptSuggestion,
                onSubmit = viewModel::submitQuickAdd,
                onDismiss = viewModel::closeQuickAdd,
            )
        }

        DrawerMenu(
            visible = drawerOpen,
            counts = DrawerCounts(
                activeCount = activeItems.size,
                completedCount = completedItems.size,
                labelCount = labels.size,
            ),
            currentScreen = screen,
            isListFiltered = filterLabelId != null,
            onSelect = { target ->
                if (target == Screen.LIST) viewModel.goToAllItems() else viewModel.navigateTo(target)
            },
            onDismiss = viewModel::closeDrawer,
        )
    }

    if (showClearConfirm) {
        ConfirmDialog(
            message = "Clear all recently completed items? This can't be undone.",
            confirmLabel = "Clear",
            onCancel = { showClearConfirm = false },
            onConfirm = {
                showClearConfirm = false
                viewModel.clearAllCompleted()
            },
        )
    }
}
