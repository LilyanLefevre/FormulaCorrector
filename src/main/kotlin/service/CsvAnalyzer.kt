package service

import model.AtomicFormula.Companion.parseFormulaString
import model.CompoundEntry
import model.Correction
import java.io.File

class CsvAnalyzer {
    fun loadCompounds(file: File): List<CompoundEntry> {
        println("=== Début du chargement du fichier CSV ===")
        println("Lecture du fichier : ${file.absolutePath}")

        return try {
            val lines = file.readLines()
            println("Nombre total de lignes : ${lines.size}")

            val compounds = lines.drop(1).mapNotNull { line ->
                try {
                    val fields = line.split(";")
                    if (fields.size < 3) {
                        println("ERREUR: Ligne mal formatée (moins de 3 champs): $line")
                        return@mapNotNull null
                    }

                    val id = fields[0]
                    val mz = fields[1].replace(",", ".").toDouble()
                    val formula = parseFormulaString(fields[2].trim('"'))

                    println("Ligne parsée avec succès: ID=$id, m/z=$mz, formule=${formula.toFormulaString()}")

                    CompoundEntry(id, mz, formula)
                } catch (e: Exception) {
                    println("ERREUR lors du parsing de la ligne: $line")
                    println("Message d'erreur: ${e.message}")
                    null
                }
            }

            println("=== Fin du chargement ===")
            println("Nombre de composés chargés: ${compounds.size}")
            compounds

        } catch (e: Exception) {
            println("ERREUR CRITIQUE lors de la lecture du fichier: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    fun applyCorrections(compounds: List<CompoundEntry>, corrections: List<Correction>): List<MatchResult> {
        println("\n=== Début de l'application des corrections ===")
        println("Nombre de composés à analyser: ${compounds.size}")
        println("Nombre de corrections disponibles: ${corrections.size}")

        val results = mutableListOf<MatchResult>()
        val compoundsByFormula = compounds.associateBy { it.originalFormula.toFormulaString() }

        compounds.forEach { compound ->
            println("\nAnalyse du composé ID=${compound.id}, formule=${compound.originalFormula.toFormulaString()}")

            // On teste toutes les corrections
            corrections.forEach { correction ->
                val correctedFormula = compound.originalFormula.add(correction.formula)
                val correctedFormulaString = correctedFormula.toFormulaString()

                // On cherche tous les matchs pour cette correction
                compoundsByFormula[correctedFormulaString]?.let { matchedCompound ->
                    println("  ✓ MATCH TROUVÉ! avec ID=${matchedCompound.id}")
                    results.add(MatchResult(compound, matchedCompound, correction.formula))
                }
            }

            // Résumé pour ce composé
            val matchesForCompound = results.filter { it.originalEntry.id == compound.id }
            when (matchesForCompound.size) {
                0 -> println("  ✗ Aucune correspondance trouvée")
                1 -> println("  ✓ Une correspondance trouvée")
                else -> println("  ⚠ ${matchesForCompound.size} correspondances trouvées !")
            }
        }

        // Résumé final plus détaillé
        println("\n=== Résumé de l'analyse ===")
        val compoundsWithMultipleMatches = results
            .groupBy { it.originalEntry.id }
            .filter { it.value.size > 1 }

        println("Nombre total de correspondances trouvées: ${results.size}")
        println("Nombre de composés ayant plusieurs correspondances: ${compoundsWithMultipleMatches.size}")

        compoundsWithMultipleMatches.forEach { (id, matches) ->
            println("\nComposé $id a ${matches.size} correspondances:")
            matches.forEach { match ->
                println("  - Match avec ${match.matchedEntry.id} via ${match.appliedCorrection.toFormulaString()}")
            }
        }

        return results
    }
}