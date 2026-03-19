package com.schultegrid.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.schultegrid.R
import com.schultegrid.domain.model.GameConfig
import com.schultegrid.ui.components.AppTitle
import com.schultegrid.ui.components.PrimaryButton
import com.schultegrid.ui.components.SettingOptionCard
import com.schultegrid.ui.theme.SchulteGridTheme

/**
 * 主页屏幕
 *
 * 游戏设置和启动页面，用户可以选择网格大小和难度级别。
 *
 * @param onStartGame 开始游戏回调
 * @param onHistoryClick 历史记录点击回调
 * @param ViewModel 主页 ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartGame: (GameConfig) -> Unit,
    onHistoryClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showSizeDialog by remember { mutableStateOf(false) }
    var showDifficultyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    SchulteGridTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    actions = {
                        IconButton(onClick = onHistoryClick) {
                            Icon(Icons.Default.List, contentDescription = stringResource(R.string.history))
                        }
                        IconButton(onClick = { showAboutDialog = true }) {
                            Icon(Icons.Default.Info, contentDescription = stringResource(R.string.about))
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                AppTitle(
                    title = stringResource(R.string.app_name),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.tagline),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 网格大小设置
                SettingOptionCard(
                    title = stringResource(R.string.setting_size),
                    currentValue = uiState.gameConfig?.getSize() ?: "",
                    onClick = { showSizeDialog = true }
                )

                // 难度设置
                SettingOptionCard(
                    title = stringResource(R.string.setting_difficulty),
                    currentValue = uiState.gameConfig?.getDifficulty() ?: "",
                    onClick = { showDifficultyDialog = true }
                )

                Spacer(modifier = Modifier.weight(1f))

                PrimaryButton(
                    text = stringResource(R.string.start_game),
                    onClick = {
                        uiState.gameConfig?.let { onStartGame(it) }
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .align(Alignment.CenterHorizontally),
                    enabled = !uiState.isLoading && uiState.gameConfig != null
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // 网格大小选择对话框
    if (showSizeDialog) {
        SizeSelectionDialog(
            currentSize = uiState.gameConfig?.getSize() ?: "",
            onDismiss = { showSizeDialog = false },
            onSizeSelected = { size ->
                viewModel.updateSize(size)
                showSizeDialog = false
            }
        )
    }

    // 难度选择对话框
    if (showDifficultyDialog) {
        DifficultySelectionDialog(
            currentDifficulty = uiState.gameConfig?.getDifficulty() ?: "",
            onDismiss = { showDifficultyDialog = false },
            onDifficultySelected = { difficulty ->
                viewModel.updateDifficulty(difficulty)
                showDifficultyDialog = false
            }
        )
    }

    // 关于对话框
    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { showAboutDialog = false }
        )
    }
}

/**
 * 网格大小选择对话框
 *
 * @param currentSize 当前选中的大小
 * @param onDismiss 关闭对话框回调
 * @param onSizeSelected 选择大小回调
 */
@Composable
fun SizeSelectionDialog(
    currentSize: String,
    onDismiss: () -> Unit,
    onSizeSelected: (String) -> Unit
) {
    val sizes = listOf("3×3", "4×4", "5×5", "6×6")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.select_size_title),
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                sizes.forEach { size ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 4.dp)
                            .clickable { onSizeSelected(size) },
                        shape = MaterialTheme.shapes.small,
                        colors = CardDefaults.cardColors(
                            containerColor = if (size == currentSize) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Text(
                            text = size,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            color = if (size == currentSize) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

/**
 * 难度选择对话框
 *
 * @param currentDifficulty 当前选中的难度
 * @param onDismiss 关闭对话框回调
 * @param onDifficultySelected 选择难度回调
 */
@Composable
fun DifficultySelectionDialog(
    currentDifficulty: String,
    onDismiss: () -> Unit,
    onDifficultySelected: (String) -> Unit
) {
    val difficulties = listOf(
        stringResource(R.string.difficulty_easy),
        stringResource(R.string.difficulty_normal),
        stringResource(R.string.difficulty_hard)
    )
    val descriptions = listOf(
        stringResource(R.string.easy_mode_hint),
        stringResource(R.string.normal_mode_desc),
        stringResource(R.string.hard_mode_hint)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.select_difficulty_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                difficulties.forEachIndexed { index, difficulty ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 4.dp)
                            .clickable { onDifficultySelected(difficulty) },
                        shape = MaterialTheme.shapes.small,
                        colors = CardDefaults.cardColors(
                            containerColor = if (difficulty == currentDifficulty) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = difficulty,
                                style = MaterialTheme.typography.titleLarge,
                                color = if (difficulty == currentDifficulty) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = descriptions[index],
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (difficulty == currentDifficulty) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

/**
 * 关于对话框
 *
 * @param onDismiss 关闭对话框回调
 */
@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.about_title),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = stringResource(R.string.about_description),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.about_author),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.about_version),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.ok))
            }
        }
    )
}
