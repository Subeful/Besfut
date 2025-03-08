package com.subefu.besfut.screens.main_fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.subefu.besfut.R
import com.subefu.besfut.adapters.SeriesAdapter
import com.subefu.besfut.databinding.FragmentRewardBinding
import com.subefu.besfut.databinding.FragmentStatisticBinding
import com.subefu.besfut.models.ModelSeriesItem

class StatisticFragment : Fragment() {
    lateinit var binding: FragmentStatisticBinding
    lateinit var rv_series: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentStatisticBinding.inflate(inflater)

        rv_series = binding.rvSeries
        rv_series.adapter = SeriesAdapter(requireContext(), listOf(
            ModelSeriesItem("item_1", 20),
            ModelSeriesItem("item_2", 10),
            ModelSeriesItem("item_3", 20),
            ModelSeriesItem("item_4", 40),
            ModelSeriesItem("item_5", 12),
            ModelSeriesItem("item_6", 23),
        ))

        return binding.root
    }
}