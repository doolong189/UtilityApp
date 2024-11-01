package com.freshervnc.utilityapplication.ui.photo.sticker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.freshervnc.utilityapplication.R
import kotlin.math.pow

abstract class StickerView : FrameLayout {

    private var iv_border: BorderView? = null
    private var iv_scale: ImageView? = null
    private var iv_delete: ImageView? = null
    private var iv_flip: ImageView? = null

    // For scaling
    private var this_orgX = -1f
    private var this_orgY = -1f
    private var scale_orgX = -1f
    private var scale_orgY = -1f
    private var scale_orgWidth = -1.0
    private var scale_orgHeight = -1.0
    // For rotating
    private var rotate_orgX = -1f
    private var rotate_orgY = -1f
    private var rotate_newX = -1f
    private var rotate_newY = -1f
    // For moving
    private var move_orgX = -1f
    private var move_orgY = -1f

    private var centerX = 0.0
    private var centerY = 0.0

    companion object {
        const val TAG = "com.fresher_vnc.utility_application.ui.photo.sticker"
        private const val BUTTON_SIZE_DP = 15
        private const val SELF_SIZE_DP = 60

        private fun convertDpToPixel(dp: Float, context: Context): Int {
            val metrics = context.resources.displayMetrics
            return (dp * (metrics.densityDpi / 100f)).toInt()
        }
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    private fun init(context: Context) {
        iv_border = BorderView(context)
        iv_scale = ImageView(context)
        iv_delete = ImageView(context)
        iv_flip = ImageView(context)

        iv_scale?.setImageResource(R.drawable.maximize)
        iv_delete?.setImageResource(R.drawable.cross)
        iv_flip?.setImageResource(R.drawable.flip_2)

        tag = "DraggableViewGroup"
        iv_border?.tag = "iv_border"
        iv_scale?.tag = "iv_scale"
        iv_delete?.tag = "iv_delete"
        iv_flip?.tag = "iv_flip"

        val margin = convertDpToPixel(BUTTON_SIZE_DP.toFloat(), context) / 2
        val size = convertDpToPixel(SELF_SIZE_DP.toFloat(), context)

        layoutParams = LayoutParams(size, size).apply {
            gravity = Gravity.CENTER
        }

        val iv_main_params = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT).apply {
            setMargins(margin, margin, margin, margin)
        }

        val iv_border_params = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT).apply {
            setMargins(margin, margin, margin, margin)
        }

        val iv_scale_params = LayoutParams(convertDpToPixel(BUTTON_SIZE_DP.toFloat(), context), convertDpToPixel(BUTTON_SIZE_DP.toFloat(), context)).apply {
            gravity = Gravity.BOTTOM or Gravity.RIGHT
        }

        val iv_delete_params = LayoutParams(convertDpToPixel(BUTTON_SIZE_DP.toFloat(), context), convertDpToPixel(BUTTON_SIZE_DP.toFloat(), context)).apply {
            gravity = Gravity.TOP or Gravity.RIGHT
        }

        val iv_flip_params = LayoutParams(convertDpToPixel(BUTTON_SIZE_DP.toFloat(), context), convertDpToPixel(BUTTON_SIZE_DP.toFloat(), context)).apply {
            gravity = Gravity.TOP or Gravity.LEFT
        }

        addView(getMainView(), iv_main_params)
        addView(iv_border, iv_border_params)
        addView(iv_scale, iv_scale_params)
        addView(iv_delete, iv_delete_params)
        addView(iv_flip, iv_flip_params)

        setOnTouchListener(mTouchListener)
        iv_scale?.setOnTouchListener(mTouchListener)
        iv_delete?.setOnClickListener {
            (parent as? ViewGroup)?.removeView(this)
        }
        iv_flip?.setOnClickListener {
            val mainView = getMainView()
            mainView.rotationY = if (mainView.rotationY == -180f) 0f else -180f
            mainView.invalidate()
            requestLayout()
        }
    }

    val isFlip: Boolean
        get() = getMainView().rotationY == -180f

    protected abstract fun getMainView(): View

    @SuppressLint("ClickableViewAccessibility")
    private val mTouchListener = OnTouchListener { view, event ->
        when (view.tag) {
            "DraggableViewGroup" -> {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        move_orgX = event.rawX
                        move_orgY = event.rawY
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val offsetX = event.rawX - move_orgX
                        val offsetY = event.rawY - move_orgY
                        x += offsetX
                        y += offsetY
                        move_orgX = event.rawX
                        move_orgY = event.rawY
                    }
                }
            }
            "iv_scale" -> {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        this_orgX = x
                        this_orgY = y
                        scale_orgX = event.rawX
                        scale_orgY = event.rawY
                        scale_orgWidth = layoutParams.width.toDouble()
                        scale_orgHeight = layoutParams.height.toDouble()

                        rotate_orgX = event.rawX
                        rotate_orgY = event.rawY

                        centerX = (x + (parent as View).x + width / 2).toDouble()
                        centerY = y + (parent as View).y + resources.getDimensionPixelSize(resources.getIdentifier("status_bar_height", "dimen", "android")).toDouble() + height / 2
                    }
                    MotionEvent.ACTION_MOVE -> {
                        rotate_newX = event.rawX
                        rotate_newY = event.rawY

                        val angleDiff = Math.abs(
                            Math.atan2((event.rawY - scale_orgY).toDouble(),
                                (event.rawX - scale_orgX).toDouble()
                            ) -
                                    Math.atan2(scale_orgY - centerY, scale_orgX - centerX)
                        ) * 180 / Math.PI

                        val length1 = getLength(centerX, centerY, scale_orgX.toDouble(), scale_orgY.toDouble())
                        val length2 = getLength(centerX, centerY, event.rawX.toDouble(), event.rawY.toDouble())

                        val minSize = convertDpToPixel(SELF_SIZE_DP.toFloat(), context)
                        val offsetX = Math.abs(event.rawX - scale_orgX).toDouble()
                        val offsetY = Math.abs(event.rawY - scale_orgY).toDouble()
                        val offset = Math.round(maxOf(offsetX, offsetY))

                        if (length2 > length1 && (angleDiff < 25 || Math.abs(angleDiff - 180) < 25)) {
                            layoutParams.width += offset.toInt()
                            layoutParams.height += offset.toInt()
                            onScaling(true)
                        } else if (length2 < length1 && (angleDiff < 25 || Math.abs(angleDiff - 180) < 25) &&
                            layoutParams.width > minSize / 2 && layoutParams.height > minSize / 2
                        ) {
                            layoutParams.width -= offset.toInt()
                            layoutParams.height -= offset.toInt()
                            onScaling(false)
                        }

                        rotation = (Math.atan2(event.rawY - centerY, event.rawX - centerX) * 180 / Math.PI).toFloat() - 45
                        onRotating()

                        rotate_orgX = rotate_newX
                        rotate_orgY = rotate_newY
                        scale_orgX = event.rawX
                        scale_orgY = event.rawY

                        postInvalidate()
                        requestLayout()
                    }
                }
            }
        }
        true
    }

    protected open fun onScaling(scaleUp: Boolean) {}

    protected open fun onRotating() {}

    inner class BorderView : View {

        constructor(context: Context) : super(context)

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

        constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

        @SuppressLint("DrawAllocation")
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val borderPaint = Paint().apply {
                strokeWidth = 5f
                color = Color.WHITE
                style = Paint.Style.STROKE
            }
            val border = Rect(left - (layoutParams as MarginLayoutParams).leftMargin,
                top - (layoutParams as MarginLayoutParams).topMargin,
                right - (layoutParams as MarginLayoutParams).rightMargin,
                bottom - (layoutParams as MarginLayoutParams).bottomMargin
            )
            canvas.drawRect(border, borderPaint)
        }
    }

    private fun getLength(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        return Math.sqrt((y2 - y1).pow(2.0) + (x2 - x1).pow(2.0))
    }
    protected fun getImageViewFlip(): View {
        return iv_flip!!
    }
}
