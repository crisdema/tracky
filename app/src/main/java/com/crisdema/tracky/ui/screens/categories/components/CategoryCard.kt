package com.crisdema.tracky.ui.screens.categories.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.crisdema.tracky.R
import com.crisdema.tracky.data.model.Category
import com.crisdema.tracky.ui.screens.categories.ALL_ICONS_FLAT

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryCard(
    category: Category,
    totalAmount: Double? = null,
    currencyFormatter: ((Double) -> String)? = null,
    onClick: () -> Unit = {},
    enableActionsMenu: Boolean = true,
    isSelected: Boolean = false,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onLongClick: (() -> Unit)? = null
) {
    var showMenu by remember { mutableStateOf(false) }

    val defaultColor = MaterialTheme.colorScheme.primary
    val categoryColor = remember(category.colorHex, defaultColor) {
        runCatching { Color(category.colorHex.toColorInt()) }
            .getOrDefault(defaultColor)
    }
    val iconResId = ALL_ICONS_FLAT[category.icon] ?: R.drawable.ic_category

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = if (enableActionsMenu) {
                        { showMenu = true }
                    } else null
                ),
            colors = CardDefaults.outlinedCardColors(
                containerColor = if (isSelected) {
                    categoryColor.copy(alpha = 0.14f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ),
            border = BorderStroke(if (isSelected) 2.5.dp else 1.5.dp, categoryColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = category.name,
                    tint = categoryColor,
                    modifier = Modifier.size(20.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (totalAmount != null && currencyFormatter != null) {
                        Text(
                            text = currencyFormatter(totalAmount),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (enableActionsMenu) {
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_edit)) },
                    onClick = {
                        showMenu = false
                        onEdit()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.action_delete),
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = {
                        showMenu = false
                        onDelete()
                    }
                )
            }
        }
    }
}