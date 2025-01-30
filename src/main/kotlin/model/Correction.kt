package model

import kotlinx.serialization.Serializable

@Serializable
data class Correction(
    val formula: AtomicFormula
) {
    // La formule sert de nom/identifiant
    val name: String get() = formula.toFormulaString()
}