package com.subefu.besfut.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.subefu.besfut.databinding.ModelGroupBinding
import com.subefu.besfut.utils.BindViewHolder


class GroupAdapter<T>(val listItem: List<T>,
                      val bindView: BindViewHolder<T>,
                      val listener: (out: Int, inn: Int) -> Unit
): RecyclerView.Adapter<GroupAdapter<T>.GroupViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = ModelGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val item = listItem[position]
        holder.bind(item, position)
    }

    override fun getItemCount() = listItem.size

    inner class GroupViewHolder(binding: ModelGroupBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: T, position: Int){
            bindView.bind(itemView, item, position, listener)
        }
    }
}