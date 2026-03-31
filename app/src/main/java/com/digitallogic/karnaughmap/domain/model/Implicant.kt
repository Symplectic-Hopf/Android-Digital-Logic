package com.digitallogic.karnaughmap.domain.model

/**
 * Represents a single implicant (group) in the Quine-McCluskey algorithm.
 * @param minterms The set of minterm indices covered by this implicant.
 * @param mask Bitmask where 1 = variable present, 0 = variable is a don't-care within the group.
 * @param value The value of fixed bits (those not in the mask are irrelevant).
 * @param variables Number of variables in the function.
 */
data class Implicant(
    val minterms: Set<Int>,
    val mask: Int,
    val value: Int,
    val variables: Int
) {
    val size: Int get() = minterms.size

    /** Returns true if this implicant covers the given minterm. */
    fun covers(minterm: Int): Boolean = minterm in minterms

    /**
     * Converts this implicant to a SOP literal string.
     * Variables are labeled A, B, C, D, E, F (MSB to LSB).
     */
    fun toSopString(): String {
        if (minterms.isEmpty()) return "0"
        val sb = StringBuilder()
        for (i in (variables - 1) downTo 0) {
            val varLabel = ('A' + (variables - 1 - i))
            val bitMask = 1 shl i
            if (mask and bitMask != 0) {
                // This bit is fixed
                if (value and bitMask == 0) {
                    sb.append(varLabel).append("'")
                } else {
                    sb.append(varLabel)
                }
            }
            // Else: this bit is a don't-care in the group (variable eliminated)
        }
        return if (sb.isEmpty()) "1" else sb.toString()
    }

    /**
     * Converts this implicant to a POS factor string (sum term).
     */
    fun toPosString(): String {
        if (minterms.isEmpty()) return "1"
        val sb = StringBuilder()
        var first = true
        for (i in (variables - 1) downTo 0) {
            val varLabel = ('A' + (variables - 1 - i))
            val bitMask = 1 shl i
            if (mask and bitMask != 0) {
                if (!first) sb.append("+")
                // In POS, a 1 in the value means the variable is complemented
                if (value and bitMask != 0) {
                    sb.append(varLabel).append("'")
                } else {
                    sb.append(varLabel)
                }
                first = false
            }
        }
        return if (sb.isEmpty()) "0" else "($sb)"
    }

    companion object {
        /** Create a prime implicant from two combinable implicants. Returns null if not combinable. */
        fun combine(a: Implicant, b: Implicant): Implicant? {
            if (a.mask != b.mask) return null
            val diff = a.value xor b.value
            // Combinable if they differ in exactly one bit
            if (diff == 0 || diff and (diff - 1) != 0) return null
            return Implicant(
                minterms = a.minterms + b.minterms,
                mask = a.mask and diff.inv(),
                value = a.value and diff.inv(),
                variables = a.variables
            )
        }
    }
}
