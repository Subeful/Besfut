package com.subefu.besfut.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DbItem::class, DbCategory::class, DbReward::class, DbStorageItem::class, DbState::class, DbExp::class, DbCoin::class, DbHistory::class, DbStoreHistory::class], version = 1)
abstract class MyDatabase : RoomDatabase(){
    abstract fun getDao(): Dao
    companion object{
        fun getDb(context: Context) = Room.databaseBuilder(context.applicationContext, MyDatabase::class.java, "myDatabase.db").build()
    }
}