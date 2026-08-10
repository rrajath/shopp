package com.rrajath.milk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.rrajath.milk.ui.theme.ShoppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShoppTheme {
                // Placeholder until the List screen lands (milestone 6).
                Box(modifier = Modifier.fillMaxSize().background(ShoppTheme.colors.background)) {
                    Text(text = "Shopp", color = ShoppTheme.colors.foreground)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceholderPreview() {
    ShoppTheme {
        Box(modifier = Modifier.fillMaxSize().background(ShoppTheme.colors.background)) {
            Text(text = "Shopp", color = ShoppTheme.colors.foreground)
        }
    }
}