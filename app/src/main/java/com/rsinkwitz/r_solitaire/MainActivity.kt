package com.rsinkwitz.r_solitaire

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.rsinkwitz.r_solitaire.ui.SolitaireScreen
import com.rsinkwitz.r_solitaire.ui.theme.RSolitaireTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RSolitaireTheme {
                SolitaireScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SolitairePreview() {
    RSolitaireTheme {
        SolitaireScreen()
    }
}