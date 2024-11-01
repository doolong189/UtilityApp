package com.freshervnc.utilityapplication.ui.photo.sticker

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

class StickerTextView : StickerView {
    private var tvMain: AutoResizeTextView? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun getMainView(): View {
        if (tvMain != null) return tvMain!!

        tvMain = AutoResizeTextView(context).apply {
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            textSize = 300f
            maxLines = 1
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                gravity = Gravity.CENTER
            }
            setMinTextSize(10f)
        }
        getImageViewFlip()?.visibility = View.GONE
        return tvMain!!
    }

    fun setText(text: String) {
        tvMain?.text = text
    }

    fun getText(): String? {
        return tvMain?.text?.toString()
    }

    fun setTextColor(textColor : String){
        return tvMain!!.setTextColor(Color.parseColor(textColor))
    }

    companion object {
        fun pixelsToSp(context: Context, px: Float): Float {
            val scaledDensity = context.resources.displayMetrics.scaledDensity
            return px / scaledDensity
        }
    }

    override fun onScaling(scaleUp: Boolean) {
        super.onScaling(scaleUp)
    }
}
