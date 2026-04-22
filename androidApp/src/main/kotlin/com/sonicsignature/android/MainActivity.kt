package com.sonicsignature.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sonicsignature.android.ui.navigation.AppNavGraph
import com.sonicsignature.ui.theme.SonicSignatureTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SonicSignatureTheme {
                AppNavGraph()
            }
        }
    }
}
