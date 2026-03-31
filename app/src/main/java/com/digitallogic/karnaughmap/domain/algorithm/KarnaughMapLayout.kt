package com.digitallogic.karnaughmap.domain.algorithm

/**
 * Provides K-map grid layout helpers for 2-6 variables.
 *
 * K-map dimensions:
 *   2 vars: 2x2  (rows=AB, cols not used... actually rows=A, cols=B)
 *   3 vars: 2x4  (rows=A, cols=BC in Gray code)
 *   4 vars: 4x4  (rows=AB, cols=CD in Gray code)
 *   5 vars: 4x8  (rows=AB, cols=CDE in Gray code)
 *   6 vars: 8x8  (rows=ABC, cols=DEF in Gray code)
 *
 * Gray code sequences:
 *   2-bit: 00, 01, 11, 10
 *   3-bit: 000, 001, 011, 010, 110, 111, 101, 100
 */
object KarnaughMapLayout {

    private val GRAY2 = intArrayOf(0, 1, 3, 2)
    private val GRAY3 = intArrayOf(0, 1, 3, 2, 6, 7, 5, 4)

    fun getRows(variables: Int): Int = when (variables) {
        2 -> 2
        3 -> 2
        4 -> 4
        5 -> 4
        6 -> 8
        else -> throw IllegalArgumentException("Variables must be 2-6")
    }

    fun getCols(variables: Int): Int = when (variables) {
        2 -> 2
        3 -> 4
        4 -> 4
        5 -> 8
        6 -> 8
        else -> throw IllegalArgumentException("Variables must be 2-6")
    }

    fun getRowBits(variables: Int): Int = when (variables) {
        2 -> 1
        3 -> 1
        4 -> 2
        5 -> 2
        6 -> 3
        else -> throw IllegalArgumentException("Variables must be 2-6")
    }

    fun getColBits(variables: Int): Int = when (variables) {
        2 -> 1
        3 -> 2
        4 -> 2
        5 -> 3
        6 -> 3
        else -> throw IllegalArgumentException("Variables must be 2-6")
    }

    fun getRowGrayCode(variables: Int): IntArray = when (getRowBits(variables)) {
        1 -> intArrayOf(0, 1)
        2 -> GRAY2
        3 -> GRAY3
        else -> throw IllegalArgumentException()
    }

    fun getColGrayCode(variables: Int): IntArray = when (getColBits(variables)) {
        1 -> intArrayOf(0, 1)
        2 -> GRAY2
        3 -> GRAY3
        else -> throw IllegalArgumentException()
    }

    /**
     * Returns the minterm index for a given (row, col) in the K-map grid.
     * MSBs are in rows, LSBs are in cols.
     */
    fun getMintermIndex(variables: Int, row: Int, col: Int): Int {
        val rowGray = getRowGrayCode(variables)
        val colGray = getColGrayCode(variables)
        val colBits = getColBits(variables)
        val rowVal = rowGray[row]
        val colVal = colGray[col]
        return (rowVal shl colBits) or colVal
    }

    /**
     * Returns (row, col) position for a given minterm index.
     */
    fun getPosition(variables: Int, minterm: Int): Pair<Int, Int> {
        val rows = getRows(variables)
        val cols = getCols(variables)
        val rowGray = getRowGrayCode(variables)
        val colGray = getColGrayCode(variables)
        val colBits = getColBits(variables)
        val rowVal = minterm ushr colBits
        val colVal = minterm and ((1 shl colBits) - 1)
        val row = rowGray.indexOf(rowVal).takeIf { it >= 0 } ?: 0
        val col = colGray.indexOf(colVal).takeIf { it >= 0 } ?: 0
        return Pair(row.coerceIn(0, rows - 1), col.coerceIn(0, cols - 1))
    }

    /** Returns row header labels for the K-map. */
    fun getRowHeaders(variables: Int): List<String> {
        return when (variables) {
            2 -> listOf("A=0", "A=1")
            3 -> listOf("A=0", "A=1")
            4 -> GRAY2.map { g ->
                val a = (g shr 1) and 1
                val b = g and 1
                "AB=${a}${b}"
            }
            5 -> GRAY2.map { g ->
                val a = (g shr 1) and 1
                val b = g and 1
                "AB=${a}${b}"
            }
            6 -> GRAY3.map { g ->
                val a = (g shr 2) and 1
                val b = (g shr 1) and 1
                val c = g and 1
                "ABC=${a}${b}${c}"
            }
            else -> emptyList()
        }
    }

    /** Returns column header labels for the K-map. */
    fun getColHeaders(variables: Int): List<String> {
        return when (variables) {
            2 -> listOf("B=0", "B=1")
            3 -> GRAY2.map { g ->
                val b = (g shr 1) and 1
                val c = g and 1
                "BC=${b}${c}"
            }
            4 -> GRAY2.map { g ->
                val c = (g shr 1) and 1
                val d = g and 1
                "CD=${c}${d}"
            }
            5 -> GRAY3.map { g ->
                val c = (g shr 2) and 1
                val d = (g shr 1) and 1
                val e = g and 1
                "CDE=${c}${d}${e}"
            }
            6 -> GRAY3.map { g ->
                val d = (g shr 2) and 1
                val e = (g shr 1) and 1
                val f = g and 1
                "DEF=${d}${e}${f}"
            }
            else -> emptyList()
        }
    }
}
