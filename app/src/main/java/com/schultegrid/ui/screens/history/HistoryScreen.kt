package com.schultegrid.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.schultegrid.R
import com.schultegrid.ui.components.EmptyState
import com.schultegrid.ui.components.RecordItemCard
import com.schultegrid.ui.components.SectionTitle
import com.schultegrid.ui.theme.SchulteGridTheme

/**
 * 历史记录屏幕
 *
 * 显示游戏记录和最佳成绩。
 *
 * @param onBack 返回回调
 * @param viewModel 历史页 ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    // 收集 UI 状态
    val uiState by viewModel.uiState.collectAsState()

    SchulteGridTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.history)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    }
                )
            }
        ) { paddingValues ->
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // 最佳成绩区域
                    item {
                        SectionTitle(stringResource(R.string.best_scores))
                    }

                    if (uiState.bestScores.isNotEmpty()) {
                        item {
                            BestScoresGrid(bestScores = uiState.bestScores)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 历史记录区域
                    item {
                        SectionTitle(stringResource(R.string.recent_records))
                    }

                    if (uiState.records.isEmpty()) {
                        item {
                            EmptyState(stringResource(R.string.no_records))
                        }
                    } else {
                        items(uiState.records) { record ->
                            RecordItemCard(
                                time = record.getFormattedTimestamp(),
                                size = record.getSize(),
                                difficulty = record.getDifficulty(),
                                score = record.getFormattedScore()
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

/**
 * 最佳成绩网格
 *
 * 以网格形式展示各个规格的最佳成绩。
 *
 * @param bestScores 最佳成绩映射
 */
@Composable
fun BestScoresGrid(
    bestScores: Map<String, String>
) {
    val sizes = listOf("3×3", "4×4", "5×5", "6×6")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        sizes.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { size ->
                    BestScoreCard(
                        size = size,
                        score = bestScores[size] ?: "--",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * 最佳成绩卡片
 *
 * 显示单个规格的最佳成绩。
 *
 * @param size 网格大小
 * @param score 最佳成绩
 * @param modifier 修饰符
 */
@Composable
fun BestScoreCard(
    size: String,
    score: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = size,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = score,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// 扩展函数：将列表分块
private fun <T> List<T>.chunked(size: Int): List<List<T>> {
    return this.windowed(size, size, partialWindows = true)
}
