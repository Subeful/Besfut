package com.subefu.besfut.screens.main_fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.subefu.besfut.R
import com.subefu.besfut.adapters.GroupAdapter
import com.subefu.besfut.databinding.FragmentStoreBinding
import com.subefu.besfut.models.ModelGroup
import com.subefu.besfut.models.ModelItem

class StoreFragment : Fragment() {
   lateinit var binding: FragmentStoreBinding
   lateinit var rv: RecyclerView
   lateinit var adapter: GroupAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentStoreBinding.inflate(inflater)

        rv = binding.rvGroup
        rv.adapter = GroupAdapter(requireContext(),
            listOf(
                ModelGroup("group_1", listOf(
                    ModelItem("Купон на еду", 20),
                    ModelItem("Купить что-то", 30),
                    ModelItem("Пропуск занятия", 20),
                    ModelItem("item_4", 10),
                    ModelItem("item_4", 10),)
                ),
                ModelGroup("group_2", listOf(
                    ModelItem("item_1", 20),
                    ModelItem("item_2", 30),
                    ModelItem("item_3", 20),
                    ModelItem("item_4", 10),)
                ),
                ModelGroup("group_3", listOf(
                    ModelItem("item_1", 20),
                    ModelItem("item_2", 30),
                    ModelItem("item_3", 20),)
                ),
            )
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

}