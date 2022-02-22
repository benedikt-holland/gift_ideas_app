package com.example.geschenkapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "friends")
data class Friend (
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    val id : Int=0,
    @ColumnInfo(name="first_name")
    val first_name : String,
    @ColumnInfo(name="last_name")
    val last_name : String,
    @ColumnInfo(name = "user_id")
    val user_id : Int,
    @ColumnInfo(name="date_of_birth")
    val data_of_birth : Date,
    @ColumnInfo(name = "is_favourite")
    val is_favourite : Int,
    @ColumnInfo(name = "age")
    val age : Int,
    @ColumnInfo(name = "count_gifts")
    val count_gifts : Int,
    @ColumnInfo(name = "remaining")
    val remaining : Int,
    @ColumnInfo(name = "friend_id")
    val friend_id: Int
)