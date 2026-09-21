package com.crisdema.tracky.ui.screens.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crisdema.tracky.R
import com.crisdema.tracky.data.model.Category
import com.crisdema.tracky.data.model.TransactionType
import com.crisdema.tracky.ui.screens.categories.components.CategoryCard
import com.crisdema.tracky.ui.theme.TrackyExpense
import com.crisdema.tracky.ui.theme.TrackyIncome
import com.crisdema.tracky.ui.theme.TrackyOnSurface
import java.text.NumberFormat
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    spaceId: String,
    highlightedCategoryId: String? = null,
    onHighlightConsumed: () -> Unit = {},
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
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
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
            val incomeCategories = state.categories.filter { it.type == TransactionType.INCOME }

            CategoryGridSection(
                title = expenseLabel,
                categories = expenseCategories,
                categoryTotals = state.categoryTotals,
                currencyFormatter = { currency.format(it) },
                onCategoryClick = { categoryId -> onOpenCategory(categoryId, state.selectedMonth) },
                highlightedCategoryId = highlightedCategoryId,
                onHighlightConsumed = onHighlightConsumed,
            )

            CategoryGridSection(
                title = incomeLabel,
                categories = incomeCategories,
                categoryTotals = state.categoryTotals,
                currencyFormatter = { currency.format(it) },
                onCategoryClick = { categoryId -> onOpenCategory(categoryId, state.selectedMonth) },
                highlightedCategoryId = highlightedCategoryId,
                onHighlightConsumed = onHighlightConsumed,
            )
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
private fun CategoryGridSection(
    title: String,
    categories: List<Category>,
    categoryTotals: Map<String, Double>,
    currencyFormatter: (Double) -> String,
    onCategoryClick: (String) -> Unit,
    highlightedCategoryId: String? = null,
    onHighlightConsumed: () -> Unit = {},
) {
    if (categories.isEmpty()) return

    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
    )

    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        categories.chunked(2).forEach { rowCategories ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowCategories.forEach { category ->
                    val total = categoryTotals[category.id] ?: 0.0
                    Column(modifier = Modifier.weight(1f)) {
                        CategoryCard(
                            category = category,
                            totalAmount = total,
                            currencyFormatter = currencyFormatter,
                            onClick = { onCategoryClick(category.id) },
                            enableActionsMenu = false,
                            isHighlighted = category.id == highlightedCategoryId,
                            onHighlightFinished = onHighlightConsumed,
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
        IconButton(
            onClick = onAddIncome,
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(TrackyIncome.copy(alpha = 0.12f))
                .border(1.dp, TrackyIncome, RoundedCornerShape(24.dp))
        ) {
            Icon(Icons.Default.Add, contentDescription = incomeLabel, tint = TrackyIncome)
        }
        IconButton(
            onClick = onAddExpense,
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(TrackyExpense.copy(alpha = 0.12f))
                .border(1.dp, TrackyExpense, RoundedCornerShape(24.dp))
        ) {
            Icon(Icons.Default.Remove, contentDescription = expenseLabel, tint = TrackyExpense)
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
                color = TrackyOnSurface
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(incomeLabel, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        income,
                        style = MaterialTheme.typography.headlineMedium,
                        color = TrackyIncome
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(expenseLabel, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        expense,
                        style = MaterialTheme.typography.headlineMedium,
                        color = TrackyExpense
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
                                    Text(
                                        text = Month.of(m).getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                        maxLines = 1,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                modifier = Modifier.weight(1f)
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