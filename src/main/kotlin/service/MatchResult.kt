package service

import model.AtomicFormula
import model.CompoundEntry
import java.io.File

data class MatchResult(
    val originalEntry: CompoundEntry,
    val matchedEntry: CompoundEntry,
    val appliedCorrection: AtomicFormula
)