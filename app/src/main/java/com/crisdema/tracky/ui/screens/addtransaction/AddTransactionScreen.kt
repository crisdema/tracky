package com.crisdema.tracky.ui.screens.addtransaction

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crisdema.tracky.R
import com.crisdema.tracky.data.model.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    spaceId: String,
    onDone: () -> Unit,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val type = viewModel.initialType
    val categories by viewModel.categories.collectAsState()

    var amountText by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }

    if (selectedCategoryId.isBlank() && categories.isNotEmpty()) {
        selectedCategoryId = categories.first().id
    }

    val selectedCategoryName = categories.find { it.id == selectedCategoryId }?.name ?: ""
    val titleRes = if (type == TransactionType.INCOME) R.string.add_income_title else R.string.add_expense_title

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(titleRes)) }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text(stringResource(R.string.label_amount)) },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                OutlinedTextField(
                    value = selectedCategoryName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.label_category_id)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                selectedCategoryId = category.id
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(R.string.label_note_optional)) },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            )

            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: return@Button
                    if (selectedCategoryId.isBlank()) return@Button
                    viewModel.save(
                        amount = amount,
                        type = type,
                        categoryId = selectedCategoryId,
                        note = note,
                        date = System.currentTimeMillis(),
                        onSaved = onDone
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
            ) {
                Text(stringResource(R.string.action_save))
            }
        }
    }
}