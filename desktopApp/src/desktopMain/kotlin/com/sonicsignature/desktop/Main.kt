package com.sonicsignature.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sonicsignature.desktop.ui.MainWindow

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Sonic Signature"
    ) {
        MainWindow()
    }
}
