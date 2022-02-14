package com.example.geschenkapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "events")
data class Event (
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "date")
    val date: Date,
    @ColumnInfo(name = "owner_id")
    val owner_id: Int,
    @PrimaryKey(autoGenerate = true)
    val id: Int=0
)