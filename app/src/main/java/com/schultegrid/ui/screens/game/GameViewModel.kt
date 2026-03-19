package com.schultegrid.ui.screens.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schultegrid.data.repository.GameRepository
import com.schultegrid.domain.engine.GameEngine
import com.schultegrid.domain.model.GameConfig
import com.schultegrid.domain.model.GameState
import com.schultegrid.domain.model.GridCell
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 游戏页 UI 状态
 *
 * 包含游戏页面需要显示的所有状态数据。
 *
 * @property gameState 当前游戏状态
 * @property gridCells 网格单元格列表
 * @property gridDimension 网格维度
 * @property isLoading 是否正在处理
 * @property error 错误信息
 * @property recordSaved 记录是否已保存
 */
data class GameUiState(
    val gameState: GameState? = null,
    val gridCells: List<GridCell> = emptyList(),
    val gridDimension: Int = 5,
    val isLoading: Boolean = false,
    val error: String? = null,
    val recordSaved: Boolean = false
)

/**
 * 游戏页 ViewModel
 *
 * 管理游戏页的业务逻辑和状态，使用 Kotlin Flow 和 Coroutines。
 *
 * @property repository 游戏数据仓库
 */
@HiltViewModel
class GameViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    /**
     * UI 状态流
     */
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    /**
     * 游戏引擎实例
     */
    private var engine: GameEngine? = null
        private set

    /**
     * 当前游戏配置
     */
    private var config: GameConfig? = null
        private set

    /**
     * 初始化游戏
     *
     * 使用指定配置创建新的游戏引擎。
     *
     * @param config 游戏配置
     */
    fun initGame(config: GameConfig) {
        this.config = config
        this.engine = GameEngine(config)

        val engine = this.engine
        if (engine != null) {
            _uiState.value = _uiState.value.copy(
                gameState = engine.gameState,
                gridCells = engine.getCells(),
                gridDimension = engine.getGridDimension(),
                recordSaved = false
            )
        }
    }

    /**
     * 开始游戏
     *
     * 启动游戏计时器和状态管理。
     */
    fun startGame() {
        try {
            val engine = this.engine
            if (engine != null) {
                engine.startGame()
                updateState()
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "启动游戏失败: ${e.message}"
            )
        }
    }

    /**
     * 处理单元格点击
     *
     * 验证点击并更新游戏状态，游戏完成后保存记录。
     *
     * @param number 被点击的单元格数字
     */
    fun onCellClick(number: Int) {
        engine?.onCellClicked(number)
        updateState()

        // 游戏完成后保存记录
        if (_uiState.value.gameState?.isFinished == true) {
            saveRecord()
        }
    }

    /**
     * 重新开始游戏
     *
     * 重置游戏引擎并重新开始。
     */
    fun restartGame() {
        engine?.reset()
        engine?.startGame()
        updateState()

        _uiState.value = _uiState.value.copy(recordSaved = false)
    }

    /**
     * 保存游戏记录
     *
     * 将游戏成绩保存到数据库。
     */
    private fun saveRecord() {
        val engine = this.engine
        val config = this.config
        if (engine == null || config == null) return

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                repository.saveRecord(
                    score = engine.getFinalScore(),
                    size = config.size,
                    difficulty = config.difficulty
                ).first()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    recordSaved = true
                )
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "保存记录失败: ${error.message}"
                )
            }
        }
    }

    /**
     * 更新游戏状态
     *
     * 从游戏引擎同步状态到 UI。
     */
    private fun updateState() {
        try {
            val engine = this.engine ?: return
            _uiState.value = _uiState.value.copy(
                gameState = engine.gameState,
                gridCells = engine.getCells()
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "更新状态失败: ${e.message}"
            )
        }
    }
}
