package com.schultegrid.domain.model

import com.schultegrid.data.local.DailyStatisticsEntity
import com.schultegrid.data.local.MonthlyStatisticsEntity
import com.schultegrid.data.local.YearlyStatisticsEntity
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 统计周期枚举
 */
enum class StatisticsPeriod(val displayName: String) {
    DAY("按日"),
    MONTH("按月"),
    YEAR("按年")
}

/**
 * 每日统计
 *
 * @property date 日期，格式: yyyy-MM-dd
 * @property count 游戏次数
 * @property averageScore 平均成绩（秒）
 * @property bestScore 最佳成绩（秒）
 */
data class DailyStatistics(
    val date: String,
    val count: Int,
    val averageScore: Float,
    val bestScore: Float
) {
    /**
     * 获取格式化的日期显示文本
     * @return "03月19日"（不含星期几）
     */
    fun getDisplayDate(): String {
        return try {
            val date = DATE_FORMAT.parse(this.date) ?: return this.date
            DISPLAY_DATE_FORMAT.format(date)
        } catch (e: Exception) {
            this.date
        }
    }

    /**
     * 获取平均成绩显示文本
     */
    fun getAverageScoreText(): String = String.format("%.2f秒", averageScore)

    /**
     * 获取最佳成绩显示文本
     */
    fun getBestScoreText(): String = String.format("%.2f秒", bestScore)

    companion object {
        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        private val DISPLAY_DATE_FORMAT = SimpleDateFormat("MM月dd日", Locale.getDefault())
    }
}

/**
 * 月度统计
 *
 * @property month 月份，格式: yyyy-MM
 * @property count 游戏次数
 * @property averageScore 平均成绩（秒）
 * @property bestScore 最佳成绩（秒）
 */
data class MonthlyStatistics(
    val month: String,
    val count: Int,
    val averageScore: Float,
    val bestScore: Float
) {
    /**
     * 获取格式化的月份显示文本
     * @return "2024年03月"
     */
    fun getDisplayMonth(): String {
        return try {
            val date = MONTH_FORMAT.parse(this.month) ?: return this.month
            DISPLAY_MONTH_FORMAT.format(date)
        } catch (e: Exception) {
            this.month
        }
    }

    fun getAverageScoreText(): String = String.format("%.2f秒", averageScore)
    fun getBestScoreText(): String = String.format("%.2f秒", bestScore)

    companion object {
        private val MONTH_FORMAT = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        private val DISPLAY_MONTH_FORMAT = SimpleDateFormat("yyyy年MM月", Locale.getDefault())
    }
}

/**
 * 年度统计
 *
 * @property year 年份，格式: yyyy
 * @property count 游戏次数
 * @property averageScore 平均成绩（秒）
 * @property bestScore 最佳成绩（秒）
 */
data class YearlyStatistics(
    val year: String,
    val count: Int,
    val averageScore: Float,
    val bestScore: Float
) {
    /**
     * 获取格式化的年份显示文本
     * @return "2024年"
     */
    fun getDisplayYear(): String = "${year}年"

    fun getAverageScoreText(): String = String.format("%.2f秒", averageScore)
    fun getBestScoreText(): String = String.format("%.2f秒", bestScore)
}

/**
 * 趋势数据
 *
 * @property countChange 次数变化 (+N/-N)
 * @property averageChange 平均变化（秒，负数表示进步）
 * @property bestChange 最佳变化（秒，负数表示进步）
 */
data class StatisticsTrend(
    val countChange: Int,
    val averageChange: Float,
    val bestChange: Float?
)

/**
 * 带趋势的统计数据
 */
data class StatisticsWithTrend<T>(
    val data: T,
    val trend: StatisticsTrend?
)

// ========== 实体转换扩展函数 ==========

/**
 * 将 DailyStatisticsEntity 转换为 DailyStatistics
 */
fun DailyStatisticsEntity.toDomainModel(): DailyStatistics {
    return DailyStatistics(
        date = date,
        count = count,
        averageScore = averageScore,
        bestScore = bestScore
    )
}

/**
 * 将 MonthlyStatisticsEntity 转换为 MonthlyStatistics
 */
fun MonthlyStatisticsEntity.toDomainModel(): MonthlyStatistics {
    return MonthlyStatistics(
        month = month,
        count = count,
        averageScore = averageScore,
        bestScore = bestScore
    )
}

/**
 * 将 YearlyStatisticsEntity 转换为 YearlyStatistics
 */
fun YearlyStatisticsEntity.toDomainModel(): YearlyStatistics {
    return YearlyStatistics(
        year = year,
        count = count,
        averageScore = averageScore,
        bestScore = bestScore
    )
}
