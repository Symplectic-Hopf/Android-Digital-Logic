package com.digitallogic.karnaughmap

import com.digitallogic.karnaughmap.domain.algorithm.QuineMcCluskey
import org.junit.Assert.*
import org.junit.Test

class QuineMcCluskeyTest {

    @Test
    fun testSimpleFunction_2variables() {
        // F(A,B) = A·B (minterm 3 only)
        val qm = QuineMcCluskey(2)
        val result = qm.simplify(setOf(3))
        assertNotNull(result)
        assertEquals("AB", result.sopExpression)
    }

    @Test
    fun testAlwaysZero() {
        val qm = QuineMcCluskey(2)
        val result = qm.simplify(emptySet())
        assertEquals("0", result.sopExpression)
        assertEquals("1", result.posExpression)
    }

    @Test
    fun testAlwaysOne_4variables() {
        val qm = QuineMcCluskey(4)
        val result = qm.simplify((0..15).toSet())
        assertEquals("1", result.sopExpression)
        assertEquals("0", result.posExpression)
    }

    @Test
    fun testWithDontCares_4variables() {
        // Classic example: F = sum(0,1,2,5,6,7,8,9,10,14) with don't cares d(3,11,12,13,15)
        val qm = QuineMcCluskey(4)
        val result = qm.simplify(
            minterms = setOf(0, 1, 2, 5, 6, 7, 8, 9, 10, 14),
            dontCares = setOf(3, 11, 12, 13, 15)
        )
        assertNotNull(result)
        assertTrue(result.sopExpression.isNotEmpty())
        assertTrue(result.primeImplicants.isNotEmpty())
    }

    @Test
    fun testVariableRange() {
        for (vars in 2..6) {
            val qm = QuineMcCluskey(vars)
            val minterms = setOf(0, 1)
            val result = qm.simplify(minterms)
            assertNotNull(result)
        }
    }

    @Test
    fun testInvalidVariables() {
        assertThrows(IllegalArgumentException::class.java) {
            QuineMcCluskey(1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            QuineMcCluskey(7)
        }
    }

    @Test
    fun test3VariableFunction() {
        // F(A,B,C) = sum(0,2,4,6) = all cells where C=0 => B'C' + BC' ...
        val qm = QuineMcCluskey(3)
        val result = qm.simplify(setOf(0, 2, 4, 6))
        assertTrue(result.sopExpression.contains("C"))
    }
}
