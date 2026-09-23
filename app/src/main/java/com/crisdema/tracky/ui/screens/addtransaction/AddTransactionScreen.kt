package com.crisdema.tracky.ui.screens.addtransaction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crisdema.tracky.R
import com.crisdema.tracky.data.model.Category
import com.crisdema.tracky.data.model.TransactionType
import com.crisdema.tracky.ui.screens.categories.components.CategoryCard
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    spaceId: String,
    onDone: (categoryId: String) -> Unit,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val type = viewModel.initialType
    val categories by viewModel.categories.collectAsState()
    val existingTransaction by viewModel.existingTransaction.collectAsState()
    val isEditMode = viewModel.isEditMode

    var amountText by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var prefilled by remember { mutableStateOf(false) }
    var noteSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var noteFieldExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(existingTransaction) {
        val txn = existingTransaction ?: return@LaunchedEffect
        amountText = txn.amount.toString()
        selectedCategoryId = txn.categoryId
        note = txn.note
        selectedDateMillis = txn.date
        prefilled = true
    }

    LaunchedEffect(selectedCategoryId) {
        noteSuggestions = viewModel.getFrequentNotes(selectedCategoryId)
    }

    val filteredNoteSuggestions = remember(note, noteSuggestions) {
        if (note.isBlank()) {
            noteSuggestions
        } else {
            noteSuggestions.filter { it.contains(note, ignoreCase = true) && it != note }
        }
    }

    if (!isEditMode && selectedCategoryId.isBlank() && categories.isNotEmpty()) {
        selectedCategoryId = viewModel.prefilledCategoryId
            ?.takeIf { id -> categories.any { it.id == id } }
            ?: categories.first().id
    }

    val titleRes = if (type == TransactionType.INCOME) R.string.add_income_title else R.string.add_expense_title
    val screenTitle = if (isEditMode) stringResource(R.string.edit_transaction_title) else stringResource(titleRes)

    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault()) }
    val dateLabel = remember(selectedDateMillis) {
        Instant.ofEpochMilli(selectedDateMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(dateFormatter)
    }

    Scaffold(topBar = { TopAppBar(title = { Text(screenTitle) }) }) { padding ->
        if (isEditMode && !prefilled) {
            Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.label_date),
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text(stringResource(R.string.label_amount)) },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = noteFieldExpanded && filteredNoteSuggestions.isNotEmpty(),
                onExpandedChange = { noteFieldExpanded = it },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                OutlinedTextField(
                    value = note,
                    onValueChange = {
                        note = it
                        noteFieldExpanded = true
                    },
                    label = { Text(stringResource(R.string.label_note)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true)
                )
                ExposedDropdownMenu(
                    expanded = noteFieldExpanded && filteredNoteSuggestions.isNotEmpty(),
                    onDismissRequest = { noteFieldExpanded = false }
                ) {
                    filteredNoteSuggestions.forEach { suggestion ->
                        DropdownMenuItem(
                            text = { Text(suggestion) },
                            onClick = {
                                note = suggestion
                                noteFieldExpanded = false
                            }
                        )
                    }
                }
            }

            Text(
                text = stringResource(R.string.label_category_id),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
            )

            CategoryGrid(
                categories = categories,
                selectedCategoryId = selectedCategoryId,
                onCategorySelected = { selectedCategoryId = it }
            )

            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: return@Button
                    if (selectedCategoryId.isBlank()) return@Button
                    viewModel.save(
                        amount = amount,
                        type = existingTransaction?.type ?: type,
                        categoryId = selectedCategoryId,
                        note = note,
                        date = selectedDateMillis,
                        onSaved = onDone
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
            ) {
                Text(stringResource(R.string.action_save))
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = run {
                val localDate = Instant.ofEpochMilli(selectedDateMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                localDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { utcMillis ->
                        val pickedLocalDate = Instant.ofEpochMilli(utcMillis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        selectedDateMillis = pickedLocalDate
                            .atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.action_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun CategoryGrid(
    categories: List<Category>,
    selectedCategoryId: String,
    onCategorySelected: (String) -> Unit
) {
    Column {
        categories.chunked(2).forEach { rowCategories ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowCategories.forEach { category ->
                    Column(modifier = Modifier.weight(1f)) {
                        CategoryCard(
                            category = category,
                            onClick = { onCategorySelected(category.id) },
                            enableActionsMenu = false,
                            isSelected = category.id == selectedCategoryId
                        )
                    }
                }
                if (rowCategories.size == 1) {
                    Column(modifier = Modifier.weight(1f)) {}
                }
            }
        }
    }
}