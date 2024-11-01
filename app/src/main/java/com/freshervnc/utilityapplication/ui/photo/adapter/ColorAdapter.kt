package com.freshervnc.utilityapplication.ui.photo.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.freshervnc.utilityapplication.R
import com.freshervnc.utilityapplication.databinding.ListColorBinding


class ColorAdapter(private var items: List<String>, private val listener: ColorListener?) : RecyclerView.Adapter<ColorAdapter.MyViewHolder>() {

    inner class MyViewHolder(val binding: ListColorBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ListColorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorAdapter.MyViewHolder, position: Int) {
        with(holder) {
            val item = items[position]
            binding.itemColor.setBackgroundColor(Color.parseColor(item))
            holder.itemView.setOnClickListener {
                listener ?.onClickItem(item)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

}