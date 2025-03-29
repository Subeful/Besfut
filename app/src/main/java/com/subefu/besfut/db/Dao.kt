package com.subefu.besfut.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.subefu.besfut.models.ModelHistory
import com.subefu.besfut.models.ModelResponse
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {

    //state
        @Insert
        fun createState(state: DbState)
        @Query("select count(id) from state")
        fun checkExistConfig(): Int
        @Query("select goalAchieved = 0 from state")
        fun checkGoalAchieved(): Boolean
        @Query("update state set goalAchieved = 1")
        fun obtainGoalAchieved()
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
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun createItem(item: DbItem)
        @Insert(onConflict = OnConflictStrategy.IGNORE)
        fun createItems(items: List<DbItem>)
        @Query("select max(id) from item")
        fun getMaxIdFromItem(): Int
        @Query("select id from item where name = :name")
        fun getItemIdByName(name: String): Int
        @Query("select * from item where id = :targetId")
        fun getItemById(targetId: Int): DbItem
        @Query("select * from item where name = :name")
        fun getItemByName(name: String): DbItem
        @Query("select * from item where categoryId = :targetId")
        fun getItemsByCategoryId(targetId: Int): List<DbItem>
        @Query("select * from item")
        fun getAllItems(): List<DbItem>

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
        @Query("update storage set name = :name where id = :id")
        fun updateStorageName(id: Int, name: String)
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
        @Query("select max(id) from storage")
        fun getMaxStorageId(): Int
        @Query("select id from reward where name = :name")
        fun getRewardIdByName(name: String): Int

    //Reward
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun createReward(reward: DbReward)
        @Insert(onConflict = OnConflictStrategy.IGNORE)
        fun createRewards(rewards: List<DbReward>)
        @Query("select * from reward where categoryId = :categoryId")
        fun getRewardByCategory(categoryId: Int): List<DbReward>
        @Query("select * from reward where name = :name")
        fun getRewardByName(name: String): DbReward
        @Query("select * from reward where id = :id")
        fun getRewardById(id: Int): DbReward
        @Query("update reward set series = :newSeries where id = :id")
        fun updateSeries(id: Int, newSeries: Int)
        @Query("select max(id) from reward")
        fun getMaxRewardId(): Int

    //Action
        @Query("update state set amountCoin = amountCoin - :price, coinInDay = coinInDay + :price")
        fun buyGoods(price: Int)
        @Query("update state set amountCoin = amountCoin + :price, activeInDay = activeInDay + :price, amountExp = amountExp + :exp ")
        fun getReward(price: Int, exp: Int)
        @Query("update state set amountCoin = amountCoin + :bonus")
        fun getBonus(bonus: Int)
        @Query("update state set amountExp = :exp, lvl = :newLvl")
        fun updateLvl(exp: Int, newLvl: Int)

        @Query("select * from coin where date = :date")
        fun getCoinOfDate(date: String): DbCoin?
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun setCoin(dbCoin: DbCoin)

        @Query("select * from experience where date = :date")
        fun getExpOfDate(date: String): DbExp?
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun setExp(dbExp: DbExp)

    //Statistic
        //Coin & Exp
            @Query("select * from experience order by id desc limit 30")
            fun getExpOfMonth(): List<DbExp>
            @Query("select * from coin order by id desc limit 30")
            fun getCoinOfMonth(): List<DbCoin>
            @Query("select * from coin order by id desc")
            fun getCoinForAllTime(): List<DbCoin>
            @Query("select sum(spent) from coin")
            fun getAllSpentCoin(): Int
            @Query("select sum(earn) from coin")
            fun getAllEarnCoin(): Int
        //Reward
            @Query("select * from reward where series != -1")
            fun getRewardSeries(): List<DbReward>
            @Query("select date, value from history h join reward r on h.rewardId = r.id where r.id = :targetRewardId order by h.date")
            fun getHistoryOfReward(targetRewardId: Int): List<ModelHistory>
            @Query("select r.name, sum(h.value) as result from history h join reward r on h.rewardId = r.id group by r.name order by result desc")
            fun getBestReward(): List<ModelResponse>
            @Query("select * from history where rewardId = :id and date = :date")
            fun getRecordOfHistoryByDate(id: Int, date: String): DbHistory?
            @Insert(onConflict = OnConflictStrategy.REPLACE)
            fun setRecordHistory(record: DbHistory)
        //Item
            @Insert(onConflict = OnConflictStrategy.REPLACE)
            fun setRecordStoreHistory(record: DbStoreHistory)
            @Query("select * from store_history where itemId = :id and date = :date")
            fun getRecordOfStoreHistoryByDate(id: Int, date: String): DbStoreHistory?
            @Query("select date, value from store_history s join item i on s.itemId = i.id where i.id = :targetItemId order by s.date")
            fun getStoreHistoryOfItem(targetItemId: Int): List<ModelHistory>
}