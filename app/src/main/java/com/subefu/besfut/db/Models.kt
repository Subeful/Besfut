package com.subefu.besfut.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "item")
data class DbItem(
    @ColumnInfo(name = "id")
    var id: Int = 0,
    @PrimaryKey
    @ColumnInfo(name = "name")
    var name: String = "",
    @ColumnInfo(name = "price")
    var price: Int = 0,
    @ColumnInfo(name = "categoryId")
    var categoryId: Int = 0,
    @ColumnInfo(name = "quantity")
    var quantity: Int
)

@Entity(tableName = "category",)
data class DbCategory(
    @ColumnInfo(name = "id")
    var id: Int = 0,
    @PrimaryKey
    @ColumnInfo(name = "name")
    var name: String = "",
    @ColumnInfo(name = "isItems")
    var isItems: Int
)

@Entity(tableName = "reward")
data class DbReward(
    @ColumnInfo(name = "id")
    var id: Int = 0,
    @PrimaryKey
    @ColumnInfo(name = "name")
    var name: String = "",
    @ColumnInfo(name = "price")
    var price: Int = 0,
    @ColumnInfo(name = "categoryId")
    var categoryId: Int = 0,
    @ColumnInfo(name = "series")
    var series: Int = 0
)

@Entity(tableName = "state")
data class DbState(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @ColumnInfo(name = "amountCoin")
    var amountCoin: Int = 0,
    @ColumnInfo(name = "amountExp")
    var amountExp: Int = 0,
    @ColumnInfo(name = "lvl")
    var lvl: Int = 0,

    @ColumnInfo(name = "coinInDay")
    var coinInDay: Int = 0,
    @ColumnInfo(name = "coinLimitDay")
    var coinLimitDay: Int = 100,

    @ColumnInfo(name = "activeInDay")
    var activeInDay: Int = 0,
    @ColumnInfo(name = "activeLimitDay")
    var activeLimitDay: Int = 100,

    @ColumnInfo(name = "currentDay")
    var currentDay: String = ""
    )

@Entity(tableName = "experience")
data class DbExp(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0,
    @ColumnInfo(name = "date")
    var date: String = "",
    @ColumnInfo(name = "earn")
    var earn: Int = 0
)

@Entity(tableName = "coin")
data class DbCoin(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0,
    @ColumnInfo(name = "date")
    var date: String = "",
    @ColumnInfo(name = "earn")
    var earn: Int = 0
)

@Entity(tableName = "history", indices = [Index(value = ["date", "rewardId"], name = "idx_history_date_reward")])
data class DbHistory(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0,
    @ColumnInfo(name = "date")
    var date: String = "",
    @ColumnInfo(name = "rewardId")
    var rewardId: String = "",
    @ColumnInfo(name = "varue")
    var varue: Int = 0
)

@Entity(tableName = "storage")
data class DbStorageItem(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    @ColumnInfo("itemId")
    var itemId: Int = 0,
    @ColumnInfo("count")
    var count: Int = 0
)



