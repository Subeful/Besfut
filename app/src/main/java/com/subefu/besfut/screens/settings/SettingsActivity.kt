package com.subefu.besfut.screens.settings

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.subefu.besfut.databinding.ActivitySettingsBinding
import com.subefu.besfut.db.DbItem
import com.subefu.besfut.db.DbReward
import com.subefu.besfut.db.ReceiveInfoItem
import com.subefu.besfut.screens.settings.bot_sheet.SettingSheetFragment

class SettingsActivity : AppCompatActivity() {
    lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.layoutStore.setOnClickListener { showBottomSheet<DbItem>("items") }
        binding.layoutReward.setOnClickListener { showBottomSheet<DbReward>("rewards") }

        hideSystemPanel()
    }

    fun <T: ReceiveInfoItem> showBottomSheet(type: String){
        SettingSheetFragment<T>(type).show(supportFragmentManager, "bottom sheet")
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