package com.schultegrid.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schultegrid.data.repository.GameRepository
import com.schultegrid.domain.model.DailyStatistics
import com.schultegrid.domain.model.MonthlyStatistics
import com.schultegrid.domain.model.StatisticsPeriod
import com.schultegrid.domain.model.YearlyStatistics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 统计页面 UI 状态
 *
 * @property period 当前统计周期
 * @property selectedSize 选中的网格大小，null 表示全部
 * @property selectedDifficulty 选中的难度，null 表示全部
 * @property dailyStats 每日统计数据
 * @property monthlyStats 月度统计数据
 * @property yearlyStats 年度统计数据
 * @property isLoading 加载状态
 * @property error 错误信息
 */
data class StatisticsUiState(
    val period: StatisticsPeriod = StatisticsPeriod.DAY,
    val selectedSize: String? = "5×5",
    val selectedDifficulty: String? = "普通",
    val dailyStats: List<DailyStatistics> = emptyList(),
    val monthlyStats: List<MonthlyStatistics> = emptyList(),
    val yearlyStats: List<YearlyStatistics> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * 统计页面 ViewModel
 *
 * 负责处理统计数据的获取和筛选逻辑。
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState(isLoading = true))
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    // 防抖任务引用
    private var debounceJob: Job? = null

    init {
        loadStatistics()
    }

    /**
     * 更新统计周期
     */
    fun updatePeriod(period: StatisticsPeriod) {
        _uiState.value = _uiState.value.copy(period = period)
    }

    /**
     * 更新网格大小筛选（带防抖）
     */
    fun updateSize(size: String?) {
        _uiState.value = _uiState.value.copy(selectedSize = size)
        debounceLoad()
    }

    /**
     * 更新难度筛选（带防抖）
     */
    fun updateDifficulty(difficulty: String?) {
        _uiState.value = _uiState.value.copy(selectedDifficulty = difficulty)
        debounceLoad()
    }

    /**
     * 防抖加载统计数据
     * 取消之前的任务，延迟 300ms 后执行加载
     */
    private fun debounceLoad() {
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(300)
            loadStatistics()
        }
    }

    /**
     * 重新加载统计数据
     */
    fun loadStatistics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (_uiState.value.period) {
                StatisticsPeriod.DAY -> loadStatistics(
                    repository::getDailyStatistics
                ) { stats -> _uiState.value = _uiState.value.copy(dailyStats = stats) }
                StatisticsPeriod.MONTH -> loadStatistics(
                    repository::getMonthlyStatistics
                ) { stats -> _uiState.value = _uiState.value.copy(monthlyStats = stats) }
                StatisticsPeriod.YEAR -> loadStatistics(
                    repository::getYearlyStatistics
                ) { stats -> _uiState.value = _uiState.value.copy(yearlyStats = stats) }
            }
        }
    }

    /**
     * 通用统计数据加载方法
     *
     * @param loadFunction 数据加载函数
     * @param updateState 状态更新函数
     */
    private fun <T> loadStatistics(
        loadFunction: (String?, String?) -> Flow<List<T>>,
        updateState: (List<T>) -> Unit
    ) {
        viewModelScope.launch {
            loadFunction(_uiState.value.selectedSize, _uiState.value.selectedDifficulty)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "加载失败"
                    )
                }
                .collect { stats ->
                    updateState(stats)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                }
        }
    }
}
