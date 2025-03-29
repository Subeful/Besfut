package com.subefu.besfut.screens.main_fragment

import android.os.Bundle
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
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class RewardFragment : Fragment() {
    var _binding: FragmentRewardBinding? = null
    val binding get() = _binding!!
    lateinit var dao: Dao

    val currentDate = SimpleDateFormat("dd-MM-yy", Locale.getDefault()).format(Date())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentRewardBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dao = MyDatabase.getDb(requireContext()).getDao()
        loadCurrentState()
        loadReward()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    fun loadCurrentState(){
        dao.getCurrentState().asLiveData().observe(viewLifecycleOwner){ state ->
            state ?: return@observe
            updateUi(state)
        }
    }

    fun updateUi(state: DbState){
        viewLifecycleOwner.lifecycleScope.launch {
            val lvl = state.lvl
            val exp = state.amountExp

            binding.apply {
                progressExpValueReward.max = lvl * lvl * 100
                progressExpValueReward.progress = exp
                progressExpReward.text = "$exp/${lvl*lvl*100}"

                balanceCoinReward.text = state.amountCoin.toString()

                progressCoinValueReward.apply {
                    max = state.activeLimitDay
                    progress = state.activeInDay
                }
                progressCoinReward.text = "${state.activeInDay}/${state.activeLimitDay}"
            }
        }
    }

    fun loadReward(){
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val categories = dao.getAllCategories(0)
            val modelGroups = categories.map{ category ->
                ModelGroup(category.name, dao.getRewardByCategory(category.id))
            }
            withContext(Dispatchers.Main) {
                if(isAdded)
                    setRecycler(modelGroups)
            }
        }
    }
    fun setRecycler(rewards: List<ModelGroup<DbReward>>){
        binding.rvGroupReward.adapter = createGroupAdapter(rewards)
    }
    fun createGroupAdapter(goods: List<ModelGroup<DbReward>>): GroupAdapter<ModelGroup<DbReward>> {
        return GroupAdapter(
            listItem = goods,
            bindView = bindGroupView(),
            listener = { out, inn ->
                val item = goods[out].listItems[inn]
                getReward(item)
            }
        )
    }
    fun bindGroupView() = object : BindViewHolder<ModelGroup<DbReward>> {
        override fun bind(view: View, item: ModelGroup<DbReward>, position: Int, listener: (Int, Int) -> Unit) {
            view.findViewById<TextView>(R.id.group_name).text = item.name
            view.findViewById<RecyclerView>(R.id.rv_items).apply {
                layoutManager = GridLayoutManager(requireContext(), 2)
                adapter = createItemAdapter(item.listItems, position, listener)
            }
        }
    }
    fun createItemAdapter(items: List<DbReward>, groupPosition: Int, listener: (out: Int, inn: Int) -> Unit): ItemAdapter<DbReward> {
        return ItemAdapter(
            itemLayoutRes = R.layout.model_item,
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
            val state = dao.getState() ?: return@launch

            checkExcessState(state, item)
            updateHistory(item.price)

            updateRewardSeries(item)

            dao.getReward(item.price, item.price/2)
        }
    }

    fun checkExcessState(state: DbState, item: DbReward){
        //coin
        val goalAchieved = dao.checkGoalAchieved()
        if (goalAchieved && state.activeInDay + item.price >= state.activeLimitDay){
            dao.getBonus(50)
            dao.obtainGoalAchieved()
        }
        //lvl
        val maxExp = state.lvl * state.lvl * 100
        if(state.amountExp + item.price/2 >= maxExp)
            dao.updateLvl(state.amountExp - maxExp, state.lvl + 1)
    }

    fun updateHistory(price: Int){
        addHistoryCoin(price)
        addHistoryExp(price)
    }
    fun addHistoryCoin(price: Int){
        val currentCoinHistory = dao.getCoinOfDate(currentDate) ?: DbCoin(date = currentDate, spent = 0)
        currentCoinHistory.earn += price
        dao.setCoin(currentCoinHistory)
    }
    fun addHistoryExp(price: Int){
        val currentExpHistory = dao.getExpOfDate(currentDate) ?: DbExp(date = currentDate, earn = 0)
        currentExpHistory.earn += price/2
        dao.setExp(currentExpHistory)
    }

    fun updateRewardSeries(item: DbReward){
        if (item.series == -1) return

        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }

        val formater = SimpleDateFormat("dd-MM-yy", Locale.getDefault())
        val yesterdayDate =  formater.format(calendar.time)

        val lastRecord = dao.getRecordOfHistoryByDate(item.id, currentDate)
            ?: DbHistory(date = currentDate, rewardId = item.id, value = 0)
        val yesterdayRecord = dao.getRecordOfHistoryByDate(item.id, yesterdayDate)

        if(yesterdayRecord == null || yesterdayRecord.value == 0)
            dao.updateSeries(item.id, 0)
        else
            dao.updateSeries(item.id, ++item.series)

        lastRecord.value += 1
        dao.setRecordHistory(lastRecord)
    }

}