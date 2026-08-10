package com.rrajath.milk.capture

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.rrajath.milk.MilkApplication
import com.rrajath.milk.ui.components.QuickAddOverlay
import com.rrajath.milk.ui.quickAddSuggestions
import com.rrajath.milk.ui.theme.ShoppTheme
import com.rrajath.milk.ui.theme.ThemeMode

/**
 * TDD §7.2/§7.3: the Quick Settings tile's capture surface. Native, not a
 * booted RN/app-navigation flow -- it just hosts the exact same
 * [QuickAddOverlay] the in-app FAB uses, reading off the same
 * [com.rrajath.milk.AppContainer] singleton, so "the same component" is
 * trivially true (TDD §1.2).
 */
class CaptureActivity : ComponentActivity() {

    private val viewModel: CaptureViewModel by viewModels {
        CaptureViewModelFactory((application as MilkApplication).container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        showOverLockScreenAndWakeUp()

        setContent {
            val container = (application as MilkApplication).container
            val themeMode by container.preferencesRepository.themeMode
                .collectAsState(initial = ThemeMode.SYSTEM)
            val quickAdd by viewModel.quickAdd.collectAsState()
            val labels by viewModel.labels.collectAsState()

            // Closing Quick Add here means "done" -- there's no List screen
            // to fall back to, so finish the activity entirely.
            LaunchedEffect(quickAdd.open) {
                if (!quickAdd.open) finish()
            }

            ShoppTheme(mode = themeMode) {
                val suggestions = remember(quickAdd.draft, labels) {
                    quickAddSuggestions(quickAdd.draft, labels)
                }
                QuickAddOverlay(
                    draft = quickAdd.draft,
                    stickyLabelId = quickAdd.stickyLabelId,
                    labels = labels,
                    suggestions = suggestions,
                    sessionAdds = quickAdd.sessionAdds,
                    onDraftChange = viewModel::updateDraft,
                    onSelectSticky = viewModel::selectStickyChip,
                    onAcceptSuggestion = viewModel::acceptSuggestion,
                    onSubmit = viewModel::submit,
                    onDismiss = viewModel::close,
                )
            }
        }
    }

    // showWhenLocked/turnScreenOn is what makes "usable without unlocking"
    // true (PRD §12). The Activity methods exist from API 27; API 26 needs
    // the window-flag equivalents (TDD §7.2: "use both behind a version check").
    private fun showOverLockScreenAndWakeUp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            )
        }
    }
}
