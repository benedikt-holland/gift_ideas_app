package com.example.geschenkapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

//Data class with data for the gift feed on the user profile page and the home screen
@Entity(tableName = "gifts")
data class Gift (
    @ColumnInfo(name="id")
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "price")
    val price: Int,
    @ColumnInfo(name = "user_id")
    val user_id: Int,
    @ColumnInfo(name = "owner_id")
    val owner_id: Int,
    @ColumnInfo(name = "is_wish")
    val is_wish: Int,
    @ColumnInfo(name = "post_privacy")
    val post_privacy : Int,
    @ColumnInfo(name= "gift_picture")
    val gift_picture: String,
    @ColumnInfo(name= "gift_link")
    val gift_link: String,
    @ColumnInfo(name = "is_closed")
    val is_closed : Int,
    @ColumnInfo(name= "user_first_name")
    val user_first_name : String,
    @ColumnInfo(name= "user_last_name")
    val user_last_name : String,
    @ColumnInfo(name= "owner_first_name")
    val owner_first_name : String,
    @ColumnInfo(name= "owner_last_name")
    val owner_last_name : String,
    @ColumnInfo(name= "member_count")
    val member_count : Int,
    @ColumnInfo(name = "likes")
    val likes: Int,
)