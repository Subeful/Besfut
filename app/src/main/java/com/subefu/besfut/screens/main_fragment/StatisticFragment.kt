package com.subefu.besfut.screens.main_fragment

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.db.williamchart.data.AxisType
import com.db.williamchart.view.LineChartView
import com.subefu.besfut.R
import com.subefu.besfut.adapters.SeriesAdapter
import com.subefu.besfut.databinding.FragmentRewardBinding
import com.subefu.besfut.databinding.FragmentStatisticBinding
import com.subefu.besfut.models.ModelSeriesItem

class StatisticFragment : Fragment() {
    lateinit var binding: FragmentStatisticBinding
    lateinit var rv_series: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentStatisticBinding.inflate(inflater)

        //создание серий
        rv_series = binding.rvSeries
        rv_series.adapter = SeriesAdapter(requireContext(), listOf(
            ModelSeriesItem("item_1", 20),
            ModelSeriesItem("item_2", 10),
            ModelSeriesItem("item_3", 20),
            ModelSeriesItem("item_4", 40),
            ModelSeriesItem("item_5", 12),
            ModelSeriesItem("item_6", 23),
        ))



        //динамическое добавление графика
        val layout = binding.seriesLayout
        val lineChartView = LineChartView(requireContext())
        lineChartView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            180
        )
        val data = listOf(
            "Jan" to 5f,"Feb" to 100f,"Mar" to 3f,"Apr" to 8f,"May" to 2f, "Jan" to 5f,
            "Feb" to 7f,"Mar" to 3f,"Apr" to 8f,"May" to 2f,"Apr" to 8f,"May" to 2f,
            )
        lineChartView.show(data)
        lineChartView.apply {
            lineColor = requireContext().resources.getColor(R.color.blue)
            axis = AxisType.X
            smooth = true
            labelsSize = 20f
            labelsColor = requireContext().resources.getColor(R.color.light)
            lineThickness = 2f
            animation.duration = 1000
            onDataPointClickListener = { index, _, _ ->
                Toast.makeText(requireContext(), if(data.isEmpty()) "0" else data.toList().get(index).second.toString(), Toast.LENGTH_SHORT).show()
            }
        }
        layout.addView(lineChartView)



        //графики из view
        val lineChart = binding.chartExp
        val list = ArrayList<Pair<String, Float>>()
        list.apply { add(Pair("1", 1f)); add(Pair("2", 2f)); add(Pair("3", 1.8f)); add(Pair("4", 2.5f)); add(Pair("5", 3f)) }
        lineChart.apply {
            lineChart.tooltip.onCreateTooltip(lineChart)
            lineChart.animation.duration = 1000
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
            lineChart1.animation.duration = 1000
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


        val listRatio = listOf(32f, 68f)
        binding.chartExpRatio.apply {
            animation.duration = 1000
            donutTotal = 100f
            animate(listRatio)
            donutColors =
                intArrayOf(resources.getColor(R.color.blue), resources.getColor(R.color.orange))
        }
        val lineChart2 = binding.chartPhone
        val list2 = ArrayList<Pair<String, Float>>()
        list2.apply { add(Pair("1", 20f)); add(Pair("2", 10f)); add(Pair("3", 4f)); add(Pair("4", 18f)); add(Pair("5", 15f)) }
        lineChart2.apply {
            tooltip.onCreateTooltip(lineChart1)
            animation.duration = 1000
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
            animation.duration = 1000
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
            animation.duration = 1000
            animate(list3)
            onDataPointClickListener = { index, _, _ ->
                Toast.makeText(
                    requireContext(),
                    if(list3.isEmpty()) "0" else list3
                        .toList().get(index).second.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        return binding.root
    }
}