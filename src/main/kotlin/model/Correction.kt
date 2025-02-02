package model

import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class Correction(
    val formula: AtomicFormula
) {
    companion object {
        fun loadCorrections(file: File): List<Correction> {
            return file.bufferedReader().lines().map { line ->
                Correction(AtomicFormula.parseFormulaString(line))
            }
                .toList()
        }
    }
}