package com.crisdema.tracky.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category @JvmOverloads constructor(
    @PrimaryKey val id: String = "",
    val spaceId: String = "",
    val name: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val colorHex: String = "#6750A4",
    val icon: String = "category"
)
