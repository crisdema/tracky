package com.crisdema.tracky.data.repository

import com.crisdema.tracky.data.local.TransactionDao
import com.crisdema.tracky.data.model.Transaction
import com.crisdema.tracky.data.remote.FirestoreSyncRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline-first: the UI reads/writes Room only, so it works with no network.
 * A background job mirrors changes to Firestore, and Firestore's own listener
 * writes remote changes (e.g. from another household member) back into Room.
 */
@Singleton
class TransactionRepository @Inject constructor(
    private val dao: TransactionDao,
    private val remote: FirestoreSyncRepository,
    private val appScope: CoroutineScope
) {
    fun observeTransactions(spaceId: String): Flow<List<Transaction>> =
        dao.observeForSpace(spaceId)

    /** Call once per space when it's opened, to start mirroring remote changes locally. */
    fun startRemoteSync(spaceId: String) {
        appScope.launch(Dispatchers.IO) {
            remote.observeTransactions(spaceId).collect { remoteTxns ->
                dao.upsertAll(remoteTxns)
                dao.deleteMissing(spaceId, remoteTxns.map { it.id })
            }
        }
    }

    suspend fun getTransactionOnce(id: String): Transaction? = dao.getById(id)

    suspend fun addTransaction(transaction: Transaction) {
        dao.upsert(transaction.copy(pendingSync = true))
        pushIfPossible(transaction)
    }

    suspend fun deleteTransaction(spaceId: String, id: String) {
        dao.delete(id)
        appScope.launch(Dispatchers.IO) {
            runCatching { remote.deleteTransaction(spaceId, id) }
        }
    }

    private suspend fun pushIfPossible(transaction: Transaction) {
        runCatching { remote.pushTransaction(transaction) }
            .onSuccess { dao.upsert(transaction.copy(pendingSync = false)) }
        // onFailure: stays pendingSync = true in Room, will retry later
    }

    suspend fun getFrequentNotes(spaceId: String, categoryId: String): List<String> =
        dao.getFrequentNotes(spaceId, categoryId)
}
