package com.subefu.besfut.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {

    //state
        @Insert
        fun createState(state: DbState)
        @Query("select count(id) from state")
        fun checkExistConfig(): Int
        @Query("select * from state")
        fun getCurrentState(): Flow<DbState>
        @Query("select * from state")
        fun getState(): DbState
        @Query("select currentDay from state")
        fun getCurrentDate(): String
        @Query("update state set currentDay = :newDate, coinInDay = 0, activeInDay = 0")
        fun updateCurrentDate(newDate: String)
        @Query("select coinLimitDay - coinInDay from state")
        fun getReminderCoinInDay(): Int

    //Item
        @Insert(onConflict = OnConflictStrategy.IGNORE)
        fun createItem(item: DbItem)
        @Insert(onConflict = OnConflictStrategy.IGNORE)
        fun createItems(items: List<DbItem>)
        @Query("select max(id) from item")
        fun getMaxIdFromItem(): Int
        @Query("select * from item where categoryId = :targetId")
        fun getItemsByCategoryId(targetId: Int): List<DbItem>
        @Query("select * from item where id = :targetId")
        fun getItemById(targetId: Int): DbItem
        @Query("select * from item")
        fun getAllItems(): List<DbItem>
        @Query("select quantity from item where id = :id")
        fun getQuantityItem(id: Int): Int

    //Category
        @Insert(onConflict = OnConflictStrategy.IGNORE)
        fun createCategory(category: DbCategory)
        @Insert(onConflict = OnConflictStrategy.IGNORE)
        fun createCategories(categories: List<DbCategory>)
        @Query("select max(id) from category")
        fun getMaxIdFromCategory(): Int
        @Query("select * from category where isItems = :targetMode")
        fun getAllCategories(targetMode: Int): List<DbCategory>
        @Query("select id from category where name = :name")
        fun getCategoryIdByName(name: String): Int
        @Query("select name from category where id = :id")
        fun getCategoryNameById(id: Int): String

    //Storage
        @Insert(onConflict = OnConflictStrategy.IGNORE)
        fun createRecordInStorage(item: DbStorageItem)
        @Insert(onConflict = OnConflictStrategy.IGNORE)
        fun createRecordsInStorage(items: List<DbStorageItem>)
        @Query("select * from storage where count != 0")
        fun getAllRemindingItems(): Flow<List<DbStorageItem>>
        @Query("update storage set count = count + :newCount where itemId = :id")
        fun updateStorageItem(id: Int, newCount: Int)
        @Query("update storage set count = count - :spent where itemId = :id")
        fun spentStorageItem(id: Int, spent: Int)

    //Reward
        @Insert(onConflict = OnConflictStrategy.IGNORE)
        fun createReward(reward: DbReward)
        @Insert(onConflict = OnConflictStrategy.IGNORE)
        fun createRewards(rewards: List<DbReward>)
//        @Query("select * from reward")
//        fun getAllReward(): List<DbReward>
        @Query("select * from reward where categoryId = :categoryId")
        fun getRewardByCategory(categoryId: Int): List<DbReward>

    //Action
        @Query("update state set amountCoin = amountCoin - :price, coinInDay = coinInDay + :price")
        fun buyGoods(price: Int)
        @Query("update state set amountCoin = amountCoin + :price, activeInDay = activeInDay + :price, amountExp = amountExp + :exp ")
        fun getReward(price: Int, exp: Int)
        @Query("update state set amountCoin = amountCoin + :bonus")
        fun getBonus(bonus: Int)
        @Query("update state set amountExp = :exp, lvl = :newLvl")
        fun updateLvl(exp: Int, newLvl: Int)

}