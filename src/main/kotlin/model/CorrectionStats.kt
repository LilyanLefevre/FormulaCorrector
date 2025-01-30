package model

data class CorrectionStats(
    val correction: Correction,
    val timesUsed: Int,
    val matchedCompounds: List<Pair<CompoundEntry, CompoundEntry>> // Paires de (original, matched)
)