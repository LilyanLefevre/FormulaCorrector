package ui

import java.awt.FileDialog
import java.awt.Frame
import java.io.File

object FileDialog {
    fun openFile(title: String = "Sélectionner un fichier", extension: String = "csv"): File? {
        val dialog = FileDialog(null as Frame?, title, FileDialog.LOAD)
        dialog.setFile("*.$extension")
        dialog.isVisible = true
        return dialog.files.firstOrNull()
    }
}