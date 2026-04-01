package com.digitallogic.karnaughmap.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digitallogic.karnaughmap.domain.model.SimplificationResult
import com.digitallogic.karnaughmap.ui.mvi.KMapEvent

@Composable
fun StepByStepPanel(
    result: SimplificationResult,
    currentStepIndex: Int,
    onEvent: (KMapEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = result.steps
    val currentStep = steps.getOrNull(currentStepIndex)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "化简步骤 (Step-by-Step)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (steps.isEmpty()) {
                Text(
                    "暂无化简步骤 (No steps available)",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                return@Column
            }

            // Step progress indicator
            Text(
                "步骤 ${currentStepIndex + 1} / ${steps.size}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            val safeProgress = (currentStepIndex + 1).toFloat() / steps.size
            LinearProgressIndicator(
                progress = { safeProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Animated step content
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "step_content"
            ) { step ->
                if (step != null) {
                    // No nested verticalScroll here: this Column is already inside the
                    // outer verticalScroll in MainScreen, so nesting another scroll in the
                    // same direction would cause a layout exception on some Compose versions.
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 80.dp)
                    ) {
                        Text(
                            step.title,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            step.description,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                } else {
                    Text(
                        "暂无步骤内容 (No content)",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 80.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = { onEvent(KMapEvent.PreviousStepClicked) },
                    enabled = currentStepIndex > 0
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
                    Spacer(Modifier.width(4.dp))
                    Text("上一步")
                }

                FilledTonalButton(
                    onClick = { onEvent(KMapEvent.NextStepClicked) },
                    enabled = steps.isNotEmpty() && currentStepIndex < steps.size - 1
                ) {
                    Text("下一步")
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                }
            }
        }
    }
}
