package com.example.geschenkapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comments")
data class Comment (
    @ColumnInfo(name= "user_id")
    val user_id : Int,
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int=0,
    @ColumnInfo(name = "comment_id")
    val comment_id : Int,
    @ColumnInfo(name = "gift_id")
    val gift_id : Int,
    @ColumnInfo(name = "first_name")
    val first_name : String,
    @ColumnInfo(name = "last_name")
    val last_name : String,
    @ColumnInfo(name = "content")
    val title: String,
    @ColumnInfo(name = "likes")
    val likes: Int
)