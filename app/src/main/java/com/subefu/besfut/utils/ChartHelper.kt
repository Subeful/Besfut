package com.subefu.besfut.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import com.db.williamchart.view.DonutChartView
import com.db.williamchart.view.LineChartView
import com.subefu.besfut.R

//принимаем данные, заполняем по умолчанию, если мало данных

object ChartHelper {
    fun setupLineChart(
        context: Context,
        chart: LineChartView,
        data: List<Pair<String, Float>> = listOf(),
        color: Int = context.getColor(R.color.blue),
        animDuration: Long = 1000L,
        showTooltip: Boolean = true)
    {
        if(data.size <= 1){
            setupEmptyLineChart(context, chart)
            return
        }

        chart.apply {
            if (showTooltip)
                tooltip.onCreateTooltip(this)
            lineColor = color
            animation.duration = animDuration
            animate(data)
        }
    }

    private fun setupEmptyLineChart(context: Context, chart: LineChartView) {
        setupLineChart(
            context = context,
            chart = chart,
            data = listOf(Pair("", 0f), Pair("", 0f)),
            color = Color.GRAY,
            showTooltip = false
        )
    }

    fun setupDonutChart(
        context: Context,
        chart: DonutChartView,
        data: List<Float> = listOf(),
        colors: IntArray = intArrayOf(context.getColor(R.color.blue), context.getColor(R.color.orange)),
        animDuration: Long = 1000L)
    {
        if(data.isEmpty()){
            setupEmptyDonutChart(context, chart)
            return
        }

        chart.apply {
            donutColors = colors
            animation.duration = animDuration
            donutBackgroundColor = context.getColor(R.color.dark_back)
            donutTotal = data.sum()
            animate(data)
        }
    }

    private fun setupEmptyDonutChart(context: Context, chart: DonutChartView) {
        setupDonutChart(
            context = context,
            chart = chart,
            data = listOf(100f),
            colors = intArrayOf(Color.GRAY, Color.GRAY)
        )
    }
}
