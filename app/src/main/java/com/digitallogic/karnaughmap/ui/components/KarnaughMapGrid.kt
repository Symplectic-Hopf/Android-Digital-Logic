package com.digitallogic.karnaughmap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digitallogic.karnaughmap.domain.algorithm.KarnaughMapLayout
import com.digitallogic.karnaughmap.domain.model.CellState
import com.digitallogic.karnaughmap.domain.model.Implicant
import com.digitallogic.karnaughmap.domain.model.KarnaughMapData
import com.digitallogic.karnaughmap.ui.theme.GroupColors

/**
 * Dynamic K-map grid component.
 * Renders a Karnaugh map with clickable cells and group overlays.
 */
@Composable
fun KarnaughMapGrid(
    kMapData: KarnaughMapData,
    highlightedImplicants: List<Implicant> = emptyList(),
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    cellSize: Dp = 48.dp
) {
    val variables = kMapData.variables
    val rows = KarnaughMapLayout.getRows(variables)
    val cols = KarnaughMapLayout.getCols(variables)
    val rowHeaders = KarnaughMapLayout.getRowHeaders(variables)
    val colHeaders = KarnaughMapLayout.getColHeaders(variables)

    Column(modifier = modifier) {
        // Column headers row
        Row {
            // Empty top-left corner
            Spacer(modifier = Modifier.size(cellSize + 8.dp, cellSize))
            colHeaders.forEachIndexed { colIdx, header ->
                Box(
                    modifier = Modifier
                        .size(cellSize, cellSize)
                        .padding(1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = header,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 2
                    )
                }
            }
        }

        // Data rows
        for (row in 0 until rows) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Row header
                Box(
                    modifier = Modifier
                        .width(cellSize + 8.dp)
                        .height(cellSize),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = rowHeaders.getOrNull(row) ?: "",
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 2
                    )
                }

                // Cells
                for (col in 0 until cols) {
                    val mintermIndex = KarnaughMapLayout.getMintermIndex(variables, row, col)
                    val cellState = kMapData.getState(mintermIndex)

                    // Find which implicants cover this cell
                    val coveringImplicantIndices = highlightedImplicants.mapIndexedNotNull { idx, imp ->
                        if (imp.covers(mintermIndex)) idx else null
                    }

                    KMapCell(
                        state = cellState,
                        mintermIndex = mintermIndex,
                        groupColorIndices = coveringImplicantIndices,
                        onClick = { onCellClick(mintermIndex) },
                        cellSize = cellSize
                    )
                }
            }
        }
    }
}

@Composable
private fun KMapCell(
    state: CellState,
    mintermIndex: Int,
    groupColorIndices: List<Int>,
    onClick: () -> Unit,
    cellSize: Dp
) {
    val bgColor = when (state) {
        CellState.ONE -> MaterialTheme.colorScheme.primaryContainer
        CellState.ZERO -> MaterialTheme.colorScheme.surface
        CellState.DONT_CARE -> MaterialTheme.colorScheme.secondaryContainer
    }

    Box(
        modifier = Modifier
            .size(cellSize)
            .padding(1.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Draw group overlays
        if (groupColorIndices.isNotEmpty()) {
            groupColorIndices.take(3).forEachIndexed { i, colorIdx ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding((i * 2).dp)
                        .background(
                            GroupColors[colorIdx % GroupColors.size].copy(alpha = 0.3f),
                            RoundedCornerShape(4.dp)
                        )
                )
            }
        }

        // Minterm index (small)
        Text(
            text = mintermIndex.toString(),
            fontSize = 8.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(2.dp)
        )

        // Cell value
        val valueText = when (state) {
            CellState.ZERO -> "0"
            CellState.ONE -> "1"
            CellState.DONT_CARE -> "X"
        }
        Text(
            text = valueText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = when (state) {
                CellState.ONE -> MaterialTheme.colorScheme.onPrimaryContainer
                CellState.ZERO -> MaterialTheme.colorScheme.onSurface
                CellState.DONT_CARE -> MaterialTheme.colorScheme.onSecondaryContainer
            }
        )
    }
}
