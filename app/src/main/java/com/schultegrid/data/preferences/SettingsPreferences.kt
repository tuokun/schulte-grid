package com.schultegrid.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.schultegrid.domain.model.GameConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 游戏设置偏好管理
 *
 * 使用 DataStore 替代 SharedPreferences，提供类型安全、响应式的数据存储。
 * 所有操作都是挂起函数，支持协程。
 *
 * @property context 应用上下文
 */
@Singleton
class SettingsPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // DataStore 委托属性，延迟初始化
    private val Context.dataStore by preferencesDataStore(name = "settings")

    companion object {
        // Preferences Keys
        private val KEY_SIZE = stringPreferencesKey("size")
        private val KEY_DIFFICULTY = stringPreferencesKey("difficulty")
    }

    /**
     * 游戏配置流
     *
     * 监听设置变化，自动发射最新的游戏配置。
     *
     * @return 游戏配置流
     */
    val gameConfig: Flow<GameConfig> = context.dataStore.data.map { prefs ->
        GameConfig(
            size = prefs[KEY_SIZE] ?: GameConfig.DEFAULT_SIZE,
            difficulty = prefs[KEY_DIFFICULTY] ?: GameConfig.DEFAULT_DIFFICULTY
        )
    }

    /**
     * 保存网格大小设置
     *
     * @param size 网格大小（如 "5×5"）
     */
    suspend fun setSize(size: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SIZE] = size
        }
    }

    /**
     * 保存难度设置
     *
     * @param difficulty 难度级别（如 "普通"）
     */
    suspend fun setDifficulty(difficulty: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DIFFICULTY] = difficulty
        }
    }

    /**
     * 保存完整的游戏配置
     *
     * @param config 游戏配置
     */
    suspend fun saveGameConfig(config: GameConfig) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SIZE] = config.size
            prefs[KEY_DIFFICULTY] = config.difficulty
        }
    }
}
