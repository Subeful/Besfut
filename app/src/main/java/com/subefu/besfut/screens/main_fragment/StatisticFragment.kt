package com.subefu.besfut.screens.main_fragment

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.get
import androidx.core.view.size
import androidx.lifecycle.lifecycleScope
import com.db.williamchart.data.AxisType
import com.db.williamchart.view.DonutChartView
import com.db.williamchart.view.LineChartView
import com.subefu.besfut.R
import com.subefu.besfut.adapters.SeriesAdapter
import com.subefu.besfut.databinding.FragmentStatisticBinding
import com.subefu.besfut.db.Dao
import com.subefu.besfut.db.DbCoin
import com.subefu.besfut.db.DbExp
import com.subefu.besfut.db.MyDatabase
import com.subefu.besfut.models.ModelHistory
import com.subefu.besfut.models.ModelSeriesItem
import com.subefu.besfut.screens.settings.SettingsActivity
import com.subefu.besfut.utils.ChartHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatisticFragment : Fragment() {
    var _binding: FragmentStatisticBinding? = null
    val binding get() = _binding!!
    lateinit var dao: Dao

    var loadDataJob: Job? = null
    val chartHelper = ChartHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dao = MyDatabase.getDb(requireContext()).getDao()
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentStatisticBinding.inflate(inflater)
        loadEmptyCharts()
        
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            loadData()
        }

        binding.settings.setOnClickListener {
            requireContext().startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }
        return binding.root
    }

    override fun onDestroyView() {
        viewLifecycleOwner.lifecycleScope.cancel()
        loadDataJob?.cancel()
        binding.seriesLayout.removeAllViews()
        _binding = null
        super.onDestroyView()
    }

    fun loadData(){
        loadDataJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            loadDataOfExp()
            loadDataOfCoin()

            loadSeries()

            loadCoinRatio()
            loadIndicateReward()

            loadLearnData()
            loadTopReward()
        }
    }

    fun loadEmptyCharts(){
        viewLifecycleOwner.lifecycleScope.launch {
            binding.apply {
                progressExpStatistic.apply {
                    max = 100
                    progress = 0
                }
                chartHelper.apply {
                    setupLineChart(requireContext(), chartCoin)
                    setupLineChart(requireContext(), chartExp)
                    setupLineChart(requireContext(), chartPhone)
                    setupLineChart(requireContext(), chartLearningLine)
                    setupDonutChart(requireContext(), chartCoinRatio)
                    setupDonutChart(requireContext(), chartLearningRatio)
                    setupDonutChart(requireContext(), chartLearningRatio)
                }
            }
        }
    }

    suspend fun loadDataOfExp(){
        val historyOfCoins = dao.getExpOfMonth()
        val state = dao.getState()
        val lvl = state.lvl
        val maxExp = lvl*lvl*100
        val earn = historyOfCoins.sumOf { record -> record.earn }
        val historyEmpty = historyOfCoins.isEmpty()
        val average = if(historyEmpty.not()) earn / historyOfCoins.size else 0

        withContext(Dispatchers.Main) {
            binding.apply {
                progressExpStatistic.max = maxExp
                progressExpStatistic.progress = state.amountExp
                progressExpValueStatistic.text = "${state.amountExp}/$maxExp EXP"
                expLvlStat.text = "$lvl LVL"
                expEarn.text = "заработано: $earn"
                expAveage.text = "среднее: $average"
            setExpChart(historyOfCoins)
            }
        }
    }
    fun setExpChart(historyOfCoins: List<DbExp>){
        val data = historyOfCoins.map { Pair(it.date, it.earn.toFloat()) }
        chartHelper.setupLineChart(requireContext(),binding.chartExp, data)
    }

    suspend fun loadDataOfCoin(){
        val historyOfCoin = dao.getCoinOfMonth()
        val all = historyOfCoin.sumOf { record -> record.earn }
        val spent = historyOfCoin.sumOf { record -> record.spent }

        withContext(Dispatchers.Main) {
            binding.apply {
                coinAll.text = all.toString()
                coinEarn.text = "заработано: $all"
                coinSpent.text = "потрачено: $spent"
            }
            setCoinChart(historyOfCoin)
        }
    }
    fun setCoinChart(historyOfCoin: List<DbCoin>){
        val data = historyOfCoin.map { Pair(it.date, it.earn.toFloat()) }
        chartHelper.setupLineChart(requireContext(),binding.chartCoin, data)
    }

    suspend fun loadSeries(){
        val series = dao.getRewardSeries()
        val seriesModels = series
            .map { x -> ModelSeriesItem(x.id, x.name, x.series) }
            .sortedByDescending { x -> x.series }

        withContext(Dispatchers.Main) {
            binding.rvSeries.adapter = SeriesAdapter(
                listItems = seriesModels,
                bindView = { view, item, position, size ->
                    bindView(view, item, position, size)
                }
            )
        }
    }
    fun bindView(view: View, item: ModelSeriesItem, position: Int, size: Int){
        viewLifecycleOwner.lifecycleScope.launch {
            view.findViewById<TextView>(R.id.model_series_item_name).text = item.name
            view.findViewById<TextView>(R.id.model_series_item_series).text = item.series.toString()

            if(position == 0 || position == size-1){
                val marginInPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, view.context.getResources().getDisplayMetrics()).toInt()
                val params: ViewGroup.MarginLayoutParams = view.layoutParams as ViewGroup.MarginLayoutParams

                if (position == 0)
                    params.setMarginStart(marginInPx)
                else if(position == size - 1)
                    params.setMarginEnd(marginInPx)

                view.layoutParams = params
            }

            val historySeries = withContext(Dispatchers.IO) {
                dao.getHistoryOfReward(item.id)
            }
            if(isAdded) {
                view.setOnClickListener {
                    setSeriesChart(item.name, historySeries)
                }
            }
        }
    }
    fun setSeriesChart(name: String, data: List<ModelHistory>){
        viewLifecycleOwner.lifecycleScope.launch {
            val layout = binding.seriesLayout

            if (layout.size > 1){
                while (layout.childCount > 1) {
                    layout.removeViewAt(1)
                }
                if((layout.get(0) as TextView).text == name){
                    layout.get(0).visibility = View.GONE
                    return@launch
                }
            }

            (layout.get(0) as TextView).apply {
                text = name
                visibility = View.VISIBLE
            }

            val lineChartView = LineChartView(requireContext())
            lineChartView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 180)

            val values = data.map { x -> Pair(x.date.substring(0, 2), x.value.toFloat()) }

            chartHelper.setupLineChart(requireContext(),lineChartView, values)
            lineChartView.apply {
                axis = AxisType.X
                smooth = true
                labelsSize = 20f
                labelsColor = requireContext().resources.getColor(R.color.light)
            }
            layout.addView(lineChartView)
        }
    }

    suspend fun loadCoinRatio(){
        val data = dao.getCoinForAllTime()
        val earn = data.sumOf { x -> x.earn }.toFloat()
        val spent = data.sumOf { x -> x.spent }.toFloat()

        withContext(Dispatchers.Main) {
            setRatioCoinDonutChart(listOf(earn, spent))
        }
    }
    fun setRatioCoinDonutChart(data: List<Float>){
        chartHelper.setupDonutChart(requireContext(), binding.chartCoinRatio, data)
    }

    suspend fun loadIndicateReward(){
        val item = dao.getItemById(1)
        val history = dao.getStoreHistoryOfItem(item.id)
        val data = history.map { x -> Pair(x.date, x.value.toFloat()) }
        val all = data.sumOf { x -> x.second.toInt() }
        val average = if(data.isEmpty().not())  all/data.size.toFloat() else 0

        withContext(Dispatchers.Main) {
            binding.apply {
                nameIndicateChart.text = item.name.uppercase()
                indicateAll.text = "всего: $all"
                indicateAverage.text = "средне: $average"
            }
            setIndicateChart(data)
        }
    }
    fun setIndicateChart(data: List<Pair<String, Float>>){
        chartHelper.setupLineChart(requireContext(),binding.chartPhone, data)
    }

    suspend fun loadLearnData(){
        val reward = dao.getRewardById(8)
        val history = dao.getHistoryOfReward(reward.id)
        val data = history.map { x -> Pair(x.date, x.value.toFloat()) }
        val allEarn = data.sumOf { x -> x.second.toInt() }
        val average = if(data.isEmpty().not())  allEarn/data.size.toFloat() else 0

        //предустановленные товары
        val activeDay = history.size
        val skipId = listOf(
            dao.getItemById(6),
            dao.getItemById(7),
        )
        var skipDay = dao.getStoreHistoryOfItem(skipId[0].id).map { record -> record.value }.sum()
        skipDay += dao.getStoreHistoryOfItem(skipId[1].id).map { record -> record.value }.sum()

        val data2 = listOf(activeDay.toFloat(), skipDay.toFloat())

        withContext(Dispatchers.Main) {
            binding.apply {
                nameLearnChart.text = reward.name.uppercase()
                learnEarn.text = "всего: $allEarn"
                learnAverage.text = "среднее: $average"
            }
            setLearnChart(data)
            setLearnRatioChart(data2)
        }
    }
    fun setLearnChart(data: List<Pair<String, Float>>){
        chartHelper.setupLineChart(requireContext(),binding.chartLearningLine, data)
    }
    fun setLearnRatioChart(data: List<Float>){
        chartHelper.setupDonutChart(requireContext(), binding.chartLearningRatio, data)
    }

    suspend fun loadTopReward(){
        val topRewards = dao.getBestReward()

        withContext(Dispatchers.Main) {
            if(topRewards.size >= 1){
                binding.topReward1Name.text = topRewards[0].name
                binding.topReward1Exp.text = topRewards[0].result.toString()
            }
            if(topRewards.size >= 2){
                binding.topReward2Name.text = topRewards[1].name
                binding.topReward2Exp.text = topRewards[1].result.toString()
            }
            if(topRewards.size == 3){
                binding.topReward3Name.text = topRewards[2].name
                binding.topReward3Exp.text = topRewards[2].result.toString()
            }
        }
    }
}