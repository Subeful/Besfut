package com.subefu.besfut.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.subefu.besfut.R

import com.subefu.besfut.databinding.ModelItemBinding
import com.subefu.besfut.db.DbItem

class ItemAdapter(val context: Context, val listItem: List<DbItem>, val listener: (position: Int) -> Unit):
    RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ModelItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val name = listItem[position].name
        val price = listItem[position].price.toString()

        holder.bind(name, price)
    }

    override fun getItemCount() = listItem.size

    inner class ItemViewHolder(binding: ModelItemBinding): RecyclerView.ViewHolder(binding.root){
        val tv_name = binding.itemName
        val tv_price = binding.itemPrice

        fun bind(name: String, price: String){
            tv_name.text = name
            tv_price.text = price

            itemView.setOnClickListener {
                listener(adapterPosition)
            }
        }
    }
}