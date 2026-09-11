package com.crisdema.tracky.ui.screens.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crisdema.tracky.R
import com.crisdema.tracky.data.model.TransactionType
import com.crisdema.tracky.ui.screens.categories.components.CategoryCard
import java.text.NumberFormat
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    spaceId: String,
    onAddTransaction: (TransactionType) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCategory: (categoryId: String, month: YearMonth) -> Unit,
    viewModel: TransactionListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val currency = remember(state.currencyCode) {
        NumberFormat.getCurrencyInstance().apply {
            runCatching { currency = java.util.Currency.getInstance(state.currencyCode) }
        }
    }

    var showMonthPicker by remember { mutableStateOf(false) }

    val settingsDesc = stringResource(R.string.action_settings)
    val incomeLabel = stringResource(R.string.label_income)
    val expenseLabel = stringResource(R.string.label_expense)
    val balanceLabel = stringResource(R.string.label_balance)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.app_name))
                        if (state.spaceName.isNotBlank()) {
                            Text(
                                text = state.spaceName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = settingsDesc)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            MonthNavigator(
                month = state.selectedMonth,
                onPrevious = { viewModel.goToPreviousMonth() },
                onNext = { viewModel.goToNextMonth() },
                onLabelClick = { showMonthPicker = true }
            )

            SummaryCard(
                incomeLabel = incomeLabel,
                expenseLabel = expenseLabel,
                balanceLabel = balanceLabel,
                income = currency.format(state.totalIncome),
                expense = currency.format(state.totalExpense),
                balance = currency.format(state.totalIncome - state.totalExpense)
            )

            AddButtonsRow(
                incomeLabel = incomeLabel,
                expenseLabel = expenseLabel,
                onAddIncome = { onAddTransaction(TransactionType.INCOME) },
                onAddExpense = { onAddTransaction(TransactionType.EXPENSE) }
            )

            val expenseCategories = state.categories.filter { it.type == TransactionType.EXPENSE }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp, bottom = 4.dp), // Added vertical padding separate from buttons
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(expenseCategories, key = { it.id }) { category ->
                    val total = state.categoryTotals[category.id] ?: 0.0
                    CategoryCard(
                        category = category,
                        totalAmount = total,
                        currencyFormatter = { currency.format(it) },
                        onClick = { onOpenCategory(category.id, state.selectedMonth) },
                        enableActionsMenu = false
                    )
                }
            }
        }
    }

    if (showMonthPicker) {
        MonthPickerDialog(
            initialYearMonth = state.selectedMonth,
            onDismiss = { showMonthPicker = false },
            onSelect = {
                viewModel.setMonth(it)
                showMonthPicker = false
            }
        )
    }
}

@Composable
private fun AddButtonsRow(
    incomeLabel: String,
    expenseLabel: String,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onAddIncome,
            colors = ButtonDefaults.buttonColors(
                containerColor = androidx.compose.ui.graphics.Color(0xFF3FAE7D),
                contentColor = androidx.compose.ui.graphics.Color.White
            ),
            modifier = Modifier.weight(1f).height(56.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text(incomeLabel, modifier = Modifier.padding(start = 8.dp))
        }
        Button(
            onClick = onAddExpense,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = androidx.compose.ui.graphics.Color.White
            ),
            modifier = Modifier.weight(1f).height(56.dp)
        ) {
            Icon(Icons.Default.Remove, contentDescription = null)
            Text(expenseLabel, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun MonthNavigator(
    month: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onLabelClick: () -> Unit
) {
    val label = remember(month) {
        month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.ChevronLeft, contentDescription = null)
        }
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .clickable(onClick = onLabelClick)
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

@Composable
private fun SummaryCard(
    incomeLabel: String,
    expenseLabel: String,
    balanceLabel: String,
    income: String,
    expense: String,
    balance: String
) {
    Card(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                balanceLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                balance,
                style = MaterialTheme.typography.headlineLarge,
                color = androidx.compose.ui.graphics.Color.White
            )

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(incomeLabel, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "+$income",
                        style = MaterialTheme.typography.headlineMedium,
                        color = androidx.compose.ui.graphics.Color(0xFF3FAE7D)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(expenseLabel, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "-$expense",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthPickerDialog(
    initialYearMonth: YearMonth,
    onDismiss: () -> Unit,
    onSelect: (YearMonth) -> Unit
) {
    var year by remember { mutableStateOf(initialYearMonth.year) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { year-- }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = null)
                }
                Text(year.toString(), style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { year++ }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        },
        text = {
            Column {
                (1..12).chunked(4).forEach { rowMonths ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowMonths.forEach { m ->
                            val candidate = YearMonth.of(year, m)
                            FilterChip(
                                selected = candidate == initialYearMonth,
                                onClick = { onSelect(candidate) },
                                label = {
                                    Text(Month.of(m).getDisplayName(TextStyle.SHORT, Locale.getDefault()))
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) }
        }
    )
}