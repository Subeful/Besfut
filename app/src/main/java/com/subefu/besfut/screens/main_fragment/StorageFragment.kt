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
    lateinit var binding: FragmentStorageBinding
    lateinit var rv: RecyclerView

    lateinit var dao: Dao

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentStorageBinding.inflate(layoutInflater)

        init()

        return binding.root
    }

    fun init(){
        dao = MyDatabase.getDb(binding.root.context).getDao()

        loadItems()
    }

    fun loadItems(){
        dao.getAllRemindingItems().asLiveData().observe(viewLifecycleOwner){
            if (it == null) return@observe

            val listCategory = HashMap<String, MutableList<DbStorageItem>>()
            val listItem = ArrayList<ModelGroupStorage>()

            lifecycleScope.launch(Dispatchers.IO) {
                for(item in it){
                    val categoryName = dao.getCategoryNameById(item.groupId)
                    Log.d("STORAGE", "category name - $categoryName")
                    if (listCategory.containsKey(categoryName)){
                        listCategory[categoryName]!!.add(item)
                        Log.d("STORAGE", "category exist - ${item.name}")
                    }
                    else {
                        listCategory.put(categoryName, mutableListOf(item))
                        Log.d("STORAGE", "category not exist - ${item.name}")
                    }
                }
                listCategory.forEach { category, goods ->
                    listItem.add(ModelGroupStorage(category, goods))
                }

                lifecycleScope.launch(Dispatchers.Main) {
                    binding.rvGroupStorage.adapter = createGroupAdapter(listItem)
                }
            }
        }
    }
    fun createGroupAdapter(goods: List<ModelGroupStorage>): GroupAdapter<ModelGroupStorage> {
        return GroupAdapter(
            goods,
            object : BindViewHolder<ModelGroupStorage> {
                override fun bind(view: View, item: ModelGroupStorage, position: Int, listener: (out: Int, inn: Int) -> Unit) {
                    bindGroupView(view, item, position, listener)
                }
            },
            { out, inn ->
                val item = goods[out].listItems[inn]
                spentItem(item)
                Log.d("INFO", "${item.name} - ${item.count}")
            }
        )
    }
    fun bindGroupView(view: View, item: ModelGroupStorage, position: Int, listener: (out: Int, inn: Int) -> Unit) {
        view.findViewById<TextView>(R.id.group_name).text = item.name
        val rv = view.findViewById<RecyclerView>(R.id.rv_items)
        rv.layoutManager = GridLayoutManager(requireContext(), 2)
        rv.adapter = createItemAdapter(item.listItems, position, listener)
    }
    fun createItemAdapter(items: List<DbStorageItem>, groupPosition: Int, listener: (out: Int, inn: Int) -> Unit): ItemAdapter<DbStorageItem> {
        return ItemAdapter(
            R.layout.model_storage_item,
            items,
            { view, item ->
                view.findViewById<TextView>(R.id.item_storage_name).text = item.name
                view.findViewById<TextView>(R.id.item_storage_price).text = item.count.toString()
            },
            { inner -> listener(groupPosition, inner) }
        )
    }

    fun spentItem(items: DbStorageItem){
        lifecycleScope.launch(Dispatchers.IO) {
            val quantity = dao.getItemById(items.itemId).quantity
            dao.spentStorageItem(items.itemId, quantity)
        }
    }
}