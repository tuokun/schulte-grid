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
 * @param nextExpectedNumber The next expected number to tap
 * @param onCellClick Callback when a cell is clicked
 * @param isEasyMode Whether easy mode is enabled (shows clicked state)
 */
@Composable
fun GameGrid(
    cells: List<GridCell>,
    dimension: Int,
    nextExpectedNumber: Int,
    onCellClick: (Int) -> Unit,
    isEasyMode: Boolean = false
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
                number = cell.number,
                isClicked = cell.isClicked && isEasyMode,
                isNextExpected = cell.number == nextExpectedNumber,
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
    number: Int,
    isClicked: Boolean,
    isNextExpected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isClicked -> GridCellClicked
        else -> GridCellDefault
    }

    val borderColor = if (isNextExpected) {
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
                width = if (isNextExpected) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            color = Color.Black,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
