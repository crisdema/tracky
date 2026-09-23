package com.crisdema.tracky.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.crisdema.tracky.data.model.Budget
import com.crisdema.tracky.data.model.Category
import com.crisdema.tracky.data.model.Space
import com.crisdema.tracky.data.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE spaceId = :spaceId ORDER BY date DESC")
    fun observeForSpace(spaceId: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE pendingSync = 1")
    suspend fun getPendingSync(): List<Transaction>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Transaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(transaction: Transaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(transactions: List<Transaction>)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM transactions WHERE spaceId = :spaceId AND pendingSync = 0 AND id NOT IN (:keepIds)")
    suspend fun deleteMissing(spaceId: String, keepIds: List<String>)

    @Query("""
        SELECT note FROM transactions
        WHERE spaceId = :spaceId AND categoryId = :categoryId AND note != ''
        GROUP BY note
        ORDER BY COUNT(*) DESC, MAX(date) DESC
        LIMIT :limit
    """)
    suspend fun getFrequentNotes(spaceId: String, categoryId: String, limit: Int = 8): List<String>
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE spaceId = :spaceId ORDER BY `order` ASC")
    fun observeForSpace(spaceId: String): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE spaceId = :spaceId ORDER BY `order` ASC")
    suspend fun getCategoriesOnce(spaceId: String): List<Category>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(category: Category)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(categories: List<Category>)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM categories WHERE spaceId = :spaceId AND id NOT IN (:keepIds)")
    suspend fun deleteMissing(spaceId: String, keepIds: List<String>)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE spaceId = :spaceId AND yearMonth = :yearMonth")
    fun observeForMonth(spaceId: String, yearMonth: String): Flow<List<Budget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: Budget)
}

@Dao
interface SpaceDao {
    @Query("SELECT * FROM spaces WHERE ownerId = :uid OR memberIds LIKE '%' || :uid || '%'")
    fun observeForUser(uid: String): Flow<List<Space>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(space: Space)

    @Update
    suspend fun update(space: Space)
}