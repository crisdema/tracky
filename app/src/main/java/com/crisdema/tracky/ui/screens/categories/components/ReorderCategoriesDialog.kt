package com.crisdema.tracky.ui.screens.categories.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.crisdema.tracky.R
import com.crisdema.tracky.data.model.Category
import com.crisdema.tracky.ui.screens.categories.ALL_ICONS_FLAT

@Composable
fun ReorderCategoriesDialog(
    expenseCategories: List<Category>,
    incomeCategories: List<Category>,
    onReorderExpense: (List<Category>) -> Unit,
    onReorderIncome: (List<Category>) -> Unit,
    onDismiss: () -> Unit
) {
    var expenseList by remember(expenseCategories) { mutableStateOf(expenseCategories) }
    var incomeList by remember(incomeCategories) { mutableStateOf(incomeCategories) }

    fun swap(list: List<Category>, index: Int, target: Int): List<Category> =
        list.toMutableList().apply {
            val tmp = this[target]
            this[target] = this[index]
            this[index] = tmp
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.action_reorder_categories)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (expenseList.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.section_expenses),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )
                    expenseList.forEachIndexed { index, category ->
                        ReorderRow(
                            category = category,
                            canMoveUp = index > 0,
                            canMoveDown = index < expenseList.lastIndex,
                            onMoveUp = {
                                expenseList = swap(expenseList, index, index - 1)
                                onReorderExpense(expenseList)
                            },
                            onMoveDown = {
                                expenseList = swap(expenseList, index, index + 1)
                                onReorderExpense(expenseList)
                            }
                        )
                    }
                }

                if (incomeList.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.section_income),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                    incomeList.forEachIndexed { index, category ->
                        ReorderRow(
                            category = category,
                            canMoveUp = index > 0,
                            canMoveDown = index < incomeList.lastIndex,
                            onMoveUp = {
                                incomeList = swap(incomeList, index, index - 1)
                                onReorderIncome(incomeList)
                            },
                            onMoveDown = {
                                incomeList = swap(incomeList, index, index + 1)
                                onReorderIncome(incomeList)
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) }
        }
    )
}

@Composable
private fun ReorderRow(
    category: Category,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val defaultColor = MaterialTheme.colorScheme.primary
    val categoryColor = remember(category.colorHex, defaultColor) {
        runCatching { Color(category.colorHex.toColorInt()) }.getOrDefault(defaultColor)
    }
    val iconResId = ALL_ICONS_FLAT[category.icon] ?: R.drawable.ic_category

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                tint = categoryColor,
                modifier = Modifier.size(20.dp)
            )
            Text(category.name, style = MaterialTheme.typography.bodyMedium)
        }
        Row {
            IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = stringResource(R.string.action_move_up))
            }
            IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = stringResource(R.string.action_move_down))
            }
        }
    }
}