package com.example.geschenkapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.geschenkapp.data.*

@Database(entities = [Comment::class, Event::class, Friend::class, Gift::class, Member::class, User::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun giftDao(): GiftDao
}