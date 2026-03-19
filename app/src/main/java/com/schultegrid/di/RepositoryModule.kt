package com.schultegrid.di

import com.schultegrid.data.local.AppDatabase
import com.schultegrid.data.local.GameRecordDao
import com.schultegrid.data.preferences.SettingsPreferences
import com.schultegrid.data.repository.GameRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 仓库层依赖注入模块
 *
 * 提供数据访问相关的依赖，包括 DAO、Repository 和 Preferences。
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
     * 提供游戏记录 DAO
     *
     * 从数据库实例中获取 DAO，用于数据库操作。
     *
     * @param database 应用数据库实例
     * @return GameRecordDao 实例
     */
    @Provides
    @Singleton
    fun provideGameRecordDao(
        database: AppDatabase
    ): GameRecordDao {
        return database.gameRecordsDao()
    }

    /**
     * 提供游戏仓库
     *
     * 仓库层统一管理数据访问，封装 DAO 和数据转换逻辑。
     *
     * @param gameRecordsDao 游戏记录 DAO
     * @return GameRepository 实例
     */
    @Provides
    @Singleton
    fun provideGameRepository(
        gameRecordsDao: GameRecordDao
    ): GameRepository {
        return GameRepository(gameRecordsDao)
    }

    /**
     * 提供设置偏好管理器
     *
     * 管理 DataStore，用于保存和读取用户设置。
     *
     * @param context 应用上下文
     * @return SettingsPreferences 实例
     */
    @Provides
    @Singleton
    fun provideSettingsPreferences(
        @ApplicationContext context: android.content.Context
    ): SettingsPreferences {
        return SettingsPreferences(context)
    }
}
