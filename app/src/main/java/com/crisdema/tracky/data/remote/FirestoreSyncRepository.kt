package com.crisdema.tracky.data.remote

import com.crisdema.tracky.data.model.Space
import com.crisdema.tracky.data.model.Category
import com.crisdema.tracky.data.model.Transaction
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore layout:
 *   spaces/{spaceId}/transactions/{id}
 *   spaces/{spaceId}/categories/{id}
 *   spaces/{spaceId}/budgets/{id}
 *
 * Security rules (Firebase console):
 *   match /spaces/{spaceId} {
 *     allow read, write: if request.auth.uid in resource.data.memberIds;
 *     match /{document=**} {
 *       allow read, write: if request.auth.uid in
 *         get(/databases/$(database)/documents/spaces/$(spaceId)).data.memberIds;
 *     }
 *   }
 */
@Singleton
class FirestoreSyncRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun spacesRef() = firestore.collection("spaces")
    private fun transactionsRef(spaceId: String) =
        spacesRef().document(spaceId).collection("transactions")

    private fun categoriesRef(spaceId: String) =
        spacesRef().document(spaceId).collection("categories")

    suspend fun getUserSpacesOnce(uid: String): List<Space> {
        val snapshot = spacesRef().whereArrayContains("memberIds", uid).get().await()
        return snapshot.documents.mapNotNull { it.toObject(Space::class.java) }
    }

    fun observeUserSpaces(uid: String): Flow<List<Space>> = callbackFlow {
        val registration: ListenerRegistration = spacesRef()
            .whereArrayContains("memberIds", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error); return@addSnapshotListener
                }
                val spaces = snapshot?.documents?.mapNotNull { it.toObject(Space::class.java) }
                trySend(spaces ?: emptyList())
            }
        awaitClose { registration.remove() }
    }

    fun observeSpace(spaceId: String): Flow<Space?> = callbackFlow {
        val registration = spacesRef().document(spaceId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error); return@addSnapshotListener
                }
                trySend(snapshot?.toObject(Space::class.java))
            }
        awaitClose { registration.remove() }
    }

    suspend fun renameSpace(spaceId: String, newName: String) {
        spacesRef().document(spaceId).update("name", newName).await()
    }

    fun observeTransactions(spaceId: String): Flow<List<Transaction>> = callbackFlow {
        val registration = transactionsRef(spaceId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error); return@addSnapshotListener
                }
                val txns = snapshot?.documents?.mapNotNull { it.toObject(Transaction::class.java) }
                trySend(txns ?: emptyList())
            }
        awaitClose { registration.remove() }
    }

    fun observeCategories(spaceId: String): Flow<List<Category>> = callbackFlow {
        val registration = categoriesRef(spaceId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error); return@addSnapshotListener
                }
                val categories = snapshot?.documents?.mapNotNull { it.toObject(Category::class.java) }
                trySend(categories ?: emptyList())
            }
        awaitClose { registration.remove() }
    }

    suspend fun pushCategories(categories: List<Category>) {
        val batch = firestore.batch()
        categories.forEach { category ->
            batch.set(categoriesRef(category.spaceId).document(category.id), category)
        }
        batch.commit().await()
    }

    suspend fun deleteCategory(spaceId: String, categoryId: String) {
        categoriesRef(spaceId).document(categoryId).delete().await()
    }

    suspend fun deleteCategory(category: Category) {
        deleteCategory(category.spaceId, category.id)
    }

    suspend fun updateCategoryOrders(spaceId: String, orderedUpdates: List<Pair<String, Long>>) {
        val batch = firestore.batch()
        orderedUpdates.forEach { (categoryId, newOrder) ->
            batch.update(categoriesRef(spaceId).document(categoryId), "order", newOrder)
        }
        batch.commit().await()
    }

    suspend fun pushTransaction(transaction: Transaction) {
        transactionsRef(transaction.spaceId)
            .document(transaction.id)
            .set(transaction)
            .await()
    }

    suspend fun deleteTransaction(spaceId: String, transactionId: String) {
        transactionsRef(spaceId).document(transactionId).delete().await()
    }

    suspend fun createSpace(space: Space) {
        spacesRef().document(space.id).set(space).await()
    }

    suspend fun joinSpace(spaceId: String, uid: String) {
        firestore.collection("spaces")
            .document(spaceId)
            .update("memberIds", FieldValue.arrayUnion(uid))
            .await()
    }

}