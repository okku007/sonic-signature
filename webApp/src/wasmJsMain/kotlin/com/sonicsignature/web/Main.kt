package com.sonicsignature.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.sonicsignature.ui.SonicSignatureAppRoot

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport("compose-target") {
        SonicSignatureAppRoot()
    }
}
