package com.example.geschenkapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*
import kotlin.collections.ArrayList

@Entity(tableName = "users")
data class User (
    @ColumnInfo(name = "first_name")
    val first_name: String,
    @ColumnInfo(name = "last_name")
    val last_name: String,
    @ColumnInfo(name = "date_of_birth")
    val date_of_birth: Date,
    @ColumnInfo(name = "friend_ids")
    val friend_ids: ArrayList<Int>,
    @ColumnInfo(name = "favourite_ids")
    val favourite_ids : ArrayList<Int>,
    @ColumnInfo(name = "email")
    val email : String,
    @ColumnInfo(name = "enabledDarkmode")
    val enabledDarkmode : Boolean,
    @ColumnInfo(name = "profile_privacy")
    val profile_privacy : Int,
    @ColumnInfo(name = "post_privacy")
    val post_privacy : Int,
    @PrimaryKey(autoGenerate = true)
    val id: Int=0
)