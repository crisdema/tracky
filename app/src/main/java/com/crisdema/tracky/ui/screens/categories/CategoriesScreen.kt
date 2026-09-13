package com.crisdema.tracky.ui.screens.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crisdema.tracky.R
import com.crisdema.tracky.data.model.Category
import com.crisdema.tracky.data.model.TransactionType
import com.crisdema.tracky.ui.screens.categories.components.AddCategoryDialog
import com.crisdema.tracky.ui.screens.categories.components.CategoryCard
import com.crisdema.tracky.ui.screens.categories.components.DeleteCategoryDialog
import com.crisdema.tracky.ui.screens.categories.components.EditCategoryDialog
import com.crisdema.tracky.ui.screens.categories.components.ReorderCategoriesDialog
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onBack: () -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories = uiState.categories

    var showAddDialog by remember { mutableStateOf(false) }
    var showReorderDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.categories_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showReorderDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = stringResource(R.string.action_reorder_categories)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.action_add_category)
                )
            }
        }
    ) { padding ->
        val expenseCategories = categories.filter { it.type == TransactionType.EXPENSE }
        val incomeCategories = categories.filter { it.type == TransactionType.INCOME }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (expenseCategories.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    SectionHeader(stringResource(R.string.section_expenses))
                }
                items(expenseCategories, key = { it.id }) { category ->
                    CategoryCard(
                        category = category,
                        onEdit = { categoryToEdit = category },
                        onDelete = { categoryToDelete = category }
                    )
                }
            }

            if (incomeCategories.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    SectionHeader(stringResource(R.string.section_income))
                }
                items(incomeCategories, key = { it.id }) { category ->
                    CategoryCard(
                        category = category,
                        onEdit = { categoryToEdit = category },
                        onDelete = { categoryToDelete = category }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddCategoryDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, type, icon, color ->
                val newCategory = Category(
                    id = UUID.randomUUID().toString(),
                    spaceId = viewModel.spaceId,
                    name = name,
                    type = type,
                    icon = icon,
                    colorHex = color
                )
                viewModel.addCategory(newCategory)
                showAddDialog = false
            }
        )
    }

    if (showReorderDialog) {
        val expenseCategories = categories.filter { it.type == TransactionType.EXPENSE }
        val incomeCategories = categories.filter { it.type == TransactionType.INCOME }
        ReorderCategoriesDialog(
            expenseCategories = expenseCategories,
            incomeCategories = incomeCategories,
            onReorderExpense = { viewModel.reorderExpenseCategories(it) },
            onReorderIncome = { viewModel.reorderIncomeCategories(it) },
            onDismiss = { showReorderDialog = false }
        )
    }

    categoryToEdit?.let { category ->
        EditCategoryDialog(
            category = category,
            onDismiss = { categoryToEdit = null },
            onConfirm = { updatedCategory ->
                viewModel.updateCategory(updatedCategory)
                categoryToEdit = null
            }
        )
    }

    categoryToDelete?.let { category ->
        DeleteCategoryDialog(
            category = category,
            onConfirm = {
                viewModel.deleteCategory(category.id)
                categoryToDelete = null
            },
            onDismiss = { categoryToDelete = null }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}