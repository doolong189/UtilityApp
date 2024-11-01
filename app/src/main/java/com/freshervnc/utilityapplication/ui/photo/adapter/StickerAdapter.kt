package com.freshervnc.utilityapplication.ui.photo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.freshervnc.utilityapplication.R


class StickerAdapter(val context: Context , val data : List<Int> , val listener : StickerListener) : BaseAdapter() {
    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(p0: Int): Int {
        return data[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_emojicon, parent, false)
        val ivSticker = view.findViewById<ImageView>(R.id.item_emojicon_imgView)
        ivSticker.setImageResource(getItem(position))
        ivSticker.setOnClickListener {
            listener.onClickItem(data[position])
        }
        return view
    }
}