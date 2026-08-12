package com.rrajath.shopp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.rrajath.shopp.ui.ShoppApp
import com.rrajath.shopp.ui.ShoppViewModel
import com.rrajath.shopp.ui.ShoppViewModelFactory
import com.rrajath.shopp.ui.theme.ShoppTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ShoppViewModel by viewModels {
        ShoppViewModelFactory((application as ShoppApplication).container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            ShoppTheme(mode = themeMode) {
                ShoppApp(viewModel = viewModel)
            }
        }
    }
}
