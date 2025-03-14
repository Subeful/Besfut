package com.subefu.besfut.screens.main_fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.subefu.besfut.adapters.GroupAdapter
import com.subefu.besfut.databinding.FragmentRewardBinding


class RewardFragment : Fragment() {
    lateinit var binding: FragmentRewardBinding
    lateinit var rv: RecyclerView
    lateinit var adapter: GroupAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRewardBinding.inflate(inflater)

        rv = binding.rvGroupReward
        rv.adapter = GroupAdapter(requireContext(), listOf()){i, o ->}
        return binding.root
    }

}