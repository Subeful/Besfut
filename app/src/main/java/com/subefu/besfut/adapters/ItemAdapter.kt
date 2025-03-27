package com.subefu.besfut.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.subefu.besfut.db.ReceiveInfoItem

class ItemAdapter<T>(
    val layoutId: Int,
    val listItem: List<T>,
    val bindView: (View, T) -> Unit,
    val listener: (position: Int) -> Unit):
RecyclerView.Adapter<ItemAdapter<T>.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(listItem[position])
    }

    override fun getItemCount() = listItem.size

    inner class ItemViewHolder(view: View): RecyclerView.ViewHolder(view){

        fun bind(item: T){
            bindView(itemView, item)

            itemView.setOnClickListener {
                listener(adapterPosition)
            }
        }
    }
}