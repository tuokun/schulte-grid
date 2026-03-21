package com.schultegrid.ui.screens.statistics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.schultegrid.R
import com.schultegrid.domain.model.StatisticsPeriod
import com.schultegrid.ui.components.EmptyState
import com.schultegrid.ui.components.FilterSelector
import com.schultegrid.ui.components.LoadingIndicator
import com.schultegrid.ui.components.SectionTitle
import com.schultegrid.ui.components.StatisticsItemCard

/**
 * 成绩统计页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onBack: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val sizeOptions = listOf("3×3", "4×4", "5×5", "6×6")
    val difficultyOptions = listOf("简单", "普通", "困难")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.statistics)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 筛选区域
            FilterSection(
                sizeOptions = sizeOptions,
                selectedSize = uiState.selectedSize,
                onSizeSelected = { viewModel.updateSize(it) },
                difficultyOptions = difficultyOptions,
                selectedDifficulty = uiState.selectedDifficulty,
                onDifficultySelected = { viewModel.updateDifficulty(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 周期切换 Tab
            TabRow(
                selectedTabIndex = uiState.period.ordinal,
                modifier = Modifier.fillMaxWidth()
            ) {
                StatisticsPeriod.entries.forEach { period ->
                    Tab(
                        selected = uiState.period == period,
                        onClick = {
                            viewModel.updatePeriod(period)
                            viewModel.loadStatistics()
                        },
                        text = { Text(period.displayName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 数据列表
            when {
                uiState.isLoading -> {
                    LoadingIndicator()
                }
                uiState.error != null -> {
                    EmptyState(uiState.error ?: "加载失败")
                }
                getCurrentStats(uiState).isEmpty() -> {
                    EmptyState(stringResource(R.string.no_statistics_data))
                }
                else -> {
                    when (uiState.period) {
                        StatisticsPeriod.DAY -> {
                            StatisticsList(
                                stats = uiState.dailyStats,
                                getTitle = { it.getDisplayDate() },
                                getCount = { it.count },
                                getAverageScore = { it.getAverageScoreText() },
                                getBestScore = { it.getBestScoreText() }
                            )
                        }
                        StatisticsPeriod.MONTH -> {
                            StatisticsList(
                                stats = uiState.monthlyStats,
                                getTitle = { it.getDisplayMonth() },
                                getCount = { it.count },
                                getAverageScore = { it.getAverageScoreText() },
                                getBestScore = { it.getBestScoreText() }
                            )
                        }
                        StatisticsPeriod.YEAR -> {
                            StatisticsList(
                                stats = uiState.yearlyStats,
                                getTitle = { it.getDisplayYear() },
                                getCount = { it.count },
                                getAverageScore = { it.getAverageScoreText() },
                                getBestScore = { it.getBestScoreText() }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 筛选区域
 */
@Composable
private fun FilterSection(
    sizeOptions: List<String>,
    selectedSize: String?,
    onSizeSelected: (String?) -> Unit,
    difficultyOptions: List<String>,
    selectedDifficulty: String?,
    onDifficultySelected: (String?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        SectionTitle(stringResource(R.string.filter_title))

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.filter_size),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(48.dp)
            )
            FilterSelector(
                label = stringResource(R.string.filter_size),
                options = sizeOptions,
                selectedOption = selectedSize,
                onOptionSelected = onSizeSelected
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.filter_difficulty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(48.dp)
            )
            FilterSelector(
                label = stringResource(R.string.filter_difficulty),
                options = difficultyOptions,
                selectedOption = selectedDifficulty,
                onOptionSelected = onDifficultySelected
            )
        }
    }
}

/**
 * 获取当前周期的统计数据
 *
 * @Suppress("UNCHECKED_CAST") - 每个分支返回具体的 List<T> 类型，但方法签名是 List<*>
 * 这种类型擦除是安全的，因为调用方只使用 isEmpty() 检查
 */
@Suppress("UNCHECKED_CAST")
private fun getCurrentStats(uiState: StatisticsUiState): List<*> {
    return when (uiState.period) {
        StatisticsPeriod.DAY -> uiState.dailyStats
        StatisticsPeriod.MONTH -> uiState.monthlyStats
        StatisticsPeriod.YEAR -> uiState.yearlyStats
    }
}

/**
 * 统计列表组件（泛型版本）
 */
@Composable
private fun <T> StatisticsList(
    stats: List<T>,
    getTitle: (T) -> String,
    getCount: (T) -> Int,
    getAverageScore: (T) -> String,
    getBestScore: (T) -> String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(stats) { stat ->
            StatisticsItemCard(
                title = getTitle(stat),
                count = getCount(stat),
                averageScore = getAverageScore(stat),
                bestScore = getBestScore(stat)
            )
        }
    }
}
