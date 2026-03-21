package com.schultegrid.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.schultegrid.R

/**
 * 统计项卡片
 *
 * @param title 标题（如 "03月19日 周三"）
 * @param count 游戏次数
 * @param averageScore 平均成绩文本（如 "12.34秒"）
 * @param bestScore 最佳成绩文本（如 "10.56秒"）
 * @param countTrend 次数变化文本（如 "+2" / "-1"）
 * @param averageTrend 平均变化文本（如 "+0.5s" / "-0.3s"）
 * @param bestTrend 最佳变化文本（如 "+0.2s" / "-0.1s"）
 */
@Composable
fun StatisticsItemCard(
    title: String,
    count: Int,
    averageScore: String,
    bestScore: String,
    countTrend: String? = null,
    averageTrend: String? = null,
    bestTrend: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // 标题行
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 次数行
            StatRow(
                label = stringResource(R.string.stats_count),
                value = stringResource(R.string.stats_count_with_value, count),
                trend = countTrend
            )
            Spacer(modifier = Modifier.height(4.dp))

            // 平均成绩行
            StatRow(
                label = stringResource(R.string.stats_average),
                value = averageScore,
                trend = averageTrend
            )
            Spacer(modifier = Modifier.height(4.dp))

            // 最佳成绩行
            StatRow(
                label = stringResource(R.string.stats_best),
                value = bestScore,
                trend = bestTrend
            )
        }
    }
}

/**
 * 统计数据行
 */
@Composable
private fun StatRow(
    label: String,
    value: String,
    trend: String?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(48.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        if (trend != null) {
            Text(
                text = trend,
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    trend.startsWith("+") && !trend.contains("+0.0") -> MaterialTheme.colorScheme.error
                    trend.startsWith("-") && !trend.contains("-0.0") -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = FontWeight.Light
            )
        }
    }
}

/**
 * 筛选下拉选择器
 *
 * @param label 标签
 * @param options 选项列表
 * @param selectedOption 当前选中项（null 表示"全部"）
 * @param onOptionSelected 选项选中回调
 */
@Composable
fun FilterSelector(
    label: String,
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { expanded = true },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Text(
            text = selectedOption ?: stringResource(R.string.filter_all),
            style = MaterialTheme.typography.bodyMedium
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.fillMaxWidth(0.9f)
    ) {
        // "全部" 选项
        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(R.string.filter_all),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            onClick = {
                onOptionSelected(null)
                expanded = false
            }
        )
        options.forEach { option ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                onClick = {
                    onOptionSelected(option)
                    expanded = false
                }
            )
        }
    }
}
