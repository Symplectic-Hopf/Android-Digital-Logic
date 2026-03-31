package com.digitallogic.karnaughmap.ui.mvi

import com.digitallogic.karnaughmap.domain.model.CellState
import com.digitallogic.karnaughmap.domain.model.KarnaughMapData
import com.digitallogic.karnaughmap.domain.model.SimplificationResult

/** All UI state for the K-map screen. */
data class KMapState(
    val variables: Int = 4,
    val kMapData: KarnaughMapData = KarnaughMapData(4),
    val simplificationResult: SimplificationResult? = null,
    val isLoading: Boolean = false,
    val currentStepIndex: Int = 0,
    val showStepByStep: Boolean = false,
    val inputMode: InputMode = InputMode.TRUTH_TABLE
)

enum class InputMode {
    TRUTH_TABLE,    // Click cells to toggle 0/1/X
    MINTERMS        // Enter minterms and don't cares as text
}

/** All user actions/events. */
sealed class KMapEvent {
    data class VariablesChanged(val variables: Int) : KMapEvent()
    data class CellClicked(val mintermIndex: Int) : KMapEvent()
    object SimplifyClicked : KMapEvent()
    object ClearClicked : KMapEvent()
    object NextStepClicked : KMapEvent()
    object PreviousStepClicked : KMapEvent()
    object ToggleStepByStep : KMapEvent()
    data class InputModeChanged(val mode: InputMode) : KMapEvent()
    data class MintermTextChanged(val minterms: String) : KMapEvent()
    data class DontCareTextChanged(val dontCares: String) : KMapEvent()
}
