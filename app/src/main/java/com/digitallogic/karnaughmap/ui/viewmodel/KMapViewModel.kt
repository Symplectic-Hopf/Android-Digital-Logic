package com.digitallogic.karnaughmap.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digitallogic.karnaughmap.domain.algorithm.QuineMcCluskey
import com.digitallogic.karnaughmap.domain.model.CellState
import com.digitallogic.karnaughmap.domain.model.KarnaughMapData
import com.digitallogic.karnaughmap.ui.mvi.InputMode
import com.digitallogic.karnaughmap.ui.mvi.KMapEvent
import com.digitallogic.karnaughmap.ui.mvi.KMapState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KMapViewModel : ViewModel() {

    private val _state = MutableStateFlow(KMapState())
    val state: StateFlow<KMapState> = _state.asStateFlow()

    fun onEvent(event: KMapEvent) {
        when (event) {
            is KMapEvent.VariablesChanged -> {
                val newVars = event.variables.coerceIn(2, 6)
                _state.value = KMapState(variables = newVars, kMapData = KarnaughMapData(newVars))
            }
            is KMapEvent.CellClicked -> {
                val current = _state.value
                val currentState = current.kMapData.getState(event.mintermIndex)
                val nextState = when (currentState) {
                    CellState.ZERO -> CellState.ONE
                    CellState.ONE -> CellState.DONT_CARE
                    CellState.DONT_CARE -> CellState.ZERO
                }
                _state.value = current.copy(
                    kMapData = current.kMapData.withUpdatedCell(event.mintermIndex, nextState),
                    simplificationResult = null
                )
            }
            is KMapEvent.SimplifyClicked -> {
                simplify()
            }
            is KMapEvent.ClearClicked -> {
                val vars = _state.value.variables
                _state.value = KMapState(variables = vars, kMapData = KarnaughMapData(vars))
            }
            is KMapEvent.NextStepClicked -> {
                val current = _state.value
                val maxStep = (current.simplificationResult?.steps?.size ?: 1) - 1
                _state.value = current.copy(
                    currentStepIndex = (current.currentStepIndex + 1).coerceAtMost(maxStep)
                )
            }
            is KMapEvent.PreviousStepClicked -> {
                val current = _state.value
                _state.value = current.copy(
                    currentStepIndex = (current.currentStepIndex - 1).coerceAtLeast(0)
                )
            }
            is KMapEvent.ToggleStepByStep -> {
                _state.value = _state.value.copy(
                    showStepByStep = !_state.value.showStepByStep,
                    currentStepIndex = 0
                )
            }
            is KMapEvent.InputModeChanged -> {
                _state.value = _state.value.copy(inputMode = event.mode)
            }
            is KMapEvent.MintermTextChanged -> {
                // Parse minterms from text and update kmap
                parseMintermText(event.minterms)
            }
            is KMapEvent.DontCareTextChanged -> {
                parseDontCareText(event.dontCares)
            }
        }
    }

    private fun simplify() {
        val current = _state.value
        _state.value = current.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val qm = QuineMcCluskey(current.variables)
                val result = qm.simplify(current.kMapData.minterms, current.kMapData.dontCares)
                _state.value = _state.value.copy(
                    isLoading = false,
                    simplificationResult = result,
                    currentStepIndex = 0
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    private fun parseMintermText(text: String) {
        val current = _state.value
        val newMinterms = text.split(",", " ").mapNotNull { it.trim().toIntOrNull() }
            .filter { it in 0 until (1 shl current.variables) }
            .toSet()
        // Rebuild cells: preserve DONT_CARE states, replace all ONE states with new minterms
        val newCells = current.kMapData.cells
            .filterValues { it == CellState.DONT_CARE }
            .toMutableMap()
        newMinterms.forEach { newCells[it] = CellState.ONE }
        _state.value = current.copy(
            kMapData = current.kMapData.copy(cells = newCells),
            simplificationResult = null
        )
    }

    private fun parseDontCareText(text: String) {
        val current = _state.value
        val newDontCares = text.split(",", " ").mapNotNull { it.trim().toIntOrNull() }
            .filter { it in 0 until (1 shl current.variables) }
            .toSet()
        // Rebuild cells: preserve ONE states, replace all DONT_CARE states with new don't-cares
        val newCells = current.kMapData.cells
            .filterValues { it == CellState.ONE }
            .toMutableMap()
        newDontCares.forEach { newCells[it] = CellState.DONT_CARE }
        _state.value = current.copy(
            kMapData = current.kMapData.copy(cells = newCells),
            simplificationResult = null
        )
    }
}
