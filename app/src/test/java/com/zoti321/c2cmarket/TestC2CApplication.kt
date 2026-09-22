package com.zoti321.c2cmarket

import android.app.Application

/**
 * Robolectric 单元测试用 Application：不启用 Hilt/Firebase，避免
 * [C2CApplication] 在测试进程注入 RemoteSync 时触发 Firebase 初始化。
 */
class TestC2CApplication : Application()
