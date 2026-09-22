package com.zoti321.c2cmarket.notification

import kotlinx.coroutines.flow.StateFlow

interface AppForegroundState {
    val isForeground: StateFlow<Boolean>
}
