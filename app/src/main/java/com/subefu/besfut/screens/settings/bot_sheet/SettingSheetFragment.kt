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
import androidx.fragment.app.Fragment
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
import com.subefu.besfut.db.DbCoin
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

private inline fun Fragment.executeIfAdded(block: () -> Unit) {
    if (isAdded) block()
}

class SettingSheetFragment(val type: String) : BottomSheetDialogFragment() {

    var _binding: FragmentSettingSheetBinding? = null
    val binding get() = _binding!!

    var _dao: Dao? = null
    val dao get() = _dao!!

    var dialog: AlertDialog? = null
    val mode = if(type == "items") 1 else 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentSettingSheetBinding.inflate(inflater)
        _dao = MyDatabase.getDb(requireContext()).getDao()

        binding.apply {
            btnAddCategory.setOnClickListener { showAlertCreateCategory() }
            textTitle.text = type
        }

        loadItemsInRv()

        return binding.root
    }

    override fun onDestroyView() {
        dialog?.dismiss()
        dialog = null
        _dao = null
        _binding = null
        super.onDestroyView()
    }

    fun loadItemsInRv(){
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val categories = dao.getAllCategories(mode)
            val listModelGroups: List<ModelGroup<ReceiveInfoItem>> = categories.map{ category ->
                val items = when(category.isItems){
                    1 -> dao.getItemsByCategoryId(category.id)
                    0 -> dao.getRewardByCategory(category.id)
                    else -> throw Exception("Invalid category type")
                }
                ModelGroup(category.name, items)
            }

            withContext(Dispatchers.Main) {
                executeIfAdded {
                    binding.recyclerItems.adapter = createGroupAdapter(listModelGroups)
                }
            }
        }
    }
    fun createGroupAdapter(items: List<ModelGroup<ReceiveInfoItem>>): GroupAdapter<ModelGroup<ReceiveInfoItem>>{
        return GroupAdapter(
            listItem = items,
            bindView = getBind(),
            listener = getItemClickListener(items)
        )
    }
    fun getBind() = object : BindViewHolder<ModelGroup<ReceiveInfoItem>>{
        override fun bind(view: View, item: ModelGroup<ReceiveInfoItem>, position: Int, listener: (Int, Int) -> Unit) {
            view.findViewById<TextView>(R.id.group_name).apply {
                text = item.name
                setOnClickListener { showCategorySelectionAlert() }
            }
            view.findViewById<RecyclerView>(R.id.rv_items).adapter =
                createItemAdapter(item.listItems, position, listener)
        }
    }
    fun createItemAdapter(items: List<ReceiveInfoItem>, groupPosition: Int, listener: (out: Int, inn: Int) -> Unit) =
        ItemAdapter(
            itemLayoutRes = R.layout.model_settings_item,
            listItem = items,
            bindView = { view, item ->
                view.findViewById<TextView>(R.id.set_item_name).text = item.getItemName()
                view.findViewById<TextView>(R.id.set_item_price).text = item.getItemPrice().toString()
            },
            listener = { inner -> listener(groupPosition, inner) }
        )
    fun showCategorySelectionAlert(){
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val categories = dao.getAllCategories(mode).map{ it.name }
            withContext(Dispatchers.Main) {
                executeIfAdded { showAddAlert(categories) }
            }
        }
    }

    fun showAlertCreateCategory(){
        viewLifecycleOwner.lifecycleScope.launch {
            val binding = AlertCreateCategoryBinding.inflate(LayoutInflater.from(requireContext()))
            val dialog = AlertDialog.Builder(requireContext())
                .setView(binding.root)
                .create()
                .apply {
                    window?.setBackgroundDrawableResource(R.drawable.shape_item)
                    show()
                }

            binding.apply {
                btnCancel.setOnClickListener { dialog.cancel() }
                btCreate.setOnClickListener {
                    val name = editName.text.toString()
                    if (name.isNotEmpty()) {
                        createCategory(name)
                        dialog.cancel()
                    } else {
                        Toast.makeText(context, "Введите название категории", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    fun showAddAlert(categories: List<String>, item: ReceiveInfoItem? = null){
        val binding = AlertCreateItemBinding.inflate(LayoutInflater.from(requireContext()))
        dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
            .apply {
                window?.setBackgroundDrawableResource(R.drawable.shape_item)
                window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
                show()
            }

        setupCommonAlertViews(binding, categories, item)

        binding.btnCreate.setOnClickListener {
            if (validateInput(binding)) {
                viewLifecycleOwner.lifecycleScope.launch {
                    if (item == null) {
                        createItem(createReceiveObjFromInput(binding))
                    } else {
                        updateItem(createReceiveObjFromInput(binding, item))
                    }
                }
                dialog?.dismiss()
            }
        }
    }
    private fun setupCommonAlertViews(binding: AlertCreateItemBinding,
                                      categories: List<String>,
                                      item: ReceiveInfoItem? = null) {
        binding.apply {
            textTitle.text = type.uppercase()
            checkSeries.visibility = if (mode == 1) View.GONE else View.VISIBLE
            editQuantity.visibility = if (mode == 1) View.VISIBLE else View.GONE

            val adapter = ArrayAdapter(requireContext(), R.layout.list_item, categories)
            editCategory.setAdapter(adapter)

            btnCancel.setOnClickListener { dialog?.dismiss() }

            item?.let { bindDataOfItem(binding, it) }
        }
    }

    fun validateInput(binding: AlertCreateItemBinding): Boolean {
        with(binding) {
            val name = editName.text.toString().trim()
            val price = editPrice.text.toString().trim()
            val category = editCategory.text.toString()

            if (name.isEmpty() || price.isEmpty() || category.isEmpty()) {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                return false
            }

            if (price.toIntOrNull() == null) {
                Toast.makeText(requireContext(), "Цена - целое число", Toast.LENGTH_SHORT).show()
                return false
            }

            // Дополнительная валидация для items
            if (mode == 1) {
                val quantity = editQuantity.text.toString().trim()
                if (quantity.isEmpty() || quantity.toIntOrNull() == null) {
                    Toast.makeText(requireContext(), "количество - целое число", Toast.LENGTH_SHORT).show()
                    return false
                }
            }

            return true
        }
    }

    suspend fun createReceiveObjFromInput(binding: AlertCreateItemBinding,
                                          existingItem: ReceiveInfoItem? = null): ReceiveObj {
        with(binding) {
            val name = editName.text.toString().trim()
            val price = editPrice.text.toString().trim().toInt()
            val category = editCategory.text.toString()

            return when (mode) {
                1 -> { // Items
                    val quantity = editQuantity.text.toString().trim().toInt()
                    val id = withContext(Dispatchers.IO){
                        (existingItem as? DbItem)?.id ?: (dao.getMaxIdFromItem() + 1)
                    }
                    ReceiveObj.ReceiveItem(name, price, category, quantity, id)
                }
                else -> { // Rewards
                    val isSeries = if (checkSeries.isChecked) 0 else -1
                    val id = withContext(Dispatchers.IO){
                        (existingItem as? DbReward)?.id ?: (dao.getMaxRewardId() + 1)
                    }
                    ReceiveObj.ReceiveReward(name, price, category, isSeries, id)
                }
            }
        }
    }

    fun createCategory(name: String){
        lifecycleScope.launch(Dispatchers.IO) {
            val currentId = dao.getMaxIdFromCategory()+1
            dao.createCategory(DbCategory(currentId, name, mode))
            withContext(Dispatchers.Main){
                executeIfAdded { loadItemsInRv() }
            }
        }
    }
    fun createItem(param: ReceiveObj) {
        lifecycleScope.launch(Dispatchers.IO) {
            when (param) {
                is ReceiveObj.ReceiveItem -> {
                    val categoryId = dao.getCategoryIdByName(param.categoryName)
                    val item = DbItem(param.id, param.name, param.price, categoryId, param.quantity)
                    dao.createItem(item)

                    val recordStorage = DbStorageItem(param.id, param.id, param.name, 0, categoryId)
                    dao.createRecordInStorage(recordStorage)
                }
                is ReceiveObj.ReceiveReward -> {
                    val categoryId = dao.getCategoryIdByName(param.categoryName)
                    val reward = DbReward(param.id, param.name, param.price, categoryId, param.isSeries)
                    dao.createReward(reward)
                }
            }

            withContext(Dispatchers.Main) {
                executeIfAdded { loadItemsInRv() }
            }
        }
    }

    fun updateItem(param: ReceiveObj){
        lifecycleScope.launch(Dispatchers.IO) {
            when(param){
                is ReceiveObj.ReceiveItem -> {
                    val categoryId = dao.getCategoryIdByName(param.categoryName)
                    val item = DbItem(param.id, param.name, param.price, categoryId, param.quantity)
                    //insert(replace)
                    dao.createItem(item)
                    dao.updateStorageName(param.id, param.name)
                }
                is ReceiveObj.ReceiveReward -> {
                    val categoryId = dao.getCategoryIdByName(param.categoryName)
                    val item = DbReward(param.id, param.name, param.price, categoryId, param.isSeries)
                    dao.createReward(item)
                }
            }
            withContext(Dispatchers.Main){
                executeIfAdded { loadItemsInRv() }
            }
        }
    }

    fun getItemClickListener(items: List<ModelGroup<ReceiveInfoItem>>): (Int, Int) -> Unit =
        { groupPos, itemPos ->
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val item = items[groupPos].listItems[itemPos]
                val categories = dao.getAllCategories(mode).map { it.name }
                withContext(Dispatchers.Main) {
                    executeIfAdded { showAddAlert(categories, item) }
                }
            }
        }

    fun bindDataOfItem(binding: AlertCreateItemBinding, receiveItem: ReceiveInfoItem){
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            when(receiveItem){
                is DbItem -> {
                    val category = dao.getCategoryNameById(receiveItem.categoryId)
                    withContext(Dispatchers.Main){
                        executeIfAdded {
                            binding.apply {
                                editName.setText(receiveItem.name)
                                editPrice.setText(receiveItem.price.toString())
                                editQuantity.setText(receiveItem.quantity.toString())
                                editCategory.setText(category)
                            }
                        }
                    }
                }
                is DbReward -> {
                    val category = dao.getCategoryNameById(receiveItem.categoryId)
                    withContext(Dispatchers.Main){
                        executeIfAdded {
                            binding.apply {
                                editName.setText(receiveItem.name)
                                editPrice.setText(receiveItem.price.toString())
                                editCategory.setText(category)
                                checkSeries.isChecked = receiveItem.series != -1
                            }
                        }
                    }
                }
            }
        }
    }
}