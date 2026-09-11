package com.crisdema.tracky.data.repository

import com.crisdema.tracky.data.local.CategoryDao
import com.crisdema.tracky.data.model.Category
import com.crisdema.tracky.data.remote.FirestoreSyncRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val dao: CategoryDao,
    private val remote: FirestoreSyncRepository,
    private val appScope: CoroutineScope
) {
    fun observeCategories(spaceId: String): Flow<List<Category>> =
        dao.observeForSpace(spaceId)

    /** Call once per space when it's opened, to start mirroring remote categories locally. */
    fun startRemoteSync(spaceId: String) {
        appScope.launch(Dispatchers.IO) {
            remote.observeCategories(spaceId).collect { remoteCategories ->
                dao.upsertAll(remoteCategories)
                dao.deleteMissing(spaceId, remoteCategories.map { it.id })
            }
        }
    }

    suspend fun addCategory(category: Category) {
        dao.upsert(category)
        appScope.launch(Dispatchers.IO) {
            runCatching { remote.pushCategories(listOf(category)) }
        }
    }

    suspend fun updateCategory(category: Category) {
        addCategory(category)
    }

    suspend fun deleteCategory(spaceId: String, id: String) {
        dao.delete(id)
        appScope.launch(Dispatchers.IO) {
            runCatching { remote.deleteCategory(spaceId, id) }
        }
    }
}