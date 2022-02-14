package com.example.geschenkapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class Member (
    @ColumnInfo(name = "gift_id")
    val gift_id : Int,
    @ColumnInfo(name = "user_id")
    val user_id : Int,
    @ColumnInfo(name = "max_price")
    val max_price : Int,
    @ColumnInfo(name = "isFixed")
    val isFixed : Boolean,
    @PrimaryKey(autoGenerate = true)
    val id: Int=0
)