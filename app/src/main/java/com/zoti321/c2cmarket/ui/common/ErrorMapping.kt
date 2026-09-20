package com.zoti321.c2cmarket.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.zoti321.c2cmarket.R
import com.zoti321.c2cmarket.data.auth.MissingWebClientIdException
import java.io.IOException

fun Throwable.fallbackUserMessage(): String = when (this) {
    is IOException -> "无法连接网络，请检查后重试"
    is MissingWebClientIdException -> message ?: "Google Web Client ID 未配置"
    else -> message?.takeIf { it.isNotBlank() } ?: "加载失败，请稍后重试"
}

@Composable
fun Throwable.userMessage(): String = when (this) {
    is IOException -> stringResource(R.string.error_network)
    is MissingWebClientIdException -> message ?: stringResource(R.string.profile_sign_in_failed)
    else -> stringResource(R.string.error_generic)
}
