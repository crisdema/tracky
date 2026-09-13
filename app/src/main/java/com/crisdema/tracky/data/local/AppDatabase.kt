package com.crisdema.tracky.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.crisdema.tracky.data.model.Space
import com.crisdema.tracky.data.model.Budget
import com.crisdema.tracky.data.model.Category
import com.crisdema.tracky.data.model.Transaction

@Database(
    entities = [Transaction::class, Category::class, Budget::class, Space::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun spaceDao(): SpaceDao
}
