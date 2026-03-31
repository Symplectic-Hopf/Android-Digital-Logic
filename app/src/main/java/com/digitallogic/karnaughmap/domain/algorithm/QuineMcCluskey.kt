package com.digitallogic.karnaughmap.domain.algorithm

import com.digitallogic.karnaughmap.domain.model.Implicant
import com.digitallogic.karnaughmap.domain.model.SimplificationResult
import com.digitallogic.karnaughmap.domain.model.SimplificationStep

/**
 * Implements the Quine-McCluskey algorithm for boolean function minimization.
 * Supports 2 to 6 variables.
 */
class QuineMcCluskey(private val variables: Int) {

    init {
        require(variables in 2..6) { "Variables must be between 2 and 6" }
    }

    /**
     * Simplify the boolean function for SOP form.
     * @param minterms Minterm indices (output = 1).
     * @param dontCares Don't care indices.
     * @return SimplificationResult containing SOP, POS, and step-by-step explanation.
     */
    fun simplify(minterms: Set<Int>, dontCares: Set<Int> = emptySet()): SimplificationResult {
        val steps = mutableListOf<SimplificationStep>()
        val allOnes = minterms + dontCares

        // Edge cases
        if (minterms.isEmpty()) {
            return SimplificationResult(
                variables, minterms, dontCares,
                emptyList(), emptyList(), emptyList(),
                "0", "1", listOf(SimplificationStep(1, "Result", "Function is always 0", emptyList()))
            )
        }
        val maxMinterms = (1 shl variables)
        if (minterms.size + dontCares.size >= maxMinterms) {
            return SimplificationResult(
                variables, minterms, dontCares,
                emptyList(), emptyList(), emptyList(),
                "1", "0", listOf(SimplificationStep(1, "Result", "Function is always 1", emptyList()))
            )
        }

        // Step 1: Create initial implicants (one per minterm/don't-care)
        val initialImplicants = allOnes.map { minterm ->
            Implicant(
                minterms = setOf(minterm),
                mask = (1 shl variables) - 1,
                value = minterm,
                variables = variables
            )
        }

        steps.add(SimplificationStep(
            1, "初始最小项 (Initial Minterms)",
            "标出所有真值为1的最小项：${minterms.sorted().joinToString(", ")}，无关项：${dontCares.sorted().joinToString(", ")}",
            initialImplicants.filter { it.minterms.first() in minterms }
        ))

        // Step 2: Find all prime implicants via iterative combining
        val primeImplicants = findPrimeImplicants(initialImplicants)

        steps.add(SimplificationStep(
            2, "本原蕴涵项 (Prime Implicants)",
            "通过逐步合并，找到所有本原蕴涵项：\n${primeImplicants.joinToString("\n") { "  ${it.toSopString()} 覆盖最小项 ${it.minterms.filter { m -> m in minterms }.sorted()}" }}",
            primeImplicants
        ))

        // Step 3: Build prime implicant chart and find essential PIs
        val essentialPIs = findEssentialPrimeImplicants(primeImplicants, minterms)

        steps.add(SimplificationStep(
            3, "必要本原蕴涵项 (Essential Prime Implicants)",
            "找到必要本原蕴涵项（每个仅被一个PI覆盖的最小项决定）：\n${essentialPIs.joinToString("\n") { "  ${it.toSopString()} 覆盖 ${it.minterms.filter { m -> m in minterms }.sorted()}" }}",
            essentialPIs
        ))

        // Step 4: Cover remaining minterms
        val coveredByEssential = essentialPIs.flatMap { pi ->
            pi.minterms.filter { it in minterms }
        }.toSet()
        val remainingMinterms = minterms - coveredByEssential
        val additionalPIs = if (remainingMinterms.isEmpty()) {
            emptyList()
        } else {
            coverRemainingMinterms(primeImplicants, remainingMinterms, essentialPIs)
        }

        val selectedImplicants = (essentialPIs + additionalPIs).distinctBy { it.minterms }

        if (remainingMinterms.isNotEmpty()) {
            steps.add(SimplificationStep(
                4, "覆盖剩余最小项 (Cover Remaining Minterms)",
                "选择额外PI覆盖剩余最小项 ${remainingMinterms.sorted()}：\n${additionalPIs.joinToString("\n") { "  ${it.toSopString()}" }}",
                additionalPIs
            ))
        } else {
            steps.add(SimplificationStep(
                4, "覆盖完成 (Coverage Complete)",
                "必要本原蕴涵项已覆盖所有最小项，无需额外选择。",
                emptyList()
            ))
        }

        // Build SOP expression
        val sopExpression = if (selectedImplicants.isEmpty()) "0"
        else selectedImplicants.joinToString(" + ") { it.toSopString() }

        // Build POS expression by applying Q-M on maxterms
        val posExpression = buildPosExpression(minterms, dontCares)

        steps.add(SimplificationStep(
            5, "最终结果 (Final Result)",
            "最简与或式 (SOP): $sopExpression\n最简或与式 (POS): $posExpression",
            selectedImplicants
        ))

        return SimplificationResult(
            variables = variables,
            minterms = minterms,
            dontCares = dontCares,
            primeImplicants = primeImplicants,
            essentialPrimeImplicants = essentialPIs,
            selectedImplicants = selectedImplicants,
            sopExpression = sopExpression,
            posExpression = posExpression,
            steps = steps
        )
    }

    /** Find all prime implicants via iterative combining. */
    private fun findPrimeImplicants(initial: List<Implicant>): List<Implicant> {
        val primeImplicants = mutableListOf<Implicant>()
        var currentGroup = initial.toMutableList()

        while (currentGroup.isNotEmpty()) {
            val nextGroup = mutableListOf<Implicant>()
            val combined = BooleanArray(currentGroup.size)

            for (i in currentGroup.indices) {
                for (j in i + 1 until currentGroup.size) {
                    val merged = Implicant.combine(currentGroup[i], currentGroup[j])
                    if (merged != null) {
                        combined[i] = true
                        combined[j] = true
                        if (!nextGroup.any { it.minterms == merged.minterms }) {
                            nextGroup.add(merged)
                        }
                    }
                }
            }

            // Uncombined implicants become prime implicants
            for (i in currentGroup.indices) {
                if (!combined[i]) {
                    val imp = currentGroup[i]
                    if (!primeImplicants.any { it.minterms == imp.minterms }) {
                        primeImplicants.add(imp)
                    }
                }
            }

            currentGroup = nextGroup
        }

        return primeImplicants
    }

    /** Find essential prime implicants using the prime implicant chart. */
    private fun findEssentialPrimeImplicants(
        primeImplicants: List<Implicant>,
        minterms: Set<Int>
    ): List<Implicant> {
        val essential = mutableListOf<Implicant>()

        for (minterm in minterms) {
            val covering = primeImplicants.filter { it.covers(minterm) }
            if (covering.size == 1) {
                val epi = covering.first()
                if (!essential.any { it.minterms == epi.minterms }) {
                    essential.add(epi)
                }
            }
        }

        return essential
    }

    /** Use Petrick's method or greedy to cover remaining minterms. */
    private fun coverRemainingMinterms(
        primeImplicants: List<Implicant>,
        remainingMinterms: Set<Int>,
        alreadySelected: List<Implicant>
    ): List<Implicant> {
        val selected = mutableListOf<Implicant>()
        var uncovered = remainingMinterms.toMutableSet()
        val candidates = primeImplicants.filter { pi ->
            !alreadySelected.any { it.minterms == pi.minterms }
        }

        // Greedy: pick PI that covers the most uncovered minterms
        while (uncovered.isNotEmpty()) {
            val best = candidates
                .filter { pi -> pi.minterms.any { it in uncovered } }
                .maxByOrNull { pi -> pi.minterms.count { it in uncovered } }
                ?: break

            selected.add(best)
            uncovered.removeAll(best.minterms)
        }

        return selected
    }

    /** Build POS expression by finding prime implicants of the complement function. */
    private fun buildPosExpression(minterms: Set<Int>, dontCares: Set<Int>): String {
        val totalCells = 1 shl variables
        val allIndices = (0 until totalCells).toSet()
        // Maxterms = cells that are 0 (not 1 and not don't-care)
        val maxterms = allIndices - minterms - dontCares

        if (maxterms.isEmpty()) return "1"
        if (minterms.isEmpty()) return "0"

        // Reuse current instance's private methods directly instead of creating a new one
        return simplifyForPos(maxterms, dontCares)
    }

    /** Internal method to produce POS factors from maxterms. */
    private fun simplifyForPos(maxterms: Set<Int>, dontCares: Set<Int>): String {
        val allOnes = maxterms + dontCares
        val initialImplicants = allOnes.map { minterm ->
            Implicant(
                minterms = setOf(minterm),
                mask = (1 shl variables) - 1,
                value = minterm,
                variables = variables
            )
        }

        val primeImplicants = findPrimeImplicants(initialImplicants)
        val essentialPIs = findEssentialPrimeImplicants(primeImplicants, maxterms)
        val coveredByEssential = essentialPIs.flatMap { it.minterms.filter { m -> m in maxterms } }.toSet()
        val remainingMaxterms = maxterms - coveredByEssential
        val additionalPIs = if (remainingMaxterms.isEmpty()) emptyList()
        else coverRemainingMinterms(primeImplicants, remainingMaxterms, essentialPIs)

        val selected = (essentialPIs + additionalPIs).distinctBy { it.minterms }
        return if (selected.isEmpty()) "1"
        else selected.joinToString("·") { it.toPosString() }
    }
}
