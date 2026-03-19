package com.schultegrid.ui.screens.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.schultegrid.R
import com.schultegrid.domain.model.GameConfig
import com.schultegrid.domain.model.GameState
import com.schultegrid.ui.components.GameGrid
import com.schultegrid.ui.components.PrimaryButton
import com.schultegrid.ui.theme.SchulteGridTheme

/**
 * 游戏屏幕
 *
 * 主要游戏区域，处理游戏交互和状态显示。
 *
 * @param config 游戏配置
 * @param onBack 返回回调
 * @param viewModel 游戏页 ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    config: GameConfig,
    onBack: () -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    // 使用配置初始化游戏
    LaunchedEffect(config) {
        viewModel.initGame(config)
    }

    // 收集 UI 状态
    val uiState by viewModel.uiState.collectAsState()

    SchulteGridTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
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
                // 信息栏
                InfoBar(
                    size = config.getSize(),
                    difficulty = config.getDifficulty(),
                    gameState = uiState.gameState
                )

                when (uiState.gameState) {
                    is GameState.Idle -> {
                        // 就绪状态
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(R.string.ready_message),
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            PrimaryButton(
                                text = stringResource(R.string.start),
                                onClick = { viewModel.startGame() },
                                modifier = Modifier
                                    .width(200.dp)
                                    .align(Alignment.CenterHorizontally)
                            )
                        }
                    }

                    is GameState.Playing -> {
                        // 游戏中状态 - 显示网格
                        val playingState = uiState.gameState as GameState.Playing
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            GameGrid(
                                cells = uiState.gridCells,
                                dimension = uiState.gridDimension,
                                nextExpectedNumber = playingState.nextExpectedNumber,
                                showHighlight = config.isEasyMode(),
                                onCellClick = { number -> viewModel.onCellClick(number) }
                            )
                        }
                    }

                    is GameState.Finished -> {
                        // 完成状态 - 显示结果
                        val finishedState = uiState.gameState as GameState.Finished
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(48.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = stringResource(R.string.complete),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))

                                    Text(
                                        text = String.format("%.2f秒", finishedState.finalTimeSeconds),
                                        style = MaterialTheme.typography.displayLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    if (finishedState.penaltyMs > 0) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = stringResource(R.string.includes_penalty) + ": ${finishedState.penaltyMs}ms",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(32.dp))

                                    PrimaryButton(
                                        text = stringResource(R.string.play_again),
                                        onClick = { viewModel.restartGame() },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    null -> {
                        // 加载状态
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

/**
 * 信息栏组件
 *
 * 显示游戏配置和当前状态。
 *
 * @param size 网格大小
 * @param difficulty 难度级别
 * @param gameState 当前游戏状态
 */
@Composable
fun InfoBar(
    size: String,
    difficulty: String,
    gameState: GameState?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$size · $difficulty",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            when (gameState) {
                is GameState.Playing -> {
                    Text(
                        text = stringResource(R.string.target) + ": ${gameState.nextExpectedNumber}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                is GameState.Finished -> {
                    Text(
                        text = stringResource(R.string.completed),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                else -> {
                    Text(
                        text = stringResource(R.string.ready_to_start),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
