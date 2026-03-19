package com.schultegrid.domain.model

import kotlin.jvm.JvmName
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 游戏记录
 *
 * 表示一条已保存的游戏记录，包含时间、配置和成绩信息。
 * 使用 data class 简化代码，提供格式化方法。
 *
 * @property timestamp 游戏完成时间戳（毫秒）
 * @property size 网格大小
 * @property difficulty 难度级别
 * @property score 完成时间（秒）
 */
data class GameRecord(
    val timestamp: Long,
    val size: String,
    val difficulty: String,
    val score: Float
) {
    /**
     * 格式化的时间字符串
     *
     * 使用缓存的 DateTimeFormatter 确保线程安全且性能优化。
     *
     * @return 格式化的时间，如 "2024-03-19 20:30:45"
     */
    fun getFormattedTimestamp(): String {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
            .format(DATE_FORMATTER)
    }

    /**
     * 格式化的成绩字符串
     *
     * @return 格式化的成绩，如 "12.34s"
     */
    fun getFormattedScore(): String {
        return String.format(Locale.getDefault(), "%.2fs", score)
    }

    /**
     * 获取网格大小
     */
    @JvmName("getSizeValue")
    fun getSize(): String = size

    /**
     * 获取难度级别
     */
    @JvmName("getDifficultyValue")
    fun getDifficulty(): String = difficulty

    companion object {
        /**
         * 缓存的日期格式化器，避免重复创建
         * DateTimeFormatter 是线程安全的，可以安全地在多线程环境中共享
         */
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    }
}
