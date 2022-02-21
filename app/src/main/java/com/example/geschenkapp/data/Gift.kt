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
    @ColumnInfo(name= "user_first_name")
    val user_first_name : String,
    @ColumnInfo(name= "user_last_name")
    val user_last_name : String,
    @ColumnInfo(name = "owner_id")
    val owner_id: Int,
    @ColumnInfo(name= "owner_first_name")
    val owner_first_name : String,
    @ColumnInfo(name= "owner_last_name")
    val owner_last_name : String,
    @ColumnInfo(name = "is_whish")
    val is_whish: Int,
    @ColumnInfo(name = "post_privacy")
    val post_privacy : Int,
    @ColumnInfo(name= "member_count")
    val member_count : Int,
    @ColumnInfo(name="id")
    @PrimaryKey(autoGenerate = true)
    val id: Int=0
)