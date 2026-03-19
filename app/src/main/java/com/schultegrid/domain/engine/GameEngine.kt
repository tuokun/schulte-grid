package com.schultegrid.domain.engine

import androidx.annotation.NonNull
import com.schultegrid.domain.model.GameConfig
import com.schultegrid.domain.model.GameState
import com.schultegrid.domain.model.GridCell
import kotlin.random.Random

/**
 * 游戏引擎
 *
 * 核心游戏逻辑控制器，负责：
 * - 管理游戏状态转换
 * - 处理单元格点击逻辑
 * - 计算游戏用时和惩罚
 * - 生成和管理网格布局
 *
 * @property config 游戏配置
 */
class GameEngine(
    @NonNull private val config: GameConfig
) {
    companion object {
        /** 困难模式每次错误点击的惩罚时间（毫秒） */
        private const val HARD_MODE_PENALTY_MS = 100L
    }

    /** 网格单元格列表 */
    private val cells: MutableList<GridCell>

    /** 单元格总数 */
    private val totalCells: Int = config.totalCells

    /** 当前游戏状态 */
    var gameState: GameState = GameState.initial(totalCells)
        private set

    /** 游戏开始时间（毫秒） */
    private var startTime: Long = 0
        private set

    /** 累计惩罚时间（毫秒） */
    private var penaltyMs: Long = 0
        private set

    /** 是否已点击第一个数字（困难模式用） */
    private var hasClickedFirstNumber: Boolean = false
        private set

    /** 随机数生成器（类级别，避免重复创建） */
    private val random = Random.Default

    init {
        cells = GridCell.generateShuffled(totalCells).toMutableList()
    }

    /**
     * 获取网格单元格
     *
     * @return 单元格列表的副本
     */
    fun getCells(): List<GridCell> = cells.toList()

    /**
     * 获取网格维度
     *
     * @return 网格维度（如 5 表示 5×5）
     */
    fun getGridDimension(): Int = config.gridDimension

    /**
     * 开始游戏
     *
     * 初始化游戏状态，重置所有单元格，记录开始时间。
     */
    fun startGame() {
        startTime = System.currentTimeMillis()
        penaltyMs = 0
        hasClickedFirstNumber = false
        gameState = GameState.playing(1, 0, 0, totalCells)

        // 重新打乱并重置所有单元格状态
        cells.shuffle(random)
        for (i in cells.indices) {
            cells[i] = cells[i].reset()
        }
    }

    /**
     * 处理单元格点击
     *
     * 验证点击是否正确，更新游戏状态。
     * 在困难模式下，错误点击会增加惩罚时间。
     *
     * @param cellNumber 被点击的单元格数字
     * @return 游戏是否完成
     */
    fun onCellClicked(cellNumber: Int): Boolean {
        // 仅在游戏进行中处理点击
        val playingState = gameState as? GameState.Playing ?: return false

        when {
            // 正确点击
            cellNumber == playingState.nextExpectedNumber -> {
                handleCorrectClick(cellNumber)
            }
            // 错误点击且是困难模式
            config.isHardMode() -> {
                handleWrongClick(playingState)
            }
        }

        return gameState.isFinished
    }

    /**
     * 处理正确点击
     */
    private fun handleCorrectClick(cellNumber: Int) {
        // 找到并标记单元格
        cells.find { it.number == cellNumber }?.click()

        // 困难模式：点击第一个数字后隐藏所有数字
        if (config.isHardMode() && cellNumber == 1 && !hasClickedFirstNumber) {
            hasClickedFirstNumber = true
            for (i in cells.indices) {
                cells[i] = cells[i].copy(isVisible = false)
            }
        }

        // 检查游戏是否完成
        if (cellNumber >= totalCells) {
            finishGame()
        } else {
            // 更新为下一个数字的状态
            val elapsed = System.currentTimeMillis() - startTime
            gameState = GameState.playing(
                nextExpected = cellNumber + 1,
                elapsed = elapsed,
                penalty = penaltyMs,
                totalCells = totalCells
            )
        }
    }

    /**
     * 处理错误点击（困难模式）
     */
    private fun handleWrongClick(playingState: GameState.Playing) {
        penaltyMs += HARD_MODE_PENALTY_MS
        val elapsed = System.currentTimeMillis() - startTime
        gameState = GameState.playing(
            nextExpected = playingState.nextExpectedNumber,
            elapsed = elapsed,
            penalty = penaltyMs,
            totalCells = totalCells
        )
    }

    /**
     * 完成游戏
     */
    private fun finishGame() {
        val elapsed = System.currentTimeMillis() - startTime
        gameState = GameState.finished(
            elapsed = elapsed,
            penalty = penaltyMs,
            totalCells = totalCells
        )
    }

    /**
     * 获取当前用时
     *
     * @return 已用时间（毫秒）
     */
    fun getElapsedTime(): Long {
        return when (gameState) {
            is GameState.Finished -> gameState.elapsedTimeMs
            is GameState.Playing -> System.currentTimeMillis() - startTime
            else -> 0
        }
    }

    /**
     * 获取最终成绩
     *
     * @return 完成时间（秒），游戏未完成返回 0
     */
    fun getFinalScore(): Float {
        return when (val state = gameState) {
            is GameState.Finished -> state.finalTimeSeconds
            else -> 0f
        }
    }

    /**
     * 重置游戏
     *
     * 重新打乱单元格，重置状态和惩罚。
     */
    fun reset() {
        cells.shuffle(random)
        for (i in cells.indices) {
            cells[i] = cells[i].reset()
        }
        gameState = GameState.initial(totalCells)
        penaltyMs = 0
    }

    /**
     * 获取游戏配置
     */
    fun getConfig(): GameConfig = config
}
