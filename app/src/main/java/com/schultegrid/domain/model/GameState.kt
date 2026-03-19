package com.schultegrid.domain.model

import kotlin.jvm.JvmName

/**
 * 游戏状态
 *
 * 使用密封类（sealed class）表示游戏的不同状态，确保类型安全。
 * 每种状态包含特定的数据和属性。
 *
 * @property nextExpectedNumber 下一个期望点击的数字
 * @property elapsedTimeMs 已用时间（毫秒）
 * @property penaltyMs 惩罚时间（毫秒）
 * @property totalCells 单元格总数
 */
sealed class GameState {
    abstract val nextExpectedNumber: Int
    abstract val elapsedTimeMs: Long
    abstract val penaltyMs: Long
    abstract val totalCells: Int

    /**
     * 判断游戏是否已完成
     */
    val isFinished: Boolean
        get() = this is Finished

    /**
     * 判断游戏是否进行中
     */
    val isPlaying: Boolean
        get() = this is Playing

    /**
     * 获取最终时间（秒）
     *
     * 包括实际用时和惩罚时间。
     */
    @JvmName("calculateFinalTimeSeconds")
    fun getFinalTimeSeconds(): Float = (elapsedTimeMs + penaltyMs) / 1000f

    /**
     * 游戏空闲状态
     *
     * 游戏尚未开始或已重置。
     *
     * @property totalCells 单元格总数
     */
    data class Idle(
        override val totalCells: Int
    ) : GameState() {
        override val nextExpectedNumber: Int = 1
        override val elapsedTimeMs: Long = 0
        override val penaltyMs: Long = 0
    }

    /**
     * 游戏进行中状态
     *
     * 游戏正在进行，用户正在点击单元格。
     *
     * @property nextExpectedNumber 下一个期望点击的数字
     * @property elapsedTimeMs 已用时间（毫秒）
     * @property penaltyMs 惩罚时间（毫秒）
     * @property totalCells 单元格总数
     */
    data class Playing(
        override val nextExpectedNumber: Int,
        override val elapsedTimeMs: Long,
        override val penaltyMs: Long,
        override val totalCells: Int
    ) : GameState()

    /**
     * 游戏完成状态
     *
     * 游戏已完成，显示最终成绩。
     *
     * @property finalTimeSeconds 最终时间（秒）
     * @property elapsedTimeMs 实际用时（毫秒）
     * @property penaltyMs 惩罚时间（毫秒）
     * @property totalCells 单元格总数
     */
    data class Finished(
        val finalTimeSeconds: Float,
        override val elapsedTimeMs: Long,
        override val penaltyMs: Long,
        override val totalCells: Int
    ) : GameState() {
        override val nextExpectedNumber: Int = totalCells + 1
    }

    companion object {
        /**
         * 创建初始状态
         *
         * @param totalCells 单元格总数
         * @return 空闲状态
         */
        fun initial(totalCells: Int): GameState = Idle(totalCells)

        /**
         * 创建进行中状态
         *
         * @param nextExpected 下一个期望数字
         * @param elapsed 已用时间
         * @param penalty 惩罚时间
         * @param totalCells 单元格总数
         * @return 进行中状态
         */
        fun playing(
            nextExpected: Int,
            elapsed: Long,
            penalty: Long,
            totalCells: Int
        ): GameState = Playing(nextExpected, elapsed, penalty, totalCells)

        /**
         * 创建完成状态
         *
         * @param elapsed 实际用时
         * @param penalty 惩罚时间
         * @param totalCells 单元格总数
         * @return 完成状态
         */
        fun finished(
            elapsed: Long,
            penalty: Long,
            totalCells: Int
        ): GameState = Finished(
            finalTimeSeconds = (elapsed + penalty) / 1000f,
            elapsedTimeMs = elapsed,
            penaltyMs = penalty,
            totalCells = totalCells
        )
    }
}
