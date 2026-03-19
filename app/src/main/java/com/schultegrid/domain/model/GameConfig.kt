package com.schultegrid.domain.model

import android.os.Parcelable
import kotlin.jvm.JvmName
import kotlinx.parcelize.Parcelize

/**
 * 游戏配置
 *
 * 封装单次游戏的所有配置信息，包括网格大小和难度级别。
 * 使用 data class 简化代码，自动生成 equals/hashCode/copy。
 * 使用 @Parcelize 支持 Parcelable 序列化（用于导航传参）。
 *
 * @property size 网格大小，如 "5×5"
 * @property difficulty 难度级别，如 "简单"、"普通"、"困难"
 */
@Parcelize
data class GameConfig(
    val size: String,
    val difficulty: String
) : Parcelable {

    /**
     * 网格维度
     *
     * 从网格大小字符串中提取数字，如 "5×5" -> 5
     */
    val gridDimension: Int
        get() = size.first().digitToInt()

    /**
     * 单元格总数
     *
     * 网格维度的平方，如 5×5 = 25
     */
    val totalCells: Int
        get() = gridDimension * gridDimension

    /**
     * 判断是否为简单模式
     *
     * 简单模式下，点击正确的单元格会显示视觉反馈。
     */
    fun isEasyMode(): Boolean = difficulty == DIFFICULTY_EASY

    /**
     * 判断是否为困难模式
     *
     * 困难模式下，点击错误的单元格会增加时间惩罚。
     */
    fun isHardMode(): Boolean = difficulty == DIFFICULTY_HARD

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
        // 网格大小常量
        const val SIZE_3X3 = "3×3"
        const val SIZE_4X4 = "4×4"
        const val SIZE_5X5 = "5×5"
        const val SIZE_6X6 = "6×6"

        // 难度级别常量
        const val DIFFICULTY_EASY = "简单"
        const val DIFFICULTY_NORMAL = "普通"
        const val DIFFICULTY_HARD = "困难"

        // 默认配置常量
        const val DEFAULT_SIZE = SIZE_5X5
        const val DEFAULT_DIFFICULTY = DIFFICULTY_NORMAL

        // 默认配置
        val DEFAULT = GameConfig(DEFAULT_SIZE, DEFAULT_DIFFICULTY)
    }
}
