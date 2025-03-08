package com.subefu.besfut.screens

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.subefu.besfut.R
import com.subefu.besfut.databinding.ActivityMainBinding
import com.subefu.besfut.screens.main_fragment.RewardFragment
import com.subefu.besfut.screens.main_fragment.StatisticFragment
import com.subefu.besfut.screens.main_fragment.StoreFragment

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var botnav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        hideSystemPanel()
        setContentView(binding.root)

        init()

        botnav.setOnItemSelectedListener{
            when(it.itemId){
                R.id.menu_store -> setFragment(StoreFragment())
                R.id.menu_reward -> setFragment(RewardFragment())
                R.id.menu_statistic -> setFragment(StatisticFragment())
            }
            true
        }
    }

    fun init(){
        setConfigBotNav()
        setFragment(StoreFragment())
    }

    fun setConfigBotNav(){
        botnav = binding.botnav
        botnav.itemActiveIndicatorColor = getColorStateList(R.color.transparent)
        botnav.isItemActiveIndicatorEnabled = true
        botnav.labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_SELECTED
        botnav.selectedItemId = R.id.menu_store
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