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
    @ColumnInfo(name = "email")
    val email : String,
    @ColumnInfo(name = "profile_privacy")
    val profile_privacy : Int,
    @ColumnInfo(name = "profile_picture")
    val profile_picture : String,
    @PrimaryKey(autoGenerate = true)
    val id: Int=0
)