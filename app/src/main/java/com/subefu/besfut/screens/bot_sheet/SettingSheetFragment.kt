package com.subefu.besfut.screens.bot_sheet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.subefu.besfut.R
import com.subefu.besfut.databinding.FragmentSettingSheetBinding

class SettingSheetFragment : BottomSheetDialogFragment() {

    lateinit var binding: FragmentSettingSheetBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSettingSheetBinding.inflate(inflater)


        return binding.root
    }
}