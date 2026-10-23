package com.amhsolution.caisse.ui.system

import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

fun ComponentActivity.enableFullScreenImmersiveMode() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    WindowInsetsControllerCompat(window, window.decorView).apply {
        hide(WindowInsetsCompat.Type.systemBars())
        systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
