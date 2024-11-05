package com.freshervnc.utilityapplication.ui.photo.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.freshervnc.utilityapplication.databinding.ListColorDrawBinding


class ColorPickerAdapter(private var items: List<String>, private val listener: ColorPickerListener?) : RecyclerView.Adapter<ColorPickerAdapter.MyViewHolder>() {

    inner class MyViewHolder(val binding: ListColorDrawBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ListColorDrawBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorPickerAdapter.MyViewHolder, position: Int) {
        with(holder) {
            val item = items[position]
            binding.itemColor.setBackgroundColor(Color.parseColor(item))
            holder.itemView.setOnClickListener {
                listener ?.onClickItemColorPicker(item)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

}