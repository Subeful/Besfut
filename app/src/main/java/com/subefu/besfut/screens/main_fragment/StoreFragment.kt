package com.subefu.besfut.screens.main_fragment

import android.annotation.SuppressLint
import android.content.Context
import android.icu.util.Calendar
import android.icu.util.TimeUnit
import android.os.Bundle
import android.util.Log
import android.util.TimeUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope

import androidx.recyclerview.widget.RecyclerView
import com.subefu.besfut.adapters.GroupAdapter
import com.subefu.besfut.databinding.FragmentStoreBinding
import com.subefu.besfut.db.Dao
import com.subefu.besfut.db.MyDatabase
import com.subefu.besfut.models.ModelGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class StoreFragment : Fragment() {
   lateinit var binding: FragmentStoreBinding
   lateinit var rv: RecyclerView

   lateinit var dao: Dao

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
        loadState()
        loadingStoreGoods()
    }

    fun loadState(){
        dao.getCurrentState().asLiveData().observe(viewLifecycleOwner){
            if (it == null) return@observe
            lifecycleScope.launch(Dispatchers.IO) {
                if(checkCurrentDate())
                    lifecycleScope.launch(Dispatchers.Main) {
                        loadingBalance(it.amountCoin)
                        loadingCoinLimit(it.coinInDay, it.coinLimitDay)
                    }
                else
                    updateCurrentDate()
            }
            Log.d("DB_TEST", "coin: ${it.amountCoin}, coinINDay: ${it.coinInDay}, coinLimit: ${it.coinLimitDay}")
        }
    }
    @SuppressLint("SimpleDateFormat")
    suspend fun checkCurrentDate(): Boolean {
        val isItDate = lifecycleScope.async(Dispatchers.IO) {
            dao.getCurrentDate() == SimpleDateFormat("dd-MM-yy").format(Date())
        }
        return isItDate.await()
    }
    fun updateCurrentDate(){
        val newDate = SimpleDateFormat("dd-MM-yy").format(Date())
        dao.updateCurrentDate(newDate)
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
            val listModelGroups = ArrayList<ModelGroup>()

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
    fun setRecycler(goods: List<ModelGroup>){
        rv = binding.rvGroupStore
        rv.adapter = GroupAdapter(binding.root.context, goods){ o, i ->
            val item = goods[o].listItems[i]
            Log.d("INFO", "$o, $i\n${item.name} - ${item.price}")
            buyGoods(item.price, item.id)
        }
    }

    fun buyGoods(price: Int, id: Int){
        lifecycleScope.launch(Dispatchers.IO) {
            if(dao.getReminderCoinInDay() > price) {
                val count = dao.getQuantityItem(id)

                dao.buyGoods(price)
                dao.updateStorageItem(count, id)
            }
        }
    }


}