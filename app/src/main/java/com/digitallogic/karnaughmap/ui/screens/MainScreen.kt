package com.digitallogic.karnaughmap.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digitallogic.karnaughmap.ui.components.KarnaughMapGrid
import com.digitallogic.karnaughmap.ui.components.ResultPanel
import com.digitallogic.karnaughmap.ui.components.StepByStepPanel
import com.digitallogic.karnaughmap.ui.mvi.KMapEvent
import com.digitallogic.karnaughmap.ui.mvi.KMapState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    state: KMapState,
    onEvent: (KMapEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("数字逻辑卡诺图化简", fontSize = 16.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Variables selector
            VariablesSelector(
                selectedVars = state.variables,
                onVarsChanged = { onEvent(KMapEvent.VariablesChanged(it)) }
            )

            // K-map grid
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "卡诺图 (Karnaugh Map)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        "点击单元格切换状态：0 → 1 → X",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Box(
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        val highlightedImplicants = if (state.showStepByStep) {
                            state.simplificationResult?.steps
                                ?.getOrNull(state.currentStepIndex)
                                ?.highlightedImplicants ?: emptyList()
                        } else {
                            state.simplificationResult?.selectedImplicants ?: emptyList()
                        }

                        KarnaughMapGrid(
                            kMapData = state.kMapData,
                            highlightedImplicants = highlightedImplicants,
                            onCellClick = { minterm -> onEvent(KMapEvent.CellClicked(minterm)) },
                            cellSize = when (state.variables) {
                                2, 3 -> 60.dp
                                4 -> 52.dp
                                5 -> 44.dp
                                6 -> 40.dp
                                else -> 48.dp
                            }
                        )
                    }
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onEvent(KMapEvent.SimplifyClicked) },
                    modifier = Modifier.weight(1f),
                    enabled = !state.isLoading && state.kMapData.minterms.isNotEmpty()
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("化简 (Simplify)")
                    }
                }

                OutlinedButton(
                    onClick = { onEvent(KMapEvent.ClearClicked) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("清空 (Clear)")
                }
            }

            // Results
            state.simplificationResult?.let { result ->
                ResultPanel(result = result)

                // Step-by-step toggle button
                OutlinedButton(
                    onClick = { onEvent(KMapEvent.ToggleStepByStep) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (state.showStepByStep) "隐藏步骤 (Hide Steps)"
                        else "显示化简步骤 (Show Steps)"
                    )
                }

                // Step-by-step panel
                if (state.showStepByStep && result.steps.isNotEmpty()) {
                    StepByStepPanel(
                        result = result,
                        currentStepIndex = state.currentStepIndex,
                        onEvent = onEvent
                    )
                } else if (state.showStepByStep) {
                    Text(
                        "暂无化简步骤 (No simplification steps)",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun VariablesSelector(
    selectedVars: Int,
    onVarsChanged: (Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                "变量数 (Variables): $selectedVars",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (vars in 2..6) {
                    FilterChip(
                        selected = selectedVars == vars,
                        onClick = { onVarsChanged(vars) },
                        label = { Text("$vars") }
                    )
                }
            }
        }
    }
}
