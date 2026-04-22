package com.sonicsignature.ui.util

import android.view.ViewTreeObserver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

@Composable
actual fun rememberKeyboardVisible(): Boolean {
    val view = LocalView.current
    var isKeyboardVisible by remember { mutableStateOf(false) }

    DisposableEffect(view) {
        val observer = view.viewTreeObserver
        val updateVisibility = {
            isKeyboardVisible =
                    ViewCompat.getRootWindowInsets(view)
                            ?.isVisible(WindowInsetsCompat.Type.ime()) == true
        }
        val listener = ViewTreeObserver.OnGlobalLayoutListener { updateVisibility() }
        observer.addOnGlobalLayoutListener(listener)
        updateVisibility()

        onDispose {
            if (observer.isAlive) {
                observer.removeOnGlobalLayoutListener(listener)
            }
        }
    }

    return isKeyboardVisible
}
