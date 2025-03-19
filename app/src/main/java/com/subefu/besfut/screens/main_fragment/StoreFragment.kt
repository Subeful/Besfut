package com.subefu.besfut.screens.main_fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
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
import com.subefu.besfut.databinding.FragmentStoreBinding
import com.subefu.besfut.db.Dao
import com.subefu.besfut.db.DbCoin
import com.subefu.besfut.db.DbItem
import com.subefu.besfut.db.DbStoreHistory
import com.subefu.besfut.db.MyDatabase
import com.subefu.besfut.models.ModelGroup
import com.subefu.besfut.utils.BindViewHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class StoreFragment : Fragment() {
   lateinit var binding: FragmentStoreBinding
   lateinit var rv: RecyclerView

   lateinit var dao: Dao
   
    var currentDate = "00-00-00" 

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentStoreBinding.inflate(inflater)

        init()

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dao = MyDatabase.getDb(context).getDao()
    }

    fun init(){
        currentDate = SimpleDateFormat("dd-MM-yy").format(Date())
        
        loadState()
        loadingStoreGoods()
    }

    fun loadState(){
        dao.getCurrentState().asLiveData().observe(viewLifecycleOwner){
            if (it == null) return@observe
            lifecycleScope.launch(Dispatchers.IO) {
                if(checkCurrentcurrentDate())
                    lifecycleScope.launch(Dispatchers.Main) {
                        loadingBalance(it.amountCoin)
                        loadingCoinLimit(it.coinInDay, it.coinLimitDay)
                    }
                else
                    upcurrentDateCurrentcurrentDate()
            }
            Log.d("DB_TEST", "coin: ${it.amountCoin}, coinINDay: ${it.coinInDay}, coinLimit: ${it.coinLimitDay}")
        }
    }
    @SuppressLint("SimplecurrentDateFormat")
    suspend fun checkCurrentcurrentDate(): Boolean {
        val isItcurrentDate = lifecycleScope.async(Dispatchers.IO) {
            dao.getCurrentDate() == currentDate
        }
        return isItcurrentDate.await()
    }
    fun upcurrentDateCurrentcurrentDate(){
        val newcurrentDate = currentDate
        dao.updateCurrentDate(newcurrentDate)
    }

    fun loadingBalance(coin: Int){
        binding.balanceCoinStore.text = coin.toString()
    }
    fun loadingCoinLimit(coinInDay: Int, coinLimit: Int){
        binding.progressCoinValueStore.text = "$coinInDay/$coinLimit"

        binding.progressCoinStore.apply {
            progress = coinInDay
            max = coinLimit
        }
    }

    fun loadingStoreGoods(){
        lifecycleScope.launch(Dispatchers.IO) {
            val listModelGroups = ArrayList<ModelGroup<DbItem>>()

            val listCategory = dao.getAllCategories(1)
            for(category in listCategory){
                val listItems = dao.getItemsByCategoryId(category.id)
                listModelGroups.add(ModelGroup(category.name, listItems))
            }

            lifecycleScope.launch(Dispatchers.Main) {
                setRecycler(listModelGroups)
            }
        }
    }
    fun setRecycler(goods: List<ModelGroup<DbItem>>){
        rv = binding.rvGroupStore
        rv.adapter = createGroupAdapter(goods)
    }
    fun createGroupAdapter(goods: List<ModelGroup<DbItem>>): GroupAdapter<ModelGroup<DbItem>> {
        return GroupAdapter(
            goods,
            object : BindViewHolder<ModelGroup<DbItem>> {
                override fun bind(view: View, item: ModelGroup<DbItem>, position: Int, listener: (out: Int, inn: Int) -> Unit) {
                    bindGroupView(view, item, position, listener)
                }
            },
            { out, inn ->
                val item = goods[out].listItems[inn]
                buyGoods(item)
                Log.d("INFO", "${item.name} - ${item.price}")
            }
        )
    }
    fun bindGroupView(view: View, item: ModelGroup<DbItem>, position: Int, listener: (out: Int, inn: Int) -> Unit) {
        view.findViewById<TextView>(R.id.group_name).text = item.name
        val rv = view.findViewById<RecyclerView>(R.id.rv_items)
        rv.layoutManager = GridLayoutManager(requireContext(), 2)
        rv.adapter = createItemAdapter(item.listItems, position, listener)
    }
    fun createItemAdapter(items: List<DbItem>, groupPosition: Int, listener: (out: Int, inn: Int) -> Unit): ItemAdapter<DbItem> {
        return ItemAdapter(
            R.layout.model_item,
            items,
            { view, item ->
                view.findViewById<TextView>(R.id.item_name).text = item.name
                view.findViewById<TextView>(R.id.item_price).text = item.price.toString()
            },
            { inner -> listener(groupPosition, inner) }
        )
    }


    fun buyGoods(item: DbItem){
        lifecycleScope.launch(Dispatchers.IO) {
            if(dao.getReminderCoinInDay() < item.price)
                return@launch

            spentHistoryCoin(item.price)
            dao.buyGoods(item.price)
            dao.updateStorageItem(item.id, item.quantity)
            addHistoryStore(item)
        }
    }

    fun spentHistoryCoin(earn: Int){
        val currentCoinHistory = dao.getCoinOfDate(currentDate) ?: DbCoin(date = currentDate, earn = 0)
        Log.d("REWARD", "${currentCoinHistory.spent}")
        currentCoinHistory.spent += earn
        dao.setCoin(currentCoinHistory)
        Log.d("REWARD", " += ${currentCoinHistory.spent}")
    }

    fun addHistoryStore(item: DbItem){
        val currentStoreHistory = dao.getRecordOfStoreHistoryByDate(item.id, currentDate)
            ?: DbStoreHistory(rewardId = item.id, value = 0, date = currentDate)
        Log.d("REWARD", "${currentStoreHistory}")
        currentStoreHistory.value += item.quantity
        dao.setRecordStoreHistory(currentStoreHistory)
    }
}