package com.schultegrid.data.local

/**
 * 统计数据实体（数据库查询结果）
 *
 * 注意：Room 需要精确的列名匹配
 */

/**
 * 每日统计数据实体
 *
 * @property date 日期，格式: yyyy-MM-dd
 * @property count 游戏次数
 * @property averageScore 平均成绩（秒）
 * @property bestScore 最佳成绩（秒）
 */
data class DailyStatisticsEntity(
    val date: String,
    val count: Int,
    val averageScore: Float,
    val bestScore: Float
)

/**
 * 月度统计数据实体
 *
 * @property month 月份，格式: yyyy-MM
 * @property count 游戏次数
 * @property averageScore 平均成绩（秒）
 * @property bestScore 最佳成绩（秒）
 */
data class MonthlyStatisticsEntity(
    val month: String,
    val count: Int,
    val averageScore: Float,
    val bestScore: Float
)

/**
 * 年度统计数据实体
 *
 * @property year 年份，格式: yyyy
 * @property count 游戏次数
 * @property averageScore 平均成绩（秒）
 * @property bestScore 最佳成绩（秒）
 */
data class YearlyStatisticsEntity(
    val year: String,
    val count: Int,
    val averageScore: Float,
    val bestScore: Float
)
