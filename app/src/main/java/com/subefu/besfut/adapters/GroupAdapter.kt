package com.subefu.besfut.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.subefu.besfut.R
import com.subefu.besfut.models.ModelGroup

class GroupAdapter(val context: Context, val listItem: List<ModelGroup>): RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.model_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.name.text = listItem[position].name
        holder.rv.layoutManager = GridLayoutManager(context, 2)
        holder.rv.adapter = ItemAdapter(context, listItem[position].listItems)
    }

    override fun getItemCount() = listItem.size

    class GroupViewHolder(view: View): RecyclerView.ViewHolder(view){
        val name = view.findViewById<TextView>(R.id.group_name)
        val rv = view.findViewById<RecyclerView>(R.id.rv_items)
    }
}