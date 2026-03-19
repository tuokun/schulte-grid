package com.schultegrid.ui.screens.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schultegrid.data.preferences.SettingsPreferences
import com.schultegrid.domain.model.GameConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 主页 UI 状态
 *
 * 包含主页屏幕需要显示的所有状态数据。
 *
 * @property isLoading 是否正在加载
 * @property gameConfig 当前游戏配置
 * @property error 错误信息
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val gameConfig: GameConfig? = null,
    val error: String? = null
)

/**
 * 主页 ViewModel
 *
 * 管理主页的业务逻辑和状态，使用 Kotlin Flow 和 Coroutines。
 *
 * @property preferences 设置偏好管理器
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preferences: SettingsPreferences
) : ViewModel() {

    /**
     * UI 状态流
     *
     * 使用 StateFlow 提供响应式状态更新。
     */
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    /**
     * 加载游戏设置
     *
     * 从 DataStore 读取用户设置并更新 UI 状态。
     */
    private fun loadSettings() {
        preferences.gameConfig
            .onEach { config ->
                _uiState.value = HomeUiState(
                    isLoading = false,
                    gameConfig = config,
                    error = null
                )
            }
            .catch { error ->
                _uiState.value = HomeUiState(
                    isLoading = false,
                    error = "加载设置失败: ${error.message}"
                )
            }
            .launchIn(viewModelScope)
    }

    /**
     * 更新网格大小
     *
     * 保存新的网格大小设置并更新 UI 状态。
     *
     * @param size 新的网格大小
     */
    fun updateSize(size: String) {
        viewModelScope.launch {
            try {
                preferences.setSize(size)
                // Flow 会自动更新 UI 状态
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "保存设置失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 更新难度级别
     *
     * 保存新的难度设置并更新 UI 状态。
     *
     * @param difficulty 新的难度级别
     */
    fun updateDifficulty(difficulty: String) {
        viewModelScope.launch {
            try {
                preferences.setDifficulty(difficulty)
                // Flow 会自动更新 UI 状态
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "保存设置失败: ${e.message}"
                )
            }
        }
    }
}
