package com.zoti321.c2cmarket.notification

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class AppForegroundTracker @Inject constructor() : DefaultLifecycleObserver, AppForegroundState {

    private val _isForeground = MutableStateFlow(false)
    override val isForeground: StateFlow<Boolean> = _isForeground.asStateFlow()

    fun register() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        _isForeground.value = true
    }

    override fun onStop(owner: LifecycleOwner) {
        _isForeground.value = false
    }
}
