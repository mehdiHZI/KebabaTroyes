package com.amhsolution.caisse

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.amhsolution.caisse.ui.app.CaisseApp
import com.amhsolution.caisse.ui.system.enableFullScreenImmersiveMode
import com.amhsolution.caisse.ui.theme.AMHCaisseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        enableFullScreenImmersiveMode()
        setContent {
            AMHCaisseTheme {
                CaisseApp()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        enableFullScreenImmersiveMode()
    }

    override fun onResume() {
        super.onResume()
        enableFullScreenImmersiveMode()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            enableFullScreenImmersiveMode()
        }
    }
}
