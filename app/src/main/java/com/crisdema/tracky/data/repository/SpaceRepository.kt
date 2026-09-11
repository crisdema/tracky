package com.crisdema.tracky.data.repository

import com.crisdema.tracky.data.local.SpaceDao
import com.crisdema.tracky.data.local.CategoryDao
import com.crisdema.tracky.data.model.Space
import com.crisdema.tracky.data.remote.FirestoreSyncRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpaceRepository @Inject constructor(
    private val spaceDao: SpaceDao,
    private val categoryDao: CategoryDao,
    private val remote: FirestoreSyncRepository,
    private val appScope: CoroutineScope
) {
    fun startRemoteSync(uid: String) {
        appScope.launch(Dispatchers.IO) {
            remote.observeUserSpaces(uid).collect { spaces ->
                spaces.forEach { spaceDao.upsert(it) }
            }
        }
    }

    suspend fun getOrCreateDefaultSpace(uid: String): Space {
        val existing = remote.getUserSpacesOnce(uid)
        if (existing.isNotEmpty()) {
            // Prefer a shared space (more than one member) over a personal-only space.
            val space = existing.firstOrNull { it.memberIds.size > 1 } ?: existing.first()
            spaceDao.upsert(space)
            return space
        }
        return createSpace("My Space", uid)
    }

    fun observeUserSpaces(uid: String): Flow<List<Space>> = remote.observeUserSpaces(uid)

    fun observeSpace(spaceId: String): Flow<Space?> = remote.observeSpace(spaceId)

    suspend fun createSpace(name: String, ownerUid: String): Space {
        val space = Space(
            id = UUID.randomUUID().toString(),
            name = name,
            ownerId = ownerUid,
            memberIds = listOf(ownerUid)
        )
        remote.createSpace(space)
        spaceDao.upsert(space)
        return space
    }

    suspend fun joinSpace(spaceId: String, uid: String): Result<Unit> {
        return try {
            remote.joinSpace(spaceId, uid)
            val spaces = remote.getUserSpacesOnce(uid)
            spaces.forEach { spaceDao.upsert(it) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun renameSpace(spaceId: String, newName: String) {
        remote.renameSpace(spaceId, newName)
    }
}