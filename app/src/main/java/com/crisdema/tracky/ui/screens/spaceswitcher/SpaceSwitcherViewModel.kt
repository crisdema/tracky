package com.crisdema.tracky.ui.screens.spaceswitcher

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crisdema.tracky.data.repository.SpaceRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class SpaceListItem(
    val id: String,
    val name: String,
    val memberCount: Int,
    val isActive: Boolean
)

@HiltViewModel
class SpaceSwitcherViewModel @Inject constructor(
    private val spaceRepository: SpaceRepository,
    auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val currentSpaceId: String = savedStateHandle.get<String>("spaceId") ?: ""
    private val uid: String = auth.currentUser?.uid ?: ""

    val spaces: StateFlow<List<SpaceListItem>> = spaceRepository.observeUserSpaces(uid)
        .map { spaces ->
            spaces.map { space ->
                SpaceListItem(
                    id = space.id,
                    name = space.name,
                    memberCount = space.memberIds.size,
                    isActive = space.id == currentSpaceId
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}