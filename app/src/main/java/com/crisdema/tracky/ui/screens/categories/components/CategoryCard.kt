package com.crisdema.tracky.ui.screens.categories.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
    isHighlighted: Boolean = false,
    emphasizeColor: Boolean = true,
    onHighlightFinished: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    val defaultColor = MaterialTheme.colorScheme.primary
    val categoryColor = remember(category.colorHex, defaultColor) {
        runCatching { Color(category.colorHex.toColorInt()) }
            .getOrDefault(defaultColor)
    }
    val iconResId = ALL_ICONS_FLAT[category.icon] ?: R.drawable.ic_category

    val haptic = LocalHapticFeedback.current
    val pulse = remember { Animatable(0f) }
    LaunchedEffect(isHighlighted) {
        if (isHighlighted) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            pulse.animateTo(1f, animationSpec = tween(750))
            pulse.animateTo(0f, animationSpec = tween(500))
            onHighlightFinished()
        }
    }

    val strokeWidth = if (isSelected) 2.5.dp else 1.5.dp

    val baseBorderAlpha = if (emphasizeColor) 1f else 0f
    val borderAlpha = baseBorderAlpha + (1f - baseBorderAlpha) * pulse.value

    val baseContainerAlpha = if (emphasizeColor && isSelected) 0.14f else 0f
    val containerAlpha = baseContainerAlpha + (0.32f - baseContainerAlpha) * pulse.value
    val containerColor = lerp(MaterialTheme.colorScheme.surface, categoryColor, containerAlpha)

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
            colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
            border = BorderStroke(strokeWidth, categoryColor.copy(alpha = borderAlpha))
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