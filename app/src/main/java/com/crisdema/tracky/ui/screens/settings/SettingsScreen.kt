package com.crisdema.tracky.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.crisdema.tracky.R
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenCategories: () -> Unit,
    onSpaceChanged: (String) -> Unit,
    onSwitchSpace: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val clipboardManager = LocalClipboardManager.current
    val currencyCode by viewModel.currencyCode.collectAsState()
    val spaceName by viewModel.spaceName.collectAsState()
    val spaceId by viewModel.currentSpaceId.collectAsState()
    val joinedSpaceId by viewModel.joinedSpaceId.collectAsState()
    val joinError by viewModel.joinError.collectAsState()
    val isJoining by viewModel.isJoining.collectAsState()

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var showSignOutConfirm by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }

    val currentCurrencyLabel = CurrencyOptions.firstOrNull { it.code == currencyCode }?.label ?: currencyCode

    LaunchedEffect(joinedSpaceId) {
        joinedSpaceId?.let {
            showJoinDialog = false
            onSpaceChanged(it)
            viewModel.consumeJoinedSpace()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_section_preferences),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingsTile(
                        icon = Icons.Default.Category,
                        label = stringResource(R.string.settings_categories),
                        onClick = onOpenCategories
                    )
                    SettingsTile(
                        icon = Icons.Default.AttachMoney,
                        label = stringResource(R.string.settings_currency),
                        value = currentCurrencyLabel,
                        onClick = { showCurrencyDialog = true }
                    )
                }
            }

            Text(
                text = stringResource(R.string.settings_section_space),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingsTile(
                        icon = Icons.Default.Edit,
                        label = stringResource(R.string.settings_space_name),
                        value = spaceName.ifBlank { stringResource(R.string.settings_unnamed_space) },
                        onClick = { showRenameDialog = true }
                    )
                    SettingsTile(
                        icon = Icons.Default.SwapHoriz,
                        label = stringResource(R.string.settings_switch_space),
                        onClick = onSwitchSpace
                    )
                    SettingsTile(
                        icon = Icons.Default.PersonAdd,
                        label = stringResource(R.string.settings_share_space),
                        onClick = { showInviteDialog = true }
                    )
                    SettingsTile(
                        icon = Icons.Default.GroupAdd,
                        label = stringResource(R.string.settings_join_space),
                        onClick = { showJoinDialog = true }
                    )
                }
            }

            Text(
                text = stringResource(R.string.settings_section_account),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                SettingsTile(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    label = stringResource(R.string.settings_logout),
                    onClick = { showSignOutConfirm = true },
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showInviteDialog) {
        InviteSpaceDialog(
            spaceId = spaceId,
            onDismiss = { showInviteDialog = false },
            onCopy = { clipboardManager.setText(AnnotatedString(spaceId)) }
        )
    }

    if (showJoinDialog) {
        JoinSpaceDialog(
            isJoining = isJoining,
            error = joinError,
            onDismiss = {
                showJoinDialog = false
                viewModel.clearJoinError()
            },
            onJoin = { enteredId -> viewModel.joinSpace(enteredId.trim()) }
        )
    }

    if (showCurrencyDialog) {
        PickerDialog(
            title = stringResource(R.string.settings_currency),
            options = CurrencyOptions.map { it.code to it.label },
            selectedKey = currencyCode,
            onDismiss = { showCurrencyDialog = false },
            onSelect = {
                viewModel.setCurrency(it)
                showCurrencyDialog = false
            }
        )
    }

    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            title = { Text(stringResource(R.string.settings_logout_confirm_title)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.signOut()
                    showSignOutConfirm = false
                    onSignedOut()
                }) {
                    Text(
                        stringResource(R.string.settings_logout),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirm = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }

    if (showRenameDialog) {
        RenameSpaceDialog(
            currentName = spaceName,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName ->
                viewModel.renameSpace(newName)
                showRenameDialog = false
            }
        )
    }
}

@Composable
private fun InviteSpaceDialog(
    spaceId: String,
    onDismiss: () -> Unit,
    onCopy: () -> Unit
) {
    var justCopied by remember { mutableStateOf(false) }

    LaunchedEffect(justCopied) {
        if (justCopied) {
            delay(1500)
            justCopied = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.settings_share_space),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.settings_share_space_title),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        onCopy()
                        justCopied = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (justCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.action_copy))
                }

                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.action_close))
                }
            }
        }
    }
}

@Composable
private fun JoinSpaceDialog(
    isJoining: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onJoin: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.GroupAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.settings_join_space),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    enabled = !isJoining,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, enabled = !isJoining) {
                        Text(stringResource(R.string.action_cancel))
                    }
                    TextButton(
                        onClick = { onJoin(text) },
                        enabled = text.isNotBlank() && !isJoining
                    ) {
                        if (isJoining) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp))
                        } else {
                            Text(stringResource(R.string.action_confirm))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsTile(
    icon: ImageVector,
    label: String,
    value: String? = null,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(icon, contentDescription = null, tint = tint)
            Column {
                Text(label, style = MaterialTheme.typography.bodyLarge, color = tint)
                if (value != null) {
                    Text(
                        value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PickerDialog(
    title: String,
    options: List<Pair<String, String>>,
    selectedKey: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                options.forEach { (code, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(code) }
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = code == selectedKey,
                            onClick = { onSelect(code) }
                        )
                        Text(
                            label,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
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
private fun RenameSpaceDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember(currentName) { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_space_name)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text.trim()) }, enabled = text.isNotBlank()) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}