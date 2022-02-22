package com.example.geschenkapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class Member (
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,
    @ColumnInfo(name = "gift_id")
    val gift_id : Int,
    @ColumnInfo(name = "user_id")
    val user_id : Int,
    @ColumnInfo(name = "max_price")
    val max_price : Int,
    @ColumnInfo(name = "is_fixed")
    val is_fixed : Int,
    @ColumnInfo(name = "first_name")
    val first_name : String,
    @ColumnInfo(name = "last_name")
    val last_name : String
)