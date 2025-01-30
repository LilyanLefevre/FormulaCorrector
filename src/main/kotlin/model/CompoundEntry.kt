package model

import kotlinx.serialization.Serializable

@Serializable
data class CompoundEntry(
    val id: String,
    val mz: Double,
    val originalFormula: AtomicFormula,
    val correctedFormula: AtomicFormula? = null
)