package com.rrajath.milk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.rrajath.milk.ui.ShoppViewModel
import com.rrajath.milk.ui.ShoppViewModelFactory
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

                ListScreen(
                    sections = sections,
                    undo = undo,
                    onCompleteItem = viewModel::completeItem,
                    onCommitEdit = viewModel::editTitle,
                    onUndo = viewModel::undoLastComplete,
                    onAddClick = { /* wired to Quick Add in milestone 7 */ },
                )
            }
        }
    }
}
