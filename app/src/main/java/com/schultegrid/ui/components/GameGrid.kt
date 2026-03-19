package com.schultegrid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.schultegrid.domain.model.GridCell
import com.schultegrid.ui.theme.GridCellClicked
import com.schultegrid.ui.theme.GridCellDefault

/**
 * Game grid component for Schulte Grid
 *
 * @param cells List of grid cells to display
 * @param dimension Grid dimension (e.g., 5 for 5x5)
 * @param nextExpectedNumber The next number that should be clicked
 * @param showHighlight Whether to show highlight border on next expected number (easy mode only)
 * @param onCellClick Callback when a cell is clicked
 */
@Composable
fun GameGrid(
    cells: List<GridCell>,
    dimension: Int,
    nextExpectedNumber: Int,
    showHighlight: Boolean,
    onCellClick: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(dimension),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(cells.size) { index ->
            val cell = cells[index]
            GridCellItem(
                cell = cell,
                nextExpectedNumber = nextExpectedNumber,
                showHighlight = showHighlight,
                onClick = { onCellClick(cell.number) }
            )
        }
    }
}

/**
 * Individual grid cell item
 */
@Composable
fun GridCellItem(
    cell: GridCell,
    nextExpectedNumber: Int,
    showHighlight: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        cell.isClicked -> GridCellClicked
        else -> GridCellDefault
    }

    // 只在简单模式且该单元格是下一个目标数字时显示高亮
    val isTargetNumber = cell.number == nextExpectedNumber
    val borderColor = if (showHighlight && isTargetNumber && !cell.isClicked) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = if (showHighlight) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (cell.isVisible) {
            Text(
                text = cell.number.toString(),
                color = Color.Black,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
