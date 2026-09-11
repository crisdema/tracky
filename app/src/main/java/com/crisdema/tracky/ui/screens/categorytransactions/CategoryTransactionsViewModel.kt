package com.crisdema.tracky.ui.screens.categorytransactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crisdema.tracky.data.model.Category
import com.crisdema.tracky.data.model.Transaction
import com.crisdema.tracky.data.repository.CategoryRepository
import com.crisdema.tracky.data.repository.SettingsRepository
import com.crisdema.tracky.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

data class CategoryTransactionsUiState(
    val category: Category? = null,
    val transactions: List<Transaction> = emptyList(),
    val total: Double = 0.0,
    val currencyCode: String = "USD"
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CategoryTransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val spaceId: String = savedStateHandle.get<String>("spaceId") ?: ""
    private val categoryId: String = savedStateHandle.get<String>("categoryId") ?: ""
    private val yearMonth: YearMonth = savedStateHandle.get<String>("yearMonth")
        ?.let { runCatching { YearMonth.parse(it) }.getOrNull() }
        ?: YearMonth.now()

    val uiState: StateFlow<CategoryTransactionsUiState> = combine(
        categoryRepository.observeCategories(spaceId),
        transactionRepository.observeTransactions(spaceId),
        settingsRepository.currencyCode
    ) { categories, transactions, currency ->
        val category = categories.firstOrNull { it.id == categoryId }
        val monthTransactions = transactions.filter { txn ->
            txn.categoryId == categoryId &&
                Instant.ofEpochMilli(txn.date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .let { YearMonth.from(it) } == yearMonth
        }
        CategoryTransactionsUiState(
            category = category,
            transactions = monthTransactions,
            total = monthTransactions.sumOf { it.amount },
            currencyCode = currency
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CategoryTransactionsUiState()
    )

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(spaceId, transactionId)
        }
    }
}