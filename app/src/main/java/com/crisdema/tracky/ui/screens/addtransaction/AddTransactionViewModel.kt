package com.crisdema.tracky.ui.screens.addtransaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crisdema.tracky.data.model.Category
import com.crisdema.tracky.data.model.Transaction
import com.crisdema.tracky.data.model.TransactionType
import com.crisdema.tracky.data.repository.CategoryRepository
import com.crisdema.tracky.data.repository.TransactionRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val spaceId: String = checkNotNull(savedStateHandle["spaceId"])
    val initialType: TransactionType = savedStateHandle.get<String>("type")
        ?.let { runCatching { TransactionType.valueOf(it) }.getOrNull() }
        ?: TransactionType.EXPENSE

    val categories: StateFlow<List<Category>> = categoryRepository
        .observeCategories(spaceId)
        .map { list -> list.filter { it.type == initialType } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        categoryRepository.startRemoteSync(spaceId)
    }

    fun save(
        amount: Double,
        type: TransactionType,
        categoryId: String,
        note: String,
        date: Long,
        onSaved: () -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            repository.addTransaction(
                Transaction(
                    id = UUID.randomUUID().toString(),
                    spaceId = spaceId,
                    categoryId = categoryId,
                    type = type,
                    amount = amount,
                    note = note,
                    date = date,
                    createdBy = uid
                )
            )
            onSaved()
        }
    }
}