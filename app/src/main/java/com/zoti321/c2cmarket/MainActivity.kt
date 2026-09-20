package com.zoti321.c2cmarket

import android.content.Intent
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
        val pendingOrderId = intent.getLongExtra(EXTRA_ORDER_ID, -1L).takeIf { it > 0 }
        setContent {
            C2cmarketTheme {
                C2CApp(pendingOrderId = pendingOrderId)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val pendingOrderId = intent.getLongExtra(EXTRA_ORDER_ID, -1L).takeIf { it > 0 }
        if (pendingOrderId != null) {
            setContent {
                C2cmarketTheme {
                    C2CApp(pendingOrderId = pendingOrderId)
                }
            }
        }
    }

    companion object {
        const val EXTRA_ORDER_ID = "extra_order_id"
    }
}
