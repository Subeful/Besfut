package com.subefu.besfut.screens.settings.bot_sheet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.subefu.besfut.R
import com.subefu.besfut.adapters.GroupAdapter
import com.subefu.besfut.adapters.ItemAdapter
import com.subefu.besfut.databinding.AlertCreateCategoryBinding
import com.subefu.besfut.databinding.AlertCreateItemBinding
import com.subefu.besfut.databinding.FragmentSettingSheetBinding
import com.subefu.besfut.db.Dao
import com.subefu.besfut.db.DbCategory
import com.subefu.besfut.db.DbItem
import com.subefu.besfut.db.DbReward
import com.subefu.besfut.db.DbStorageItem
import com.subefu.besfut.db.MyDatabase
import com.subefu.besfut.db.ReceiveInfoItem
import com.subefu.besfut.models.ModelGroup
import com.subefu.besfut.models.ReceiveObj
import com.subefu.besfut.utils.BindViewHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingSheetFragment<T: ReceiveInfoItem>(val type: String) : BottomSheetDialogFragment() {

    lateinit var binding: FragmentSettingSheetBinding
    lateinit var dao: Dao
    var dialog: AlertDialog? = null
    val mode = if(type == "items") 1 else 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSettingSheetBinding.inflate(inflater)

        init()

        binding.btnAddCategory.setOnClickListener {
            showAlertCreateCategory()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dialog?.dismiss()
        dialog = null
    }

    fun showAlertCreateCategory(){
        val binding = AlertCreateCategoryBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = AlertDialog.Builder(requireContext()).setView(binding.root).create()

        lifecycleScope.launch {
            binding.btnCancel.setOnClickListener { dialog.cancel() }
            binding.btCreate.setOnClickListener {
                val name = binding.editName.text.toString()

                createCategory(name)
                dialog.cancel()
            }

            dialog.getWindow()?.setBackgroundDrawableResource(R.drawable.shape_item)
            dialog.show()
        }
    }
    fun createCategory(name: String){
        lifecycleScope.launch(Dispatchers.IO) {
            val currentId = dao.getMaxIdFromCategory()+1
            val category = DbCategory(currentId, name, mode)
            dao.createCategory(category)
            Log.d("Setting answer", "${name}")
            withContext(Dispatchers.Main){
                loadItemsInRv()
            }
        }
    }

    fun init(){
        Log.d("Settings", "init")
        binding.textTitle.text = type
        dao = MyDatabase.getDb(requireContext()).getDao()

        loadItemsInRv()
    }

    fun loadItemsInRv(){
        Log.d("Settings", "start load")
        lifecycleScope.launch(Dispatchers.IO) {
            val listModelGroups = ArrayList<ModelGroup<T>>()

            when(type){
                "items" -> {
                    val categories = dao.getAllCategories(1)
                    Log.d("Settings", "categories: $categories")
                    for(category in categories){
                        val listItems = dao.getItemsByCategoryId(category.id)
                        Log.d("Settings", listItems.toString())
                        listModelGroups.add(ModelGroup(category.name, listItems as List<T>))
                    }
                }
                "rewards" -> {
                    val listCategory = dao.getAllCategories(0)
                    Log.d("Settings", "categories: $listCategory")
                    for(category in listCategory){
                        val listItems = dao.getRewardByCategory(category.id)
                        Log.d("Settings", listItems.toString())
                        listModelGroups.add(ModelGroup(category.name, listItems as List<T>))
                    }
                }
                else -> {}
            }

            Log.d("Settings", "list: " + listModelGroups.toString())
            lifecycleScope.launch(Dispatchers.Main) {
                binding.recyclerItems.adapter = createGroupAdapter(listModelGroups)
            }
        }
    }

    fun createGroupAdapter(items: List<ModelGroup<T>>): GroupAdapter<ModelGroup<T>>{
        return GroupAdapter(
            items,
            getBind(),
            getItemClickListener(items)
        )
    }
    fun getBind() = object : BindViewHolder<ModelGroup<T>>{
        override fun bind(view: View, item: ModelGroup<T>, position: Int, listener: (Int, Int) -> Unit) {
            view.findViewById<TextView>(R.id.group_name).apply {
                text = item.name
                setOnClickListener { getGroupClickListener() }
            }
            val inner_rv = view.findViewById<RecyclerView>(R.id.rv_items)
            inner_rv.adapter = createItemAdapter(item.listItems, position, listener)
        }
    }

    fun createItemAdapter(items: List<T>, groupPosition: Int, listener: (out: Int, inn: Int) -> Unit) =
        ItemAdapter(
            itemLayoutRes = R.layout.model_settings_item,
            listItem = items,
            bindView = { view, item ->
                view.findViewById<TextView>(R.id.set_item_name).text = item.getItemName()
                view.findViewById<TextView>(R.id.set_item_price).text = item.getItemPrice().toString()
            },
            listener = { inner -> listener(groupPosition, inner) }
        )
    fun getGroupClickListener(){
        val categories = mutableListOf<String>()

        lifecycleScope.launch(Dispatchers.IO) {
            categories.addAll(dao.getAllCategories(mode).map{x -> x.name})
            launch(Dispatchers.Main) {
                showAddAlert(categories)
            }
        }
    }
    fun showAddAlert(categories: List<String>){
        val binding = AlertCreateItemBinding.inflate(LayoutInflater.from(requireContext()))
        dialog = AlertDialog.Builder(requireContext()).setView(binding.root).create()

        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, categories)
        val autoCompleteTextView = binding.editCategory.findViewById<AutoCompleteTextView>(R.id.edit_category)
        autoCompleteTextView.setAdapter(adapter)

        binding.textTitle.text = type.uppercase()

        if(type == "items")
            binding.checkSeries.visibility = View.GONE
        else
            binding.editQuantity.visibility = View.GONE

        binding.btnCancel.setOnClickListener { dialog?.cancel() }
        binding.btnCreate.setOnClickListener {
            val name = binding.editName.text.toString().trim()
            val price = binding.editPrice.text.toString().trim()
            val category = binding.editCategory.text.toString()

            //check for null fields
            if(name.isEmpty() || price.isEmpty() || category.isEmpty()){
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(price.toIntOrNull() == null){
                Toast.makeText(requireContext(), "Цена - целое число", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(type == "items"){
                val quantity = binding.editQuantity.text.toString().trim()
                if(quantity.isEmpty() || (quantity.toIntOrNull() == null)) {
                    Toast.makeText(requireContext(), "количество - целое число", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                createItem(ReceiveObj.ReceiveItem(name, price.toInt(), category, quantity.toInt()))
            }
            else{
                val writeSeries = if(binding.checkSeries.isChecked) 0 else -1
                createItem(ReceiveObj.ReceiveReward(name, price.toInt(), category, writeSeries))
            }

            dialog?.cancel()
        }

        dialog?.getWindow()?.setBackgroundDrawableResource(R.drawable.shape_item)
        dialog?.window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        dialog?.show()
    }
    fun createItem(param: ReceiveObj){
        lifecycleScope.launch(Dispatchers.IO) {
            when(param){
                is ReceiveObj.ReceiveItem -> {
                    val categoryId = dao.getCategoryIdByName(param.categoryName)
                    val currentItemId = dao.getMaxIdFromItem()+1
                    val item = DbItem(currentItemId, param.name, param.price, categoryId, param.quantity)
                    dao.createItem(item)

                    val recordStorage = DbStorageItem(currentItemId,currentItemId, param.name, 0, categoryId)
                    dao.createRecordInStorage(recordStorage)
                }
                is ReceiveObj.ReceiveReward -> {
                    val categoryId = dao.getCategoryIdByName(param.categoryName)
                    val currentRewardId = dao.getMaxRewardId()+1
                    val item = DbReward(currentRewardId, param.name, param.price, categoryId, param.isSeries)
                    dao.createReward(item)
                }
            }

            loadItemsInRv()
        }
    }

    fun getItemClickListener(items: List<ModelGroup<T>>): (Int, Int)->Unit
        = { x: Int, y: Int ->
            val item = items[x].listItems[y]
            lifecycleScope.launch(Dispatchers.IO) {
                val categories = mutableListOf<String>()
                categories.addAll(dao.getAllCategories(mode).map { x -> x.name })
                withContext(Dispatchers.Main) {
                    showEditAlert(item, categories)
                }
            }
        }

    fun showEditAlert(item: T, categories: List<String>){
        val binding = AlertCreateItemBinding.inflate(LayoutInflater.from(requireContext()))
        dialog = AlertDialog.Builder(requireContext()).setView(binding.root).create()

        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, categories)
        val autoCompleteTextView = binding.editCategory.findViewById<AutoCompleteTextView>(R.id.edit_category)
        autoCompleteTextView.setAdapter(adapter)

        loadDataOfItem(binding, item)

        binding.btnCancel.setOnClickListener { dialog?.cancel() }
        binding.btnCreate.setOnClickListener {
            val name = binding.editName.text.toString().trim()
            val price = binding.editPrice.text.toString().trim()
            val category = binding.editCategory.text.toString()

            //check for null fields
            if(name.isEmpty() || price.isEmpty() || category.isEmpty()){
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(price.toIntOrNull() == null){
                Toast.makeText(requireContext(), "Цена - целое число", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(type == "items"){
                val quantity = binding.editQuantity.text.toString().trim()
                if(quantity.isEmpty() || (quantity.toIntOrNull() == null)) {
                    Toast.makeText(requireContext(), "количество - целое число", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val currentId = (item as DbItem).id
                updateItem(ReceiveObj.ReceiveItem(name, price.toInt(), category, quantity.toInt(), currentId))
            }
            else{
                val writeSeries = if(binding.checkSeries.isChecked) 0 else -1
                val currentId = (item as DbReward).id
                updateItem(ReceiveObj.ReceiveReward(name, price.toInt(), category, writeSeries, currentId))
            }

            dialog?.cancel()
        }

        dialog?.getWindow()?.setBackgroundDrawableResource(R.drawable.shape_item)
        dialog?.window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        dialog?.show()
    }
    fun loadDataOfItem(binding: AlertCreateItemBinding, item: T){
        binding.textTitle.text = type.uppercase()
        lifecycleScope.launch(Dispatchers.IO) {
            val categories = dao.getAllCategories(mode).map { x->x.name }
            when(item){
                is DbItem -> {
                    val item = dao.getItemByName(item.name)
                    val category = dao.getCategoryNameById(item.categoryId)
                    withContext(Dispatchers.Main){
                        binding.editName.setText(item.name)
                        binding.editPrice.setText(item.price.toString())
                        binding.editQuantity.setText(item.quantity.toString())
                        binding.editCategory.
                        setText(category)

                        binding.checkSeries.visibility = View.GONE
                    }
                }
                is DbReward -> {
                    val reward = dao.getRewardByName(item.name)
                    val category = dao.getCategoryNameById(item.categoryId)
                    withContext(Dispatchers.Main){
                        binding.editName.setText(reward.name)
                        binding.editPrice.setText(reward.price.toString())
                        binding.editCategory.setText(category)
                        binding.checkSeries.isChecked = reward.series != -1

                        binding.editQuantity.visibility = View.GONE
                    }
                }
                else -> { throw NullPointerException("generic object is don`t cast to DbReward or DbItem") }
            }
            withContext(Dispatchers.Main){
                val adapter = ArrayAdapter(requireContext(), R.layout.list_item, categories)
                val autoCompleteTextView = binding.editCategory.findViewById<AutoCompleteTextView>(R.id.edit_category)
                autoCompleteTextView.setAdapter(adapter)
            }
        }
    }
    fun updateItem(param: ReceiveObj){
        lifecycleScope.launch(Dispatchers.IO) {
            when(param){
                is ReceiveObj.ReceiveItem -> {
                    val categoryId = dao.getCategoryIdByName(param.categoryName)
                    val currentItemId = param.id
                    val item = DbItem(currentItemId, param.name, param.price, categoryId, param.quantity)
                    dao.createItem(item)

                    dao.updateStorageName(currentItemId, param.name)
                }
                is ReceiveObj.ReceiveReward -> {
                    val categoryId = dao.getCategoryIdByName(param.categoryName)
                    val currentRewardId = param.id
                    val item = DbReward(currentRewardId, param.name, param.price, categoryId, param.isSeries)
                    dao.createReward(item)
                }
            }

            loadItemsInRv()
        }
    }

}