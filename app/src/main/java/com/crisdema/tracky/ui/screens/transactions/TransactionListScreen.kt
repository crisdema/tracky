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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }

    val settingsDesc = stringResource(R.string.action_settings)
    val incomeLabel = stringResource(R.string.label_income)
    val expenseLabel = stringResource(R.string.label_expense)
    val balanceLabel = stringResource(R.string.label_balance)

    LaunchedEffect(highlightedCategoryId) {
        val categoryType = state.categories.firstOrNull { it.id == highlightedCategoryId }?.type
        if (categoryType != null) {
            selectedType = categoryType
        }
    }

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
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp
            ) {
                AddButtonsRow(
                    incomeLabel = incomeLabel,
                    expenseLabel = expenseLabel,
                    onAddIncome = { onAddTransaction(TransactionType.INCOME) },
                    onAddExpense = { onAddTransaction(TransactionType.EXPENSE) }
                )
            }
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
                balance = currency.format(state.totalIncome - state.totalExpense),
                totalIncome = state.totalIncome,
                totalExpense = state.totalExpense
            )

            TypeToggle(
                selectedType = selectedType,
                expenseLabel = expenseLabel,
                incomeLabel = incomeLabel,
                onTypeSelected = { selectedType = it }
            )

            val visibleCategories = state.categories.filter { it.type == selectedType }

            CategoryGrid(
                categories = visibleCategories,
                categoryTotals = state.categoryTotals,
                currencyFormatter = { currency.format(it) },
                onCategoryClick = { categoryId -> onOpenCategory(categoryId, state.selectedMonth) },
                highlightedCategoryId = highlightedCategoryId,
                onHighlightConsumed = onHighlightConsumed
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeToggle(
    selectedType: TransactionType,
    expenseLabel: String,
    incomeLabel: String,
    onTypeSelected: (TransactionType) -> Unit
) {
    val transparentColors = SegmentedButtonDefaults.colors(
        activeContainerColor = Color.Transparent,
        inactiveContainerColor = Color.Transparent,
        activeContentColor = MaterialTheme.colorScheme.onSurface,
        inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        SegmentedButton(
            selected = selectedType == TransactionType.EXPENSE,
            onClick = { onTypeSelected(TransactionType.EXPENSE) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            colors = transparentColors,
            icon = { SegmentedButtonDefaults.Icon(active = selectedType == TransactionType.EXPENSE) }
        ) {
            Text(expenseLabel)
        }
        SegmentedButton(
            selected = selectedType == TransactionType.INCOME,
            onClick = { onTypeSelected(TransactionType.INCOME) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            colors = transparentColors,
            icon = { SegmentedButtonDefaults.Icon(active = selectedType == TransactionType.INCOME) }
        ) {
            Text(incomeLabel)
        }
    }
}

@Composable
private fun CategoryGrid(
    categories: List<Category>,
    categoryTotals: Map<String, Double>,
    currencyFormatter: (Double) -> String,
    onCategoryClick: (String) -> Unit,
    highlightedCategoryId: String? = null,
    onHighlightConsumed: () -> Unit = {}
) {
    if (categories.isEmpty()) return

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
                            emphasizeColor = false,
                            onHighlightFinished = onHighlightConsumed
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 28.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IconButton(
            onClick = onAddIncome,
            modifier = Modifier
                .weight(1f)
                .height(60.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(TrackyIncome.copy(alpha = 0.12f))
                .border(1.dp, TrackyIncome, RoundedCornerShape(28.dp))
        ) {
            Icon(Icons.Default.Add, contentDescription = incomeLabel, tint = TrackyIncome)
        }
        IconButton(
            onClick = onAddExpense,
            modifier = Modifier
                .weight(1f)
                .height(60.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(TrackyExpense.copy(alpha = 0.12f))
                .border(1.dp, TrackyExpense, RoundedCornerShape(28.dp))
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
    balance: String,
    totalIncome: Double,
    totalExpense: Double
) {
    val total = totalIncome + totalExpense
    val incomeFraction = if (total > 0.0) (totalIncome / total).toFloat() else 0.5f
    val expenseFraction = if (total > 0.0) (totalExpense / total).toFloat() else 0.5f

    Card(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Text(
                balanceLabel.uppercase(),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                balance,
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                color = TrackyOnSurface,
                modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            ) {
                if (incomeFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(incomeFraction)
                            .fillMaxHeight()
                            .background(TrackyIncome)
                    )
                }
                if (expenseFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(expenseFraction)
                            .fillMaxHeight()
                            .background(TrackyExpense)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column {
                    Text(
                        incomeLabel.uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        income,
                        style = MaterialTheme.typography.titleMedium,
                        color = TrackyIncome
                    )
                }
                Column {
                    Text(
                        expenseLabel.uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        expense,
                        style = MaterialTheme.typography.titleMedium,
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