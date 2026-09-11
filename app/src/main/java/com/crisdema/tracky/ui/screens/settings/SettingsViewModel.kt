package com.crisdema.tracky.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crisdema.tracky.data.repository.SettingsRepository
import com.crisdema.tracky.data.repository.SpaceRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CurrencyOption(val code: String, val label: String)

val CurrencyOptions = listOf(
    CurrencyOption("USD", "US Dollar ($)"),
    CurrencyOption("EUR", "Euro (€)"),
    CurrencyOption("GBP", "British Pound (£)"),
    CurrencyOption("JPY", "Japanese Yen (¥)"),
    CurrencyOption("INR", "Indian Rupee (₹)"),
    CurrencyOption("BRL", "Brazilian Real (R$)"),
    CurrencyOption("MXN", "Mexican Peso ($)"),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val spaceRepository: SpaceRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    val currencyCode: StateFlow<String> = settingsRepository.currencyCode

    private val _currentSpaceId = MutableStateFlow("")
    val currentSpaceId: StateFlow<String> = _currentSpaceId.asStateFlow()

    private val _joinedSpaceId = MutableStateFlow<String?>(null)
    val joinedSpaceId: StateFlow<String?> = _joinedSpaceId.asStateFlow()

    private val _joinError = MutableStateFlow<String?>(null)
    val joinError: StateFlow<String?> = _joinError.asStateFlow()

    val spaceName: StateFlow<String> = _currentSpaceId
        .flatMapLatest { id ->
            if (id.isBlank()) flowOf("") else spaceRepository.observeSpace(id).map { it?.name.orEmpty() }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    init {
        loadCurrentSpace()
    }

    private fun loadCurrentSpace() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val space = spaceRepository.getOrCreateDefaultSpace(uid)
            _currentSpaceId.value = space.id
        }
    }

    fun setCurrency(code: String) = settingsRepository.setCurrency(code)

    fun renameSpace(newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            spaceRepository.renameSpace(_currentSpaceId.value, trimmed)
        }
    }

    fun joinSpace(scannedSpaceId: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val result = spaceRepository.joinSpace(scannedSpaceId, uid)
            result.onSuccess {
                _currentSpaceId.value = scannedSpaceId
                _joinedSpaceId.value = scannedSpaceId
            }.onFailure {
                _joinError.value = it.message
            }
        }
    }

    fun consumeJoinedSpace() {
        _joinedSpaceId.value = null
    }

    fun clearJoinError() {
        _joinError.value = null
    }

    fun signOut() = auth.signOut()
}