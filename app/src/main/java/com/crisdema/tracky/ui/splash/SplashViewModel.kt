package com.crisdema.tracky.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crisdema.tracky.data.repository.SpaceRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SplashState {
    data object Loading : SplashState
    data object LoggedOut : SplashState
    data class LoggedIn(val spaceId: String) : SplashState
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    auth: FirebaseAuth,
    private val spaceRepository: SpaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashState>(SplashState.Loading)
    val uiState: StateFlow<SplashState> = _uiState

    init {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _uiState.value = SplashState.LoggedOut
        } else {
            viewModelScope.launch {
                val space = spaceRepository.getOrCreateDefaultSpace(uid)
                _uiState.value = SplashState.LoggedIn(space.id)
            }
        }
    }
}