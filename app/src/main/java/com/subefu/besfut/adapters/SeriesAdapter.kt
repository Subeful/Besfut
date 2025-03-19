package com.subefu.besfut.adapters

import android.content.Context
import android.os.Build
import android.os.strictmode.UntaggedSocketViolation
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.subefu.besfut.R
import com.subefu.besfut.models.ModelSeriesItem
import com.subefu.besfut.utils.BindViewHolder


class SeriesAdapter(
    val listItems: List<ModelSeriesItem>,
    val bindView: (View, ModelSeriesItem, Int, Int) -> Unit,
): RecyclerView.Adapter<SeriesAdapter.SeriesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeriesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.model_series_item, parent, false)
        return SeriesViewHolder(view)
    }

    override fun onBindViewHolder(holder: SeriesViewHolder, position: Int) {
        val item = listItems[position]

        holder.bind(item, position)
    }

    override fun getItemCount() = listItems.size

   inner  class SeriesViewHolder(view: View): RecyclerView.ViewHolder(view){

       fun bind(item: ModelSeriesItem, position: Int){
            bindView(itemView, item, position, listItems.size)
        }
    }
}