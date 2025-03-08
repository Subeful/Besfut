package com.subefu.besfut.adapters

import android.content.Context
import android.os.Build
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.subefu.besfut.R
import com.subefu.besfut.models.ModelSeriesItem


class SeriesAdapter(val context: Context,  val listItems: List<ModelSeriesItem>): RecyclerView.Adapter<SeriesAdapter.SeriesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeriesViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.model_series_item, parent, false)
        return SeriesViewHolder(view)
    }

    override fun onBindViewHolder(holder: SeriesViewHolder, position: Int) {
        val item = listItems[position]
        holder.name.text = item.name
        holder.series.text = item.series.toString()

        val marginInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 20f, context.getResources().getDisplayMetrics()
        ).toInt()

        val params: ViewGroup.MarginLayoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        if(position == 0){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                params.setMarginStart(marginInPx)
        }
        else if(position == listItems.size - 1){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                params.setMarginEnd(marginInPx)
        }
        holder.itemView.layoutParams = params
    }

    override fun getItemCount() = listItems.size

    class SeriesViewHolder(view: View): RecyclerView.ViewHolder(view){
        val name = view.findViewById<TextView>(R.id.model_series_item_name)
        val series = view.findViewById<TextView>(R.id.model_series_item_series)
    }
}