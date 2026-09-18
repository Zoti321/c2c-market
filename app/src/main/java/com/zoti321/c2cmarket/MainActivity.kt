package com.zoti321.c2cmarket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.zoti321.c2cmarket.ui.navigation.C2CApp
import com.zoti321.c2cmarket.ui.theme.C2cmarketTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            C2cmarketTheme {
                C2CApp()
            }
        }
    }
}
