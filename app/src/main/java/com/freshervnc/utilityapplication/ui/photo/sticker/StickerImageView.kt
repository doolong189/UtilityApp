package com.freshervnc.utilityapplication.ui.photo.sticker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView


class StickerImageView : StickerView {
    // Owner ID management
    var ownerId: String? = null
    private var iv_main: ImageView? = null

    // Constructors
    constructor(context: Context?) : super(context!!)

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    )

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    )

    // Initializes and returns the main view for the sticker
    public override fun getMainView(): View {
        if (this.iv_main == null) {
            this.iv_main = ImageView(context)
            iv_main!!.scaleType = ImageView.ScaleType.FIT_XY
        }
        return iv_main!!
    }

    // Sets image as a resource ID
    fun setImageResource(res_id: Int) {
        iv_main!!.setImageResource(res_id)
    }

    // Sets image as a Drawable
    fun setImageDrawable(drawable: Drawable?) {
        iv_main!!.setImageDrawable(drawable)
    }

    var imageBitmap: Bitmap?
        // Retrieves the Bitmap from the ImageView
        get() = (iv_main!!.drawable as BitmapDrawable).bitmap
        // Sets image as a Bitmap
        set(bmp) {
            iv_main!!.setImageBitmap(bmp)
        }
}