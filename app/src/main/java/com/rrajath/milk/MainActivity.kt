package com.rrajath.milk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.rrajath.milk.ui.ShoppViewModel
import com.rrajath.milk.ui.ShoppViewModelFactory
import com.rrajath.milk.ui.components.QuickAddOverlay
import com.rrajath.milk.ui.quickAddSuggestions
import com.rrajath.milk.ui.screens.ListScreen
import com.rrajath.milk.ui.theme.ShoppTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ShoppViewModel by viewModels {
        ShoppViewModelFactory((application as MilkApplication).container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShoppTheme {
                val sections by viewModel.sections.collectAsState()
                val undo by viewModel.undo.collectAsState()
                val quickAdd by viewModel.quickAdd.collectAsState()
                val labels by viewModel.labels.collectAsState()

                Box(modifier = Modifier.fillMaxSize()) {
                    ListScreen(
                        sections = sections,
                        undo = undo,
                        onCompleteItem = viewModel::completeItem,
                        onCommitEdit = viewModel::editTitle,
                        onUndo = viewModel::undoLastComplete,
                        onAddClick = viewModel::openQuickAdd,
                    )

                    if (quickAdd.open) {
                        val suggestions = remember(quickAdd.draft, labels) {
                            quickAddSuggestions(quickAdd.draft, labels)
                        }
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
                }
            }
        }
    }
}
