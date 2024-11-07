package com.freshervnc.utilityapplication.ui.photo.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.freshervnc.utilityapplication.R
import com.freshervnc.utilityapplication.databinding.ListColorBinding


class ColorTextAdapter(private var items: List<String>, private val listener: ColorTextListener?) : RecyclerView.Adapter<ColorTextAdapter.MyViewHolder>() {
    private var selectedPosition = RecyclerView.NO_POSITION
    inner class MyViewHolder(val binding: ListColorBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ListColorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorTextAdapter.MyViewHolder, position: Int) {
        with(holder) {
            val item = items[position]
            binding.itemColor.setBackgroundColor(Color.parseColor(item))
            if (position == selectedPosition) {
                binding.itemImageClick.visibility = View.VISIBLE
            }else{
                binding.itemImageClick.visibility = View.GONE
            }
            holder.itemView.setOnClickListener {
                if (holder.itemView.isEnabled) {
                    val previousPosition = selectedPosition
                    selectedPosition = holder.adapterPosition
                    notifyItemChanged(previousPosition)
                    notifyItemChanged(selectedPosition)
                    listener ?.onClickItemColorText(item, holder.adapterPosition)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

}