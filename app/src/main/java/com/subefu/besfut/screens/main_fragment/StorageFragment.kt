package com.subefu.besfut.screens.main_fragment

import android.content.Context
import android.os.Bundle
import android.util.ArraySet
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.subefu.besfut.R
import com.subefu.besfut.adapters.GroupAdapter
import com.subefu.besfut.adapters.ItemAdapter
import com.subefu.besfut.databinding.FragmentStorageBinding
import com.subefu.besfut.databinding.FragmentStoreBinding
import com.subefu.besfut.db.Dao
import com.subefu.besfut.db.DbItem
import com.subefu.besfut.db.DbStorageItem
import com.subefu.besfut.db.MyDatabase
import com.subefu.besfut.models.ModelGroup
import com.subefu.besfut.models.ModelGroupStorage
import com.subefu.besfut.utils.BindViewHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.streams.toList

class StorageFragment : Fragment() {
    var _binding: FragmentStorageBinding? = null
    val binding get() = _binding!!

    lateinit var dao: Dao

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentStorageBinding.inflate(layoutInflater)
        dao = MyDatabase.getDb(binding.root.context).getDao()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeItems()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    fun observeItems(){
        dao.getAllRemindingItems().asLiveData().observe(viewLifecycleOwner){
            it ?: return@observe
            loadItems(it)
        }
    }
    fun loadItems(items: List<DbStorageItem>) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val groupedItems = items.groupBy { item ->
                dao.getCategoryNameById(item.groupId) ?: "Unknown"
            }.map { (category, goods) ->
                ModelGroupStorage(category, goods)
            }

            withContext(Dispatchers.Main) {
                binding.rvGroupStorage.adapter = createGroupAdapter(groupedItems)
            }
        }
    }

    fun createGroupAdapter(goods: List<ModelGroupStorage>): GroupAdapter<ModelGroupStorage> {
        return GroupAdapter(
            listItem = goods,
            bindView = bindGroupView(),
            listener = { out, inn ->
                val item = goods[out].listItems[inn]
                spentItem(item)
            }
        )
    }
    fun bindGroupView() = object : BindViewHolder<ModelGroupStorage> {
        override fun bind(view: View, item: ModelGroupStorage, position: Int, listener: (out: Int, inn: Int) -> Unit) {
            view.findViewById<TextView>(R.id.group_name).text = item.name
            val rv = view.findViewById<RecyclerView>(R.id.rv_items)
            rv.layoutManager = GridLayoutManager(requireContext(), 2)
            rv.adapter = createItemAdapter(item.listItems, position, listener)
        }
    }
    fun createItemAdapter(items: List<DbStorageItem>, groupPosition: Int, listener: (out: Int, inn: Int) -> Unit)
    : ItemAdapter<DbStorageItem> {
        return ItemAdapter(
            itemLayoutRes = R.layout.model_storage_item,
            listItem = items,
            bindView = { view, item ->
                view.findViewById<TextView>(R.id.item_storage_name).text = item.name
                view.findViewById<TextView>(R.id.item_storage_price).text = item.count.toString()
            },
            listener = { inner -> listener(groupPosition, inner) }
        )
    }

    fun spentItem(items: DbStorageItem){
        lifecycleScope.launch(Dispatchers.IO) {
            val quantity = dao.getItemById(items.itemId).quantity
            dao.spentStorageItem(items.itemId, quantity)
        }
    }
}