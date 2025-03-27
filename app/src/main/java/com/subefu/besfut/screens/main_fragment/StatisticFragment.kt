package com.subefu.besfut.screens.main_fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Highlights
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.blue
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.db.williamchart.data.AxisType
import com.db.williamchart.data.configuration.LineChartConfiguration
import com.db.williamchart.view.LineChartView
import com.subefu.besfut.R
import com.subefu.besfut.adapters.SeriesAdapter
import com.subefu.besfut.databinding.FragmentRewardBinding
import com.subefu.besfut.databinding.FragmentStatisticBinding
import com.subefu.besfut.db.Dao
import com.subefu.besfut.db.DbCoin
import com.subefu.besfut.db.DbExp
import com.subefu.besfut.db.DbHistory
import com.subefu.besfut.db.DbReward
import com.subefu.besfut.db.MyDatabase
import com.subefu.besfut.models.ModelHistory
import com.subefu.besfut.models.ModelSeriesItem
import com.subefu.besfut.screens.SettingsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Format
import java.text.SimpleDateFormat
import java.util.Date

class StatisticFragment : Fragment() {
    lateinit var binding: FragmentStatisticBinding
    lateinit var dao: Dao

    val animDuration = 1000L
    var currentDate: String = "00-00-00"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentStatisticBinding.inflate(inflater)

        /*//графики из view
        val lineChart = binding.chartExp
        val list = ArrayList<Pair<String, Float>>()
        list.apply { add(Pair("1", 1f)); add(Pair("2", 2f)); add(Pair("3", 1.8f)); add(Pair("4", 2.5f)); add(Pair("5", 3f)) }
        lineChart.apply {
            lineChart.tooltip.onCreateTooltip(lineChart)
            lineChart.animation.duration = animDuration
//            lineChart.gradientFillColors = intArrayOf(Color.parseColor("#F2C788"), Color.TRANSPARENT)
            lineChart.animate(list)
            lineChart.onDataPointClickListener = { index, _, _ ->
                Toast.makeText(
                    requireContext(),
                     if(list.isEmpty()) "0" else list
                        .toList().get(index).second.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        val lineChart1 = binding.chartCoin
        val list1 = ArrayList<Pair<String, Float>>()
        list1.apply { add(Pair("1", 20f)); add(Pair("2", 10f)); add(Pair("3", 4f)); add(Pair("4", 18f)); add(Pair("5", 15f)) }
        lineChart1.apply {
            lineChart1.tooltip.onCreateTooltip(lineChart1)
            lineChart1.animation.duration = animDuration
//            lineChart.gradientFillColors = intArrayOf(Color.parseColor("#F2C788"), Color.TRANSPARENT)
            lineChart1.animate(list1)
            lineChart1.onDataPointClickListener = { index, _, _ ->
                Toast.makeText(
                    requireContext(),
                    if(list1.isEmpty()) "0" else list1
                        .toList().get(index).second.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }



        val lineChart2 = binding.chartPhone
        val list2 = ArrayList<Pair<String, Float>>()
        list2.apply { add(Pair("1", 20f)); add(Pair("2", 10f)); add(Pair("3", 4f)); add(Pair("4", 18f)); add(Pair("5", 15f)) }
        lineChart2.apply {
            tooltip.onCreateTooltip(lineChart1)
            animation.duration = animDuration
            animate(list2)
            onDataPointClickListener = { index, _, _ ->
                Toast.makeText(
                    requireContext(),
                    if(list2.isEmpty()) "0" else list2
                        .toList().get(index).second.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }



        val listPhoneRatio = listOf(41f, 59f)
        binding.chartLearningRatio.apply {
            animation.duration = animDuration
            donutTotal = 100f
            animate(listPhoneRatio)
            donutColors =
                intArrayOf(resources.getColor(R.color.blue), resources.getColor(R.color.orange))
        }
        val lineChart3 = binding.chartLearningLine
        val list3 = ArrayList<Pair<String, Float>>()
        list3.apply { add(Pair("1", 20f)); add(Pair("2", 10f)); add(Pair("3", 4f)); add(Pair("4", 18f)); add(Pair("5", 15f)) }
        lineChart3.apply {
            tooltip.onCreateTooltip(lineChart1)
            animation.duration = animDuration
            animate(list3)
            onDataPointClickListener = { index, _, _ ->
                Toast.makeText(
                    requireContext(),
                    if(list3.isEmpty()) "0" else list3
                        .toList().get(index).second.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }*/

        loadEmptyCharts()
        init()

        binding.settings.setOnClickListener {
            requireContext().startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch(Dispatchers.Main) {
            loadAllDataOfStatistic()
        }
    }

    fun init(){
        dao = MyDatabase.getDb(requireContext()).getDao()
        setCurrentDate()
        loadEmptyCharts()
    }

    fun loadAllDataOfStatistic(){
        loadDataOfExp()
        loadDataOfCoin()

        loadSeries()

        loadCoinRatio()
        loadIndicateReward()

        loadLearnData()
        loadTopReward()
    }

    fun loadEmptyCharts(){
        binding.progressExpStatistic.apply {
            max = 100
            progress = 0
        }

        binding.chartCoin.apply {
            animate(listOf(Pair("", 0f), Pair("", 0f), Pair("", 0f)))
            lineColor = Color.GRAY
        }
        binding.chartExp.apply {
            animate(listOf(Pair("", 0f), Pair("", 0f), Pair("", 0f)))
            lineColor = Color.GRAY
        }
        binding.chartCoinRatio.apply {
            animate(listOf(100f))
            donutColors = intArrayOf(Color.GRAY)
        }
        binding.chartPhone.apply {
            animate(listOf(Pair("", 0f), Pair("", 0f), Pair("", 0f)))
            lineColor = Color.GRAY
        }
        binding.chartLearningLine.apply {
            animate(listOf(Pair("", 0f), Pair("", 0f), Pair("", 0f)))
            lineColor = Color.GRAY
        }
        binding.chartLearningRatio.apply {
            animate(listOf(100f))
            donutColors = intArrayOf(Color.GRAY)
        }
        binding.chartLearningRatio.apply {
            animate(listOf(100f))
            donutColors = intArrayOf(Color.GRAY)
        }

    }
    fun setCurrentDate(){
        currentDate = SimpleDateFormat("dd-MM-yy").format(Date())
    }

    fun loadDataOfExp(){
        lifecycleScope.launch(Dispatchers.IO) {
            val historyOfCoins = dao.getExpOfMonth()
            val state = dao.getState()
            val lvl = state.lvl
            val maxExp = lvl*lvl*100
            val earn = historyOfCoins.sumOf { x -> x.earn }
            val average = if(historyOfCoins.size != 0) earn / historyOfCoins.size else 0

            launch(Dispatchers.Main) {
                setExpChart(historyOfCoins)
                binding.progressExpStatistic.max = maxExp
                binding.progressExpStatistic.progress = state.amountExp
                binding.progressExpValueStatistic.text = "${state.amountExp}/$maxExp EXP"
                binding.expLvlStat.text = "$lvl LVL"
                binding.expEarn.text = "заработано: $earn"
                binding.expAveage.text = "среднее: $average"
            }
        }
    }
    fun setExpChart(historyOfCoins: List<DbExp>){
        if(historyOfCoins.size <= 1)
            return

        val data = ArrayList<Pair<String, Float>>()
        historyOfCoins.forEach {
            data.add(Pair(it.date, it.earn.toFloat()))
        }

        binding.chartExp.apply {
            tooltip.onCreateTooltip(this)
            lineColor = resources.getColor(R.color.blue)
            animation.duration = animDuration
            animate(data)
        }
    }
    fun loadDataOfCoin(){
        lifecycleScope.launch(Dispatchers.IO) {
            val historyOfCoin = dao.getCoinOfMonth()
            val all = historyOfCoin.sumOf { x -> x.earn }
            val spent = historyOfCoin.sumOf { x -> x.spent }

            launch(Dispatchers.Main) {
                setCoinChart(historyOfCoin)
                binding.coinAll.text = all.toString()
                binding.coinEarn.text = "заработано: $all"
                binding.coinSpent.text = "потрачено: $spent"
            }
        }
    }
    fun setCoinChart(historyOfCoin: List<DbCoin>){
        if(historyOfCoin.size <= 0)
            return

        val data = ArrayList<Pair<String, Float>>()
        historyOfCoin.forEach {
            data.add(Pair(it.date, it.earn.toFloat()))
        }

        binding.chartCoin.apply {
            tooltip.onCreateTooltip(this)
            lineColor = resources.getColor(R.color.blue)
            animation.duration = animDuration
            animate(data)
        }
    }

    fun loadSeries(){
        lifecycleScope.launch(Dispatchers.IO) {
            val series = dao.getRewardSeries()
            val seriesModels = series
                .map { x -> ModelSeriesItem(x.id, x.name, x.series) }
                .sortedByDescending { x -> x.series }

            launch(Dispatchers.Main) {
                binding.rvSeries.adapter = SeriesAdapter(
                    listItems = seriesModels,
                    bindView = {view, item, position, size ->
                        bindView(view, item, position, size)
                    }
                )
            }
        }
    }
    fun bindView(view: View, item: ModelSeriesItem, position: Int, size: Int){
        view.findViewById<TextView>(R.id.model_series_item_name).text = item.name
        view.findViewById<TextView>(R.id.model_series_item_series).text = item.series.toString()

        val marginInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 20f, view.context.getResources().getDisplayMetrics()
        ).toInt()
        val params: ViewGroup.MarginLayoutParams = view.layoutParams as ViewGroup.MarginLayoutParams

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            if (position == 0)
                params.setMarginStart(marginInPx)
            else if(position == size - 1)
                params.setMarginEnd(marginInPx)
        }
        view.layoutParams = params

        view.setOnClickListener{
            loadDataOfSeriesChart(item)
        }
    }
    fun loadDataOfSeriesChart(item: ModelSeriesItem){
        lifecycleScope.launch(Dispatchers.IO) {
            val historySeries = dao.getHistoryOfReward(item.id)
            launch(Dispatchers.Main) {
                setSeriesChart(item.name, historySeries)
            }
        }
    }
    fun setSeriesChart(name: String, data: List<ModelHistory>){
        val layout = binding.seriesLayout

        if (layout.size > 1){
            layout.removeViewAt(1)
            if((layout.get(0) as TextView).text == name){
                layout.get(0).visibility = View.GONE
                return
            }
        }

        (layout.get(0) as TextView).apply {
            text = name
            visibility = View.VISIBLE
        }

        val lineChartView = LineChartView(requireContext())
        lineChartView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            180
        )
        val values = data.map { x -> Pair(x.date.substring(0, 2), x.value.toFloat()) }

        lineChartView.show(values)
        lineChartView.apply {
            lineColor = requireContext().resources.getColor(R.color.blue)
            axis = AxisType.X
            smooth = true
            labelsSize = 20f
            labelsColor = requireContext().resources.getColor(R.color.light)
            lineThickness = 2f
            animation.duration = animDuration
            onDataPointClickListener = { index, _, _ ->
                val value = if(data.isEmpty()) "0" else data.toList().get(index).value.toString()
                Toast.makeText(requireContext(), value, Toast.LENGTH_SHORT).show()
            }
        }
        setFillDataIfEmptyChart(lineChartView)
        layout.addView(lineChartView)
    }

    fun setFillDataIfEmptyChart(lineChartView: LineChartView){
        val listFilling = listOf(Pair("", 0f), Pair("", 0f))
        if(lineChartView.size <= 1)
            lineChartView.apply {
                animate(listFilling)
                lineColor = Color.GRAY
            }
    }

    fun loadCoinRatio(){
        lifecycleScope.launch(Dispatchers.IO) {
            val data = dao.getCoinForAllTime()
            val earn = data.sumOf { x -> x.earn }.toFloat()
            val spent = data.sumOf { x -> x.spent }.toFloat()

            launch(Dispatchers.Main) {
                setRatioCoinDonutChart(listOf(earn, spent))
            }
        }
    }
    fun setRatioCoinDonutChart(data: List<Float>){
        binding.chartCoinRatio.apply {
            animation.duration = animDuration
            donutTotal = data.sum()
            animate(data)
            donutBackgroundColor = resources.getColor(R.color.dark_back)
            donutColors = intArrayOf(resources.getColor(R.color.blue), resources.getColor(R.color.orange))
        }
    }

    fun loadIndicateReward(){
        lifecycleScope.launch(Dispatchers.IO) {
            val item = dao.getItemByName("Телефон")
            val history = dao.getStoreHistoryOfItem(item.id)
            val data = history.map { x -> Pair(x.date, x.value.toFloat()) }
            val all = data.sumOf { x -> x.second.toInt() }
            val average = all/data.size.toFloat()

            launch(Dispatchers.Main) {
                binding.nameIndicateChart.text = item.name.uppercase()
                binding.indicateAll.text = "всего: $all"
                binding.indicateAverage.text = "средне: $average"

                setIndicateChart(data)
            }
        }
    }
    fun setIndicateChart(data: List<Pair<String, Float>>){
        binding.chartPhone.apply {
            tooltip.onCreateTooltip(this)
            lineColor = resources.getColor(R.color.blue)
            animation.duration = animDuration
            animate(data)
        }
        setFillDataIfEmptyChart(binding.chartPhone)
    }

    fun loadLearnData(){
        lifecycleScope.launch(Dispatchers.IO) {
            val reward = dao.getRewardByName("Android")
            val history = dao.getHistoryOfReward(reward.id)
            val data = history.map { x -> Pair(x.date, x.value.toFloat()) }
            val allEarn = data.sumOf { x -> x.second.toInt() }
            val average = allEarn/data.size.toFloat()

            val activeDay = history.size
            val skipId = listOf(
                dao.getItemByName("Выходной"),
                dao.getItemByName("Пропуск занятия"),
            )
            var skipDay = dao.getStoreHistoryOfItem(skipId[0].id).map { x -> x.value }.sum()
            skipDay += dao.getStoreHistoryOfItem(skipId[1].id).map { x -> x.value }.sum()

            launch(Dispatchers.Main) {
                binding.nameLearnChart.text = reward.name.uppercase()
                binding.learnEarn.text = "всего: $allEarn"
                binding.learnAverage.text = "среднее: $average"

                setLearnChart(data)
                setLearnRatioChart(listOf(activeDay.toFloat(), skipDay.toFloat()))
            }
        }
    }
    fun setLearnChart(data: List<Pair<String, Float>>){
        binding.chartLearningLine.apply {
            tooltip.onCreateTooltip(this)
            lineColor = resources.getColor(R.color.blue)
            animation.duration = animDuration
            animate(data)
        }
        setFillDataIfEmptyChart(binding.chartLearningLine)
    }
    fun setLearnRatioChart(data: List<Float>){
        binding.chartLearningRatio.apply {
            animation.duration = animDuration
            donutTotal = data.sum()
            animate(data)
            donutBackgroundColor = resources.getColor(R.color.dark_back)
            donutColors = intArrayOf(resources.getColor(R.color.blue), resources.getColor(R.color.orange))
        }
    }

    fun loadTopReward(){
        lifecycleScope.launch(Dispatchers.IO) {
            val topRewards = dao.getBestReward()

            launch(Dispatchers.Main) {
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
}