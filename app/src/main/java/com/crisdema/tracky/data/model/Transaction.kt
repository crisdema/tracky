package com.crisdema.tracky.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType { INCOME, EXPENSE }

@Entity(tableName = "transactions")
data class Transaction @JvmOverloads constructor(
    @PrimaryKey val id: String = "",
    val spaceId: String = "",
    val categoryId: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val amount: Double = 0.0,
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val createdBy: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val pendingSync: Boolean = false
)
