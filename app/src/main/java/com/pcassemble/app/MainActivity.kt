package com.pcassemble.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pcassemble.app.ui.nav.AppNavHost
import com.pcassemble.app.ui.theme.PcAssembleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PcAssembleTheme {
                AppNavHost()
            }
        }
    }
}
