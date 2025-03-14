package com.subefu.besfut.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.subefu.besfut.databinding.ModelGroupBinding
import com.subefu.besfut.models.ModelGroup


class GroupAdapter(val context: Context, val listItem: List<ModelGroup>, val listener: (out: Int, inn: Int) -> Unit): RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = ModelGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val name = listItem[position].name
        holder.bind(name, position)
    }

    override fun getItemCount() = listItem.size

    inner class GroupViewHolder(binding: ModelGroupBinding): RecyclerView.ViewHolder(binding.root){
        val tv_name = binding.groupName
        val rv = binding.rvItems

        fun bind(name: String, position: Int){
            tv_name.text = name

            rv.layoutManager = GridLayoutManager(itemView.context, 2)
            rv.adapter = ItemAdapter(itemView.context, listItem[position].listItems){ i ->
                listener(position, i)
            }
        }
    }
}