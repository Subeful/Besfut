package com.subefu.besfut.screens.main_fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager

import androidx.recyclerview.widget.RecyclerView
import com.subefu.besfut.R
import com.subefu.besfut.adapters.GroupAdapter
import com.subefu.besfut.adapters.ItemAdapter
import com.subefu.besfut.databinding.FragmentStoreBinding
import com.subefu.besfut.db.Dao
import com.subefu.besfut.db.DbCoin
import com.subefu.besfut.db.DbItem
import com.subefu.besfut.db.DbState
import com.subefu.besfut.db.DbStoreHistory
import com.subefu.besfut.db.MyDatabase
import com.subefu.besfut.models.ModelGroup
import com.subefu.besfut.utils.BindViewHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StoreFragment : Fragment() {
    var _binding: FragmentStoreBinding? = null
    val binding get() = _binding!!
    lateinit var dao: Dao

    var currentDate = SimpleDateFormat("dd-MM-yy", Locale.getDefault()).format(Date())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentStoreBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dao = MyDatabase.getDb(requireContext()).getDao()
        loadStoreGoods()
    }

    override fun onStart() {
        super.onStart()
        loadCurrentState()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    fun loadCurrentState(){
        dao.getCurrentState().asLiveData().observe(viewLifecycleOwner){ state ->
            state ?: return@observe

            viewLifecycleOwner.lifecycleScope.launch {
                val isCurrentDate = checkCurrentDate()
                if(isCurrentDate && isAdded)
                    updateUi(state)
                else
                    updateCurrentDate()
            }
        }
    }
    fun updateUi(state: DbState) {
        binding.apply {
            balanceCoinStore.text = state.amountCoin.toString()
            progressCoinValueStore.text = "${state.coinInDay}/${state.coinLimitDay}"
            progressCoinStore.apply {
                progress = state.coinInDay
                max = state.coinLimitDay
            }
        }
    }
    suspend fun checkCurrentDate(): Boolean {
        return withContext(Dispatchers.IO){
            dao.getCurrentDate() == currentDate
        }
    }
    fun updateCurrentDate(){
        lifecycleScope.launch(Dispatchers.IO) {
            dao.updateCurrentDate(currentDate)
        }
    }

    fun loadStoreGoods(){
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val categories = dao.getAllCategories(1)
            val modelGroups = categories.map{ category ->
                ModelGroup(category.name, dao.getItemsByCategoryId(category.id))
            }
            withContext(Dispatchers.Main) {
                if(isAdded)
                    setRecycler(modelGroups)
            }
        }
    }
    fun setRecycler(goods: List<ModelGroup<DbItem>>){
        binding.rvGroupStore.adapter = createGroupAdapter(goods)
    }
    fun createGroupAdapter(goods: List<ModelGroup<DbItem>>): GroupAdapter<ModelGroup<DbItem>> {
        return GroupAdapter(
            listItem = goods,
            bindView = bindGroupView(),
            listener = { out, inn ->
                val item = goods[out].listItems[inn]
                buyItem(item)
            }
        )
    }
    fun bindGroupView() = object : BindViewHolder<ModelGroup<DbItem>>{
        override fun bind(view: View, item: ModelGroup<DbItem>, position: Int, listener: (Int, Int) -> Unit) {
            view.findViewById<TextView>(R.id.group_name).text = item.name
            view.findViewById<RecyclerView>(R.id.rv_items).apply {
                layoutManager = GridLayoutManager(requireContext(), 2)
                adapter = createItemAdapter(item.listItems, position, listener)
            }
        }
    }
    fun createItemAdapter(items: List<DbItem>, groupPosition: Int, listener: (Int, Int) -> Unit) = ItemAdapter(
            itemLayoutRes = R.layout.model_item,
            listItem = items,
            bindView = { view, item ->
                view.findViewById<TextView>(R.id.item_name).text = item.name
                view.findViewById<TextView>(R.id.item_price).text = item.price.toString()
            },
            listener = { inner -> listener(groupPosition, inner) }
        )


    fun buyItem(item: DbItem){
        lifecycleScope.launch(Dispatchers.IO) {
            val remainingCoins = dao.getReminderCoinInDay()
            if(remainingCoins < item.price){
                withContext(Dispatchers.Main) {
                    if(isAdded)
                        Toast.makeText(context, "Недостаточно монет", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }
            dao.buyGoods(item.price)
            dao.updateStorageItem(item.id, item.quantity)
            updateHistory(item)
        }
    }
    fun updateHistory(item: DbItem) {
        updateCoinHistory(item)
        updateStoreHistory(item)
    }

    fun updateCoinHistory(item: DbItem) {
        val coinHistory = dao.getCoinOfDate(currentDate) ?: DbCoin(date = currentDate, earn = 0)
        coinHistory.spent += item.price
        dao.setCoin(coinHistory)
    }

    fun updateStoreHistory(item: DbItem) {
        val storeHistory = dao.getRecordOfStoreHistoryByDate(item.id, currentDate)
            ?: DbStoreHistory(rewardId = item.id, value = 0, date = currentDate)
        storeHistory.value += item.quantity
        dao.setRecordStoreHistory(storeHistory)
    }
}