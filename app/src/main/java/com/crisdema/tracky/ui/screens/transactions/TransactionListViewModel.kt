package com.crisdema.tracky.ui.screens.transactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crisdema.tracky.data.model.Category
import com.crisdema.tracky.data.model.Transaction
import com.crisdema.tracky.data.model.TransactionType
import com.crisdema.tracky.data.repository.CategoryRepository
import com.crisdema.tracky.data.repository.SettingsRepository
import com.crisdema.tracky.data.repository.SpaceRepository
import com.crisdema.tracky.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

data class TransactionListUiState(
    val categories: List<Category> = emptyList(),
    val categoryTotals: Map<String, Double> = emptyMap(),
    val currencyCode: String = "USD",
    val selectedMonth: YearMonth = YearMonth.now(),
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val transactions: List<Transaction> = emptyList(),
    val spaceName: String = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository,
    private val spaceRepository: SpaceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val spaceId: String = savedStateHandle.get<String>("spaceId")
        ?: savedStateHandle.get<String>("id")
        ?: ""

    private val _selectedMonth = MutableStateFlow(YearMonth.now())

    init {
        if (spaceId.isNotBlank()) {
            categoryRepository.startRemoteSync(spaceId)
            transactionRepository.startRemoteSync(spaceId)
        }
    }

    val uiState: StateFlow<TransactionListUiState> = _selectedMonth
        .flatMapLatest { month ->
            combine(
                categoryRepository.observeCategories(spaceId),
                transactionRepository.observeTransactions(spaceId),
                settingsRepository.currencyCode,
                spaceRepository.observeSpace(spaceId)
            ) { categories: List<Category>, transactions: List<Transaction>, currency: String, space ->
                val monthTransactions = transactions.filter { txn ->
                    val txnMonth = Instant.ofEpochMilli(txn.date)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .let { YearMonth.from(it) }
                    txnMonth == month
                }

                val income = monthTransactions
                    .filter { it.type == TransactionType.INCOME }
                    .sumOf { it.amount }
                val expense = monthTransactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }

                val categoryTotals = monthTransactions
                    .groupBy { it.categoryId }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }

                TransactionListUiState(
                    categories = categories,
                    categoryTotals = categoryTotals,
                    currencyCode = currency,
                    selectedMonth = month,
                    totalExpense = expense,
                    totalIncome = income,
                    transactions = monthTransactions,
                    spaceName = space?.name.orEmpty()
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = TransactionListUiState()
        )

    fun goToPreviousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun goToNextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    fun setMonth(month: YearMonth) {
        _selectedMonth.value = month
    }

}