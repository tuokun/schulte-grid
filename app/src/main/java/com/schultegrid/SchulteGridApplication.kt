package com.schultegrid

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Schulte Grid
 *
 * 初始化 Hilt 依赖注入，这是整个应用的入口点。
 * 使用 @HiltAndroidApp 注解触发 Hilt 代码生成。
 */
@HiltAndroidApp
class SchulteGridApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Hilt 会自动处理依赖注入，无需手动初始化
    }
}
