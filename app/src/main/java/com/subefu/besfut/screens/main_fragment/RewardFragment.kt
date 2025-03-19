package com.subefu.besfut.screens.main_fragment

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
import com.subefu.besfut.databinding.FragmentRewardBinding
import com.subefu.besfut.db.Dao
import com.subefu.besfut.db.DbCoin
import com.subefu.besfut.db.DbExp
import com.subefu.besfut.db.DbHistory
import com.subefu.besfut.db.DbReward
import com.subefu.besfut.db.DbState
import com.subefu.besfut.db.MyDatabase
import com.subefu.besfut.models.ModelGroup
import com.subefu.besfut.utils.BindViewHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


class RewardFragment : Fragment() {
    lateinit var binding: FragmentRewardBinding
    lateinit var rv: RecyclerView

    lateinit var dao: Dao

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRewardBinding.inflate(inflater)

        init()

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dao = MyDatabase.getDb(context).getDao()
    }

    fun init(){
        loadState()
        loadReward()
    }

    fun loadState(){
        dao.getCurrentState().asLiveData().observe(viewLifecycleOwner){
            if (it == null) return@observe

            loadBalance(it)
            loadExp(it)
            loadActiveLimit(it)

            Log.d("DB_TEST", "coin: ${it.amountCoin}, coinINDay: ${it.coinInDay}, coinLimit: ${it.coinLimitDay}")
        }
    }

    fun loadBalance(state: DbState){
        binding.balanceCoinReward.text = state.amountCoin.toString()
    }

    fun loadExp(state: DbState){
        var lvl = state.lvl
        val exp = state.amountExp

        binding.progressExpValueReward.max = lvl * lvl * 100
        binding.progressExpValueReward.progress = exp

        binding.progressExpReward.text = "$exp/${lvl*lvl*100}"

        Log.d("REWARD", "max: ${binding.progressExpValueReward.max}, progress: ${binding.progressExpValueReward.progress}")
    }

    fun loadActiveLimit(state: DbState){
        binding.progressCoinValueReward.apply {
            max = state.activeLimitDay
            progress = state.activeInDay
        }
        binding.progressCoinReward.text = "${state.activeInDay}/${state.activeLimitDay}"
    }

    fun loadReward(){
        lifecycleScope.launch(Dispatchers.IO) {
            val listModelGroups = ArrayList<ModelGroup<DbReward>>()

            val listCategory = dao.getAllCategories(0)
            for(category in listCategory){
                val listItems = dao.getRewardByCategory(category.id)
                listModelGroups.add(ModelGroup(category.name, listItems))
            }

            lifecycleScope.launch(Dispatchers.Main) {
                setRecycler(listModelGroups)
            }
        }
    }
    fun setRecycler(rewards: List<ModelGroup<DbReward>>){
        rv = binding.rvGroupReward
        rv.adapter = createGroupAdapter(rewards)
    }
    fun createGroupAdapter(goods: List<ModelGroup<DbReward>>): GroupAdapter<ModelGroup<DbReward>> {
        return GroupAdapter(
            listItem = goods,
            bindView = object : BindViewHolder<ModelGroup<DbReward>> {
                override fun bind(view: View, item: ModelGroup<DbReward>, position: Int, listener: (out: Int, inn: Int) -> Unit) {
                    bindGroupView(view, item, position, listener)
                }
            },
            listener = { out, inn ->
                val item = goods[out].listItems[inn]
                getReward(item)
                Log.d("INFO", "${item.name} - ${item.price}")
            }
        )
    }
    fun bindGroupView(view: View, item: ModelGroup<DbReward>, position: Int, listener: (out: Int, inn: Int) -> Unit) {
        view.findViewById<TextView>(R.id.group_name).text = item.name
        val rv = view.findViewById<RecyclerView>(R.id.rv_items)
        rv.layoutManager = GridLayoutManager(requireContext(), 2)
        rv.adapter = createItemAdapter(item.listItems, position, listener)
    }
    fun createItemAdapter(items: List<DbReward>, groupPosition: Int, listener: (out: Int, inn: Int) -> Unit): ItemAdapter<DbReward> {
        return ItemAdapter(
            layoutId = R.layout.model_item,
            listItem = items,
            bindView = { view, item ->
                view.findViewById<TextView>(R.id.item_name).text = item.name
                view.findViewById<TextView>(R.id.item_price).text = item.price.toString()
            },
            listener = { inner -> listener(groupPosition, inner) }
        )
    }

    fun getReward(item: DbReward){
        lifecycleScope.launch(Dispatchers.IO) {

            val state = dao.getState()
            checkExcessLimitActive(state, item)
            checkExcessExp(state, item)
            addHistoryCoin(item.price)
            addHistoryExp(item.price)

            updateRewardSeries(item)

            dao.getReward(item.price, item.price/2)
        }
    }
    fun checkExcessLimitActive(state: DbState, item: DbReward){
        if (state.activeInDay + item.price >= state.activeLimitDay)
            dao.getBonus(50)
    }
    fun checkExcessExp(state: DbState, item: DbReward){
        val maxExp = state.lvl * state.lvl *100
        if(state.amountExp + item.price/2 >= maxExp)
            dao.updateLvl(state.amountExp - maxExp, ++state.lvl)
    }
    fun addHistoryCoin(earn: Int){
        val date = SimpleDateFormat("dd-MM-yy").format(Date())
        val currentCoinHistory = dao.getCoinOfDate(date) ?: DbCoin(date = date, spent = 0)
        Log.d("REWARD", "${currentCoinHistory.earn}")
        currentCoinHistory.earn += earn
        dao.setCoin(currentCoinHistory)
        Log.d("REWARD", " += ${currentCoinHistory.earn}")
    }
    fun addHistoryExp(earn: Int){
        val date = SimpleDateFormat("dd-MM-yy").format(Date())
        val currentExpHistory = dao.getExpOfDate(date) ?: DbExp(date = date, earn = 0)
        Log.d("REWARD", "${currentExpHistory.earn}")
        currentExpHistory.earn += earn/2
        dao.setExp(currentExpHistory)
        Log.d("REWARD", " += ${currentExpHistory.earn}")
    }

    fun updateRewardSeries(item: DbReward){
        if (item.series == -1) return

        val calendar = Calendar.getInstance()
        val formater = SimpleDateFormat("dd-MM-yy")
        val date = formater.format(calendar.time)
        calendar[Calendar.DAY_OF_WEEK] -= 1
        val yesterdayDate =  formater.format(calendar.time)
        val lastRecord = dao.getRecordOfHistoryByDate(item.id, date)
            ?: DbHistory(date = date, rewardId = item.id, value = 0)
        val yesterdayRecord = dao.getRecordOfHistoryByDate(item.id, yesterdayDate)
            ?: DbHistory(date = date, rewardId = item.id, value = 0)
        if(yesterdayRecord.value == 0)
            dao.updateSeries(item.id, 0)
        else
            dao.updateSeries(item.id, ++item.series)

        lastRecord.value += 1
        dao.setRecordHistory(lastRecord)
    }
}