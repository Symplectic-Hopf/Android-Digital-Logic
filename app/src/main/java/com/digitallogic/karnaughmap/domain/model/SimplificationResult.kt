package com.digitallogic.karnaughmap.domain.model

/**
 * A single step in the simplification process.
 */
data class SimplificationStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val highlightedImplicants: List<Implicant> = emptyList()
)

/**
 * Result of the Quine-McCluskey simplification.
 */
data class SimplificationResult(
    val variables: Int,
    val minterms: Set<Int>,
    val dontCares: Set<Int>,
    /** All prime implicants found */
    val primeImplicants: List<Implicant>,
    /** Essential prime implicants */
    val essentialPrimeImplicants: List<Implicant>,
    /** Final selected implicants for the minimal cover */
    val selectedImplicants: List<Implicant>,
    /** Minimal SOP expression */
    val sopExpression: String,
    /** Minimal POS expression */
    val posExpression: String,
    /** Step-by-step explanation */
    val steps: List<SimplificationStep>
)
