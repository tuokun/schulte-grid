package com.schultegrid.di

import android.content.Context
import com.schultegrid.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt 依赖注入模块
 *
 * 提供应用级别的依赖，如数据库实例。
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * 提供 Room 数据库实例
     *
     * 使用 application context 确保数据库生命周期正确。
     *
     * @param context 应用上下文
     * @return AppDatabase 实例
     */
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getDatabase(context)
    }
}
