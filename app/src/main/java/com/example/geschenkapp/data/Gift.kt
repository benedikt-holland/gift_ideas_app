package com.example.geschenkapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gifts")
data class Gift (
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "price")
    val price: Int,
    @ColumnInfo(name = "likes")
    val likes: Int,
    @ColumnInfo(name = "user_id")
    val user_id: Int,
    @ColumnInfo(name = "owner_id")
    val owner_id: Int,
    @ColumnInfo(name = "isWhish")
    val isWhish: Boolean,
    @ColumnInfo(name = "post_privacy")
    val post_privacy : Int,
    @PrimaryKey(autoGenerate = true)
    val id: Int=0
)