package model

import kotlinx.serialization.Serializable

@Serializable
data class AtomicFormula(
    val carbon: Int = 0,
    val hydrogen: Int = 0,
    val nitrogen: Int = 0,
    val oxygen: Int = 0,
    val sulfur: Int = 0,
    val phosphorus: Int = 0,
    val chlorine: Int = 0
) {
    fun add(other: AtomicFormula): AtomicFormula = AtomicFormula(
        carbon = carbon + other.carbon,
        hydrogen = hydrogen + other.hydrogen,
        nitrogen = nitrogen + other.nitrogen,
        oxygen = oxygen + other.oxygen,
        sulfur = sulfur + other.sulfur,
        phosphorus = phosphorus + other.phosphorus,
        chlorine = chlorine + other.chlorine
    )

    fun toFormulaString(): String = buildString {
        append("C$carbon")
        append("H$hydrogen")
        append("Cl$chlorine")
        append("N$nitrogen")
        append("O$oxygen")
        append("P$phosphorus")
        append("S$sulfur")
    }

    companion object {
        fun parseFormulaString(formula: String): AtomicFormula {
            val pattern = Regex("([A-Z][a-z]?)(-?\\d*)")
            val atoms = pattern.findAll(formula)
                .map { it.groupValues[1] to it.groupValues[2].toInt() }
                .toMap()

            return AtomicFormula(
                carbon = atoms["C"] ?: 0,
                hydrogen = atoms["H"] ?: 0,
                nitrogen = atoms["N"] ?: 0,
                oxygen = atoms["O"] ?: 0,
                sulfur = atoms["S"] ?: 0,
                phosphorus = atoms["P"] ?: 0,
                chlorine = atoms["Cl"] ?: 0
            )
        }
    }
}