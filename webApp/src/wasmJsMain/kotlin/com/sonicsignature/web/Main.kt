package com.sonicsignature.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.sonicsignature.ui.SonicSignatureAppRoot

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(canvasElementId = "compose-target", title = "Sonic Signature") {
        SonicSignatureAppRoot()
    }
}
