package com.schultegrid.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schultegrid.data.repository.GameRepository
import com.schultegrid.domain.model.GameRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 历史页 UI 状态
 *
 * @property records 游戏记录列表
 * @property bestScores 最佳成绩映射
 * @property isLoading 是否正在加载
 * @property error 错误信息
 */
data class HistoryUiState(
    val records: List<GameRecord> = emptyList(),
    val bestScores: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * 历史页 ViewModel
 *
 * 管理历史记录和最佳成绩的业务逻辑。
 *
 * @property repository 游戏数据仓库
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    /**
     * UI 状态流
     */
    private val _uiState = MutableStateFlow(HistoryUiState(isLoading = true))
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    /**
     * 加载历史数据
     *
     * 使用 combine 合并游戏记录和最佳成绩两个 Flow，确保状态一致更新。
     */
    private fun loadHistory() {
        viewModelScope.launch {
            try {
                combine(
                    repository.getAllRecords(),
                    repository.getBestScores()
                ) { records, bestScores ->
                    HistoryUiState(
                        records = records,
                        bestScores = bestScores,
                        isLoading = false,
                        error = null
                    )
                }
                    .catch { error ->
                        _uiState.value = HistoryUiState(
                            isLoading = false,
                            error = "加载历史数据失败: ${error.message}"
                        )
                    }
                    .collect { newState ->
                        _uiState.value = newState
                    }
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "加载历史数据失败: ${error.message}"
                )
            }
        }
    }

    /**
     * 清空所有历史记录
     *
     * 删除所有游戏记录和最佳成绩。
     */
    fun clearHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                repository.deleteAllRecords().collect { }
                _uiState.value = HistoryUiState(
                    records = emptyList(),
                    bestScores = emptyMap(),
                    isLoading = false
                )
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "清空记录失败: ${error.message}"
                )
            }
        }
    }
}
