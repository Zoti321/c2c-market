package com.zoti321.c2cmarket.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.zoti321.c2cmarket.R
import java.io.IOException

@Composable
fun Throwable.userMessage(): String = when (this) {
    is IOException -> stringResource(R.string.error_network)
    else -> stringResource(R.string.error_generic)
}
