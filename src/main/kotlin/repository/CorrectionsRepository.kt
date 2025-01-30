package repository

import kotlinx.serialization.json.Json
import model.AtomicFormula
import model.AtomicFormula.Companion.parseFormulaString
import model.Correction
import java.nio.file.Paths

class CorrectionsRepository {
    private val appDataDir = Paths.get(System.getProperty("user.home"), ".massspec-corrector")
    private val correctionsFile = appDataDir.resolve("corrections.txt").toFile()

    init {
        appDataDir.toFile().mkdirs()
        if (!correctionsFile.exists()) {
            // Copie le fichier des resources vers le fichier local
            val defaultContent = this::class.java.getResourceAsStream("/corrections.txt")?.bufferedReader()?.readText()
                ?: throw IllegalStateException("Fichier corrections.txt non trouvé dans les resources")
            correctionsFile.writeText(defaultContent)
        }
    }

    fun getCorrections(): List<Correction> {
        return try {
            correctionsFile.readLines().map { Correction(parseFormulaString(it)) }
        } catch (e: Exception) {
            println("Erreur lors de la lecture des corrections : ${e.message}")
            emptyList()
        }
    }

    fun saveCorrections(corrections: List<Correction>) {
        try {
            correctionsFile.writeText(corrections.joinToString("\n") { it.formula.toFormulaString() })
        } catch (e: Exception) {
            println("Erreur lors de la sauvegarde des corrections : ${e.message}")
        }
    }
}