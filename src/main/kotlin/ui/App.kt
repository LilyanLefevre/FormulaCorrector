package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import model.CompoundEntry
import model.Correction
import model.Correction.Companion.loadCorrections
import model.CorrectionStats
import service.CsvAnalyzer
import service.MatchResult
import java.io.File

enum class SortColumn {
    ID, MASS, FORMULA, MATCHES
}

data class SortState(
    val column: SortColumn,
    val ascending: Boolean
)

@Composable
@Preview
fun App() {
    var compounds by remember { mutableStateOf<List<CompoundEntry>>(emptyList()) }
    var corrections by remember { mutableStateOf(listOf<Correction>()) }
    var csvFile by remember { mutableStateOf<File?>(null) }

    val analyzer = remember { CsvAnalyzer() }
    var matchResults by remember { mutableStateOf<List<MatchResult>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var showStatsDialog by remember { mutableStateOf(false) }
    var sortState by remember { mutableStateOf(SortState(SortColumn.ID, true)) }

    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Barre de recherche en haut
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                label = { Text("Rechercher...") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, "Rechercher") }
            )
            // Barre d'outils
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Boutons de gauche
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        val file = FileDialog.openFile()
                        if (file != null) {
                            csvFile = file
                            compounds = analyzer.loadCompounds(file)
                        }
                    }) {
                        Text("Charger CSV")
                    }

                    Button(onClick = {
                        val file = FileDialog.openFile(title = "Sélectionner les corrections", extension = "txt")
                        if (file != null) {
                            corrections = loadCorrections(file)
                        }
                    }) {
                        Text("Charger corrections")
                    }

                    Button(
                        onClick = {
                            matchResults = analyzer.applyCorrections(compounds, corrections)
                        },
                        enabled = compounds.isNotEmpty() && corrections.isNotEmpty()
                    ) {
                        Text("Appliquer corrections")
                    }

                    Button(
                        onClick = {
                            csvFile?.let {
                                compounds = analyzer.loadCompounds(it)
                                matchResults = emptyList()
                            }
                        },
                        enabled = csvFile != null
                    ) {
                        Text("Reset")
                    }

                    Button(
                        onClick = {
                            csvFile?.let { originalFile ->
                                val outputFile = File(
                                    originalFile.parentFile,
                                    "resultats_${originalFile.nameWithoutExtension}.csv"
                                )
                                outputFile.bufferedWriter().use { writer ->
                                    // En-tête
                                    writer.write("ID;m/z;Formule Originale;Correspondances trouvees\n")

                                    // Données
                                    compounds.forEach { compound ->
                                        var line = buildString {
                                            append(compound.id).append(";")
                                            append(compound.mz).append(";")
                                            append(compound.originalFormula.toFormulaString()).append(";")
                                        }

                                        val matchesForCompound = matchResults.filter { it.originalEntry.id == compound.id }
                                        if (matchesForCompound.isNotEmpty()) {
                                            line += "["
                                            matchesForCompound.forEach { match ->
                                                line += "correction = " + match.appliedCorrection.toFormulaString() + " result = (ID " + match.matchedEntry.id + ") " + match.originalEntry.originalFormula.add(match.appliedCorrection).toFormulaString() + ", "
                                            }
                                            line += "]"
                                        } else {
                                            line += "-"
                                        }

                                        writer.write(line)
                                        writer.newLine()
                                    }
                                }
                            }
                        },
                        enabled = compounds.isNotEmpty() && matchResults.isNotEmpty()
                    ) {
                        Text("Enregistrer CSV")
                    }

                    Button(
                        onClick = { showStatsDialog = true },
                        enabled = matchResults.isNotEmpty()
                    ) {
                        Text("Statistiques")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tableau avec scrollbar
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val listState = rememberLazyListState()

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // En-tête
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colors.primary.copy(alpha = 0.1f))
                        ) {
                            SortableTableHeader("ID", 0.1f, SortColumn.ID, sortState) {
                                sortState = if (sortState.column == SortColumn.ID) {
                                    sortState.copy(ascending = !sortState.ascending)
                                } else {
                                    SortState(SortColumn.ID, true)
                                }
                            }
                            SortableTableHeader("Masse", 0.15f, SortColumn.MASS, sortState) {
                                sortState = if (sortState.column == SortColumn.MASS) {
                                    sortState.copy(ascending = !sortState.ascending)
                                } else {
                                    SortState(SortColumn.MASS, true)
                                }
                            }
                            SortableTableHeader("Formule Originale", 0.2f, SortColumn.FORMULA, sortState) {
                                sortState = if (sortState.column == SortColumn.FORMULA) {
                                    sortState.copy(ascending = !sortState.ascending)
                                } else {
                                    SortState(SortColumn.FORMULA, true)
                                }
                            }
                            SortableTableHeader("Correspondances trouvées", 0.55f, SortColumn.MATCHES, sortState) {
                                sortState = if (sortState.column == SortColumn.MATCHES) {
                                    sortState.copy(ascending = !sortState.ascending)
                                } else {
                                    SortState(SortColumn.MATCHES, true)
                                }
                            }
                        }
                        Divider(color = MaterialTheme.colors.primary)
                    }

                    // Modifier le filtrage et tri des données
                    val filteredAndSortedCompounds = compounds
                        .sortedWith { a, b ->
                            val comparison = when (sortState.column) {
                                SortColumn.ID -> a.id.toInt().compareTo(b.id.toInt())
                                SortColumn.MASS -> a.mz.compareTo(b.mz)
                                SortColumn.FORMULA -> a.originalFormula.toFormulaString().compareTo(b.originalFormula.toFormulaString())
                                SortColumn.MATCHES -> {
                                    val matchesA = matchResults.count { it.originalEntry.id == a.id }
                                    val matchesB = matchResults.count { it.originalEntry.id == b.id }
                                    matchesA.compareTo(matchesB)
                                }
                            }
                            if (sortState.ascending) comparison else -comparison
                        }
                        .filter { compound ->
                            searchQuery.isEmpty() || listOf(
                                compound.id,
                                compound.mz.toString(),
                                compound.originalFormula.toFormulaString(),
                                matchResults.filter { it.originalEntry.id == compound.id }
                                    .joinToString { "${it.matchedEntry.id} ${it.appliedCorrection.toFormulaString()}" }
                            ).any { it.contains(searchQuery, ignoreCase = true) }
                        }

                    items(filteredAndSortedCompounds) { compound ->
                        val matchesForCompound = matchResults.filter { it.originalEntry.id == compound.id }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    when (matchesForCompound.size) {
                                        0 -> Color.White
                                        1 -> Color(0xFFF5F5F5)
                                        else -> Color(0xFFFFE0E0)
                                    }
                                )
                        ) {
                            TableCell(compound.id, 0.1f)
                            TableCell(compound.mz.toString(), 0.15f)
                            TableCell(compound.originalFormula.toFormulaString(), 0.2f)
                            Column(modifier = Modifier.fillMaxWidth(0.55f)) {
                                matchesForCompound.forEach { match ->
                                    Text(
                                        buildAnnotatedString {
                                            append("on applique ")
                                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                                append(match.appliedCorrection.toFormulaString())
                                            }
                                            append(" ce qui donne ")
                                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                                append(match.originalEntry.originalFormula.add(match.appliedCorrection).toFormulaString())
                                            }
                                            append(", ID ")
                                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                                append(match.matchedEntry.id)
                                            }
                                        },
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }
                                if (matchesForCompound.isEmpty()) {
                                    Text("-", modifier = Modifier.padding(4.dp))
                                }
                            }
                        }
                        Divider(color = Color.LightGray)
                    }
                }

                VerticalScrollbar(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(16.dp), // largeur augmentée
                    adapter = rememberScrollbarAdapter(scrollState = listState),
                    style = ScrollbarStyle(
                        minimalHeight = 64.dp, // hauteur minimale du curseur
                        thickness = 16.dp, // épaisseur de la barre
                        shape = RoundedCornerShape(8.dp), // coins arrondis
                        hoverDurationMillis = 300, // durée du hover
                        unhoverColor = MaterialTheme.colors.primary.copy(alpha = 0.35f), // couleur normale
                        hoverColor = MaterialTheme.colors.primary.copy(alpha = 0.50f) // couleur au survol
                    )
                )
            }
        }

        if (showStatsDialog) {
            StatsDialog(
                matchResults = matchResults,
                onDismiss = { showStatsDialog = false }
            )
        }
    }
}

@Composable
private fun SortableTableHeader(
    text: String,
    widthFraction: Float,
    column: SortColumn,
    currentSort: SortState,
    onSort: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .clickable(onClick = onSort)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier.weight(1f)
        )
        if (currentSort.column == column) {
            Icon(
                if (currentSort.ascending)
                    Icons.Default.KeyboardArrowUp
                else
                    Icons.Default.KeyboardArrowDown,
                contentDescription = if (currentSort.ascending) "Tri ascendant" else "Tri descendant",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun TableCell(text: String, widthFraction: Float) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .padding(8.dp),
        style = MaterialTheme.typography.body2
    )
}

@Composable
fun StatsDialog(
    matchResults: List<MatchResult>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Text(
                    "Statistiques des corrections",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Calcul des stats
                val stats = matchResults
                    .groupBy { it.appliedCorrection.toFormulaString() }
                    .map { (formula, results) ->
                        CorrectionStats(
                            correction = Correction(results.first().appliedCorrection),
                            timesUsed = results.size,
                            matchedCompounds = results.map { it.originalEntry to it.matchedEntry }
                        )
                    }
                    .sortedByDescending { it.timesUsed }

                // Affichage des stats
                LazyColumn {
                    items(stats) { stat ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = 2.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Correction: ${stat.correction.formula.toFormulaString()}",
                                    style = MaterialTheme.typography.subtitle1,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Utilisée ${stat.timesUsed} fois",
                                    style = MaterialTheme.typography.body1
                                )
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Total des corrections appliquées : ${matchResults.size}",
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}