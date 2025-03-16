package com.subefu.besfut.screens

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.room.Dao
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.subefu.besfut.R
import com.subefu.besfut.databinding.ActivityMainBinding
import com.subefu.besfut.db.DbCategory
import com.subefu.besfut.db.DbItem
import com.subefu.besfut.db.DbReward
import com.subefu.besfut.db.DbState
import com.subefu.besfut.db.DbStorageItem
import com.subefu.besfut.db.MyDatabase
import com.subefu.besfut.screens.main_fragment.RewardFragment
import com.subefu.besfut.screens.main_fragment.StatisticFragment
import com.subefu.besfut.screens.main_fragment.StorageFragment
import com.subefu.besfut.screens.main_fragment.StoreFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var dao: com.subefu.besfut.db.Dao

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    fun init(){
        hideSystemPanel()
        setListenerBotNav()
        setConfigBotNav()

        initialDatabase()
    }

    fun initialDatabase(){
        dao = MyDatabase.getDb(this).getDao()
        lifecycleScope.launch(Dispatchers.IO) {
            if(dao.checkExistConfig() == 0) {
                dao.createState(DbState(coinLimitDay = 1000))

                loadSystemItem()
                loadSystemReward()
                loadSystemStorage()

                setFragment(StoreFragment())
            }
        }
    }
    fun loadSystemItem(){
        val category_id = dao.getMaxIdFromCategory()
        val item_id = dao.getMaxIdFromItem()
        dao.createCategories(listOf(
            DbCategory(category_id+1, "Развлечения", 1),
            DbCategory(category_id+2, "Пропуски", 1))
        )

        val name_1 = dao.getCategoryIdByName("Развлечения")
        val name_2 = dao.getCategoryIdByName("Пропуски")
        dao.createItems(listOf(
            DbItem(item_id + 1,  "Телефон", 30, name_1, 30),
            DbItem(item_id + 2,  "Музыка", 30, name_1, 1),
            DbItem(item_id + 3,  "Купить что-то", 30, name_1, 1),
            DbItem(item_id + 4,  "Запретка", 30, name_1, 1),
            DbItem(item_id + 5,  "Купон на еду", 30, name_1, 100),
        ))
        dao.createItems(listOf(
            DbItem(item_id + 6, "Выходной", 30, name_2, 1),
            DbItem(item_id + 7, "Пропуск занятия", 30, name_2, 1),
            DbItem(item_id + 8, "Пропуск трени", 30, name_2, 1),
            DbItem(item_id + 9, "Отложить сон", 30, name_2, 30),
        ))
    }

    fun loadSystemReward(){
        val category_id = dao.getMaxIdFromCategory()

        dao.createCategories(
            listOf(
                DbCategory(category_id+1, "Спорт и здоровье", 0),
                DbCategory(category_id+2, "Учеба и развитие", 0),
            )
        )
        dao.createRewards(
            listOf(
                DbReward(1, "Зарядка", 20, category_id+1, 0),
                DbReward(1, "Прогулка", 20, category_id+1, 0),
                DbReward(1, "Сон > 8ч.", 20, category_id+1, 0),
                DbReward(1, "Тренировка", 20, category_id+1, 0),
            )
        )
        dao.createRewards(
            listOf(
                DbReward(1, "Что-то новое", 20, category_id+2, 0),
                DbReward(1, "+20 слов", 20, category_id+2, 0),
                DbReward(1, "Прочитать главу.", 20, category_id+2, 0),
                DbReward(1, "Android", 20, category_id+2, 0),
            )
        )
    }
    fun loadSystemStorage(){
        dao.createRecordsInStorage(
            dao.getAllItems().map { x -> DbStorageItem(x.id, itemId = x.id, name = x.name, groupId = x.categoryId, count = 0) }.toList()
        )
    }

    fun setListenerBotNav(){
        binding.botnav.setOnItemSelectedListener{
            when(it.itemId){
                R.id.menu_store -> setFragment(StoreFragment())
                R.id.menu_storage -> setFragment(StorageFragment())
                R.id.menu_reward -> setFragment(RewardFragment())
                R.id.menu_statistic -> setFragment(StatisticFragment())
            }
            true
        }
    }
    fun setConfigBotNav(){
        binding.botnav.apply {
            itemActiveIndicatorColor = getColorStateList(R.color.transparent)
            isItemActiveIndicatorEnabled = true
            labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_SELECTED
            selectedItemId = R.id.menu_store}
    }

    fun setFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(binding.frameLayout.id, fragment).commit()
    }

    fun hideSystemPanel(){
        this.window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
    }
}