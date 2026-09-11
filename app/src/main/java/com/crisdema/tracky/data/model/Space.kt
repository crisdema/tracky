package com.crisdema.tracky.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spaces")
data class Space @JvmOverloads constructor(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val ownerId: String = "",
    val memberIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "budgets")
data class Budget @JvmOverloads constructor(
    @PrimaryKey val id: String = "",
    val spaceId: String = "",
    val categoryId: String = "",
    val monthlyLimit: Double = 0.0,
    val yearMonth: String = ""
)
