package com.digitallogic.karnaughmap.domain.model

/**
 * Cell state in the K-map.
 */
enum class CellState {
    ZERO,       // Logic 0
    ONE,        // Logic 1
    DONT_CARE   // Don't care (X)
}

/**
 * Holds the K-map data for a given number of variables.
 * @param variables Number of variables (2-6).
 * @param cells Map from minterm index to cell state.
 */
data class KarnaughMapData(
    val variables: Int,
    val cells: Map<Int, CellState> = emptyMap()
) {
    val totalCells: Int get() = 1 shl variables

    /** Returns minterms (cells with value 1). */
    val minterms: Set<Int> get() = cells.entries
        .filter { it.value == CellState.ONE }
        .map { it.key }
        .toSet()

    /** Returns maxterms (cells with value 0). */
    val maxterms: Set<Int> get() = cells.entries
        .filter { it.value == CellState.ZERO }
        .map { it.key }
        .toSet()

    /** Returns don't care indices. */
    val dontCares: Set<Int> get() = cells.entries
        .filter { it.value == CellState.DONT_CARE }
        .map { it.key }
        .toSet()

    fun getState(minterm: Int): CellState = cells[minterm] ?: CellState.ZERO

    fun withUpdatedCell(minterm: Int, state: CellState): KarnaughMapData =
        copy(cells = cells + (minterm to state))
}
