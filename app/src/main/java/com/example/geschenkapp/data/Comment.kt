package com.example.geschenkapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comments")
data class Comment (
    @ColumnInfo(name = "content")
    val title: String,
    @ColumnInfo(name = "user_id")
    val user_id: Int,
    @ColumnInfo(name = "gift_id")
    val gift_id: Int,
    @PrimaryKey(autoGenerate = true)
    val id: Int=0
)