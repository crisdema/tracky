package com.crisdema.tracky.ui.screens.categorytransactions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import com.crisdema.tracky.R
import com.crisdema.tracky.data.model.Transaction
import com.crisdema.tracky.data.model.TransactionType
import com.crisdema.tracky.ui.screens.categories.ALL_ICONS_FLAT
import com.crisdema.tracky.ui.theme.TrackyOnSurface
import com.crisdema.tracky.ui.theme.TrackyOnSurfaceVariant
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryTransactionsScreen(
    onBack: () -> Unit,
    onEditTransaction: (String) -> Unit,
    onAddTransaction: (TransactionType) -> Unit,
    viewModel: CategoryTransactionsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val currency = remember(state.currencyCode) {
        NumberFormat.getCurrencyInstance().apply {
            runCatching { this.currency = Currency.getInstance(state.currencyCode) }
        }
    }
    val deleteDesc = stringResource(R.string.action_delete)
    val addDesc = stringResource(R.string.action_add)

    val categoryColor = remember(state.category?.colorHex) {
        state.category?.colorHex
            ?.let { runCatching { Color(it.toColorInt()) }.getOrNull() }
            ?: TrackyOnSurfaceVariant
    }
    val iconResId = ALL_ICONS_FLAT[state.category?.icon] ?: R.drawable.ic_category

    val groupedTransactions = remember(state.transactions) {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        state.transactions
            .sortedByDescending { it.date }
            .groupBy { txn ->
                val date = Instant.ofEpochMilli(txn.date).atZone(zone).toLocalDate()
                val pattern = if (date.year == today.year) "MMM d" else "MMM d, yyyy"
                SimpleDateFormat(pattern, Locale.getDefault()).format(Date(txn.date))
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.category?.name ?: stringResource(R.string.settings_categories)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                actions = {
                    state.category?.let { category ->
                        IconButton(onClick = { onAddTransaction(category.type) }) {
                            Icon(Icons.Default.Add, contentDescription = addDesc)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxWidth()) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = categoryColor.copy(alpha = 0.14f)
                ),
                border = BorderStroke(1.5.dp, categoryColor)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = iconResId),
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            state.category?.name.orEmpty(),
                            style = MaterialTheme.typography.titleMedium,
                            color = TrackyOnSurface,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                    Text(
                        currency.format(state.total),
                        style = MaterialTheme.typography.titleLarge,
                        color = TrackyOnSurface
                    )
                }
            }

            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                groupedTransactions.forEach { (dateLabel, txns) ->
                    item(key = "header_$dateLabel") {
                        Text(
                            dateLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 4.dp)
                        )
                    }
                    items(txns, key = { it.id }) { txn ->
                        CategoryTransactionRow(
                            txn = txn,
                            currency = currency,
                            deleteDesc = deleteDesc,
                            onClick = { onEditTransaction(txn.id) },
                            onDelete = { viewModel.deleteTransaction(txn.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryTransactionRow(
    txn: Transaction,
    currency: NumberFormat,
    deleteDesc: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                if (txn.note.isNotBlank()) Text(txn.note, style = MaterialTheme.typography.bodyLarge)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                val sign = if (txn.type == TransactionType.EXPENSE) "-" else "+"
                Text("$sign${currency.format(txn.amount)}", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = deleteDesc)
                }
            }
        }
    }
}