package com.freshervnc.utilityapplication.ui.photo.sticker

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.RectF
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.SparseIntArray
import android.util.TypedValue
import android.widget.TextView

@SuppressLint("AppCompatCustomView")
class AutoResizeTextView : TextView {
    private fun interface SizeTester {
        /**
         * @param suggestedSize Size of text to be tested
         * @param availableSpace Available space in which text must fit
         * @return an integer < 0 if after applying `suggestedSize` to text, it takes less space than `availableSpace`, > 0 otherwise
         */
        fun onTestSize(suggestedSize: Int, availableSpace: RectF): Int
    }

    private val mTextRect = RectF()
    private var mAvailableSpaceRect: RectF = RectF()
    private var mTextCachedSizes = SparseIntArray()
    private var mPaint: TextPaint = TextPaint(paint)
    private var mMaxTextSize: Float = textSize
    private var mSpacingMulti = 1.0f
    private var mSpacingAdd = 0.0f
    private var mMinTextSize = 18f
    private var mWidthLimit: Int = 0
    private var mMaxLines: Int = NO_LINE_LIMIT
    private var mEnableSizeCache = true
    private var mInitialized = false

    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        initialize()
    }

    private fun initialize() {
        mMaxTextSize = textSize
        mAvailableSpaceRect = RectF()
        mTextCachedSizes = SparseIntArray()
        if (mMaxLines == 0) {
            mMaxLines = NO_LINE_LIMIT
        }
        mInitialized = true
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        adjustTextSize(text.toString())
    }

    override fun setTextSize(size: Float) {
        mMaxTextSize = size
        mTextCachedSizes.clear()
        adjustTextSize(text.toString())
    }

    override fun setMaxLines(maxlines: Int) {
        super.setMaxLines(maxlines)
        mMaxLines = maxlines
        reAdjust()
    }

    override fun getMaxLines(): Int = mMaxLines

    override fun setSingleLine() {
        super.setSingleLine()
        mMaxLines = 1
        reAdjust()
    }

    override fun setSingleLine(singleLine: Boolean) {
        super.setSingleLine(singleLine)
        mMaxLines = if (singleLine) 1 else NO_LINE_LIMIT
        reAdjust()
    }

    override fun setLines(lines: Int) {
        super.setLines(lines)
        mMaxLines = lines
        reAdjust()
    }

    override fun setTextSize(unit: Int, size: Float) {
        val r = context?.resources ?: Resources.getSystem()
        mMaxTextSize = TypedValue.applyDimension(unit, size, r.displayMetrics)
        mTextCachedSizes.clear()
        adjustTextSize(text.toString())
    }

    override fun setLineSpacing(add: Float, mult: Float) {
        super.setLineSpacing(add, mult)
        mSpacingMulti = mult
        mSpacingAdd = add
    }

    fun setMinTextSize(minTextSize: Float) {
        mMinTextSize = minTextSize
        reAdjust()
    }

    private fun reAdjust() {
        adjustTextSize(text.toString())
    }

    private fun adjustTextSize(string: String) {
        if (!mInitialized) return

        val startSize = mMinTextSize.toInt()
        val heightLimit = measuredHeight - compoundPaddingBottom - compoundPaddingTop
        mWidthLimit = measuredWidth - compoundPaddingLeft - compoundPaddingRight
        mAvailableSpaceRect.right = mWidthLimit.toFloat()
        mAvailableSpaceRect.bottom = heightLimit.toFloat()
        super.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            efficientTextSizeSearch(startSize, mMaxTextSize.toInt(), mSizeTester, mAvailableSpaceRect).toFloat()
        )
    }

    private val mSizeTester = SizeTester { suggestedSize, availableSpace ->
        mPaint.textSize = suggestedSize.toFloat()
        val text = text.toString()
        val singleLine = getMaxLines() == 1
        if (singleLine) {
            mTextRect.bottom = mPaint.fontSpacing
            mTextRect.right = mPaint.measureText(text)
        } else {
            val layout = StaticLayout(text, mPaint, mWidthLimit, Layout.Alignment.ALIGN_NORMAL, mSpacingMulti, mSpacingAdd, true)
            if (getMaxLines() != NO_LINE_LIMIT && layout.lineCount > getMaxLines()) {
                return@SizeTester 1
            }
            mTextRect.bottom = layout.height.toFloat()
            mTextRect.right = (0 until layout.lineCount).maxOf { layout.getLineWidth(it).toInt() }.toFloat()
        }

        mTextRect.offsetTo(0f, 0f)
        if (availableSpace.contains(mTextRect)) -1 else 1
    }

    fun enableSizeCache(enable: Boolean) {
        mEnableSizeCache = enable
        mTextCachedSizes.clear()
        adjustTextSize(text.toString())
    }

    private fun efficientTextSizeSearch(start: Int, end: Int, sizeTester: SizeTester, availableSpace: RectF): Int {
        if (!mEnableSizeCache) return binarySearch(start, end, sizeTester, availableSpace)

        val key = text?.length ?: 0
        val size = mTextCachedSizes[key]
        if (size != 0) return size

        val resultSize = binarySearch(start, end, sizeTester, availableSpace)
        mTextCachedSizes.put(key, resultSize)
        return resultSize
    }

    private fun binarySearch(start: Int, end: Int, sizeTester: SizeTester, availableSpace: RectF): Int {
        var lo = start
        var hi = end - 1
        var lastBest = start
        while (lo <= hi) {
            val mid = (lo + hi) ushr 1
            val cmp = sizeTester.onTestSize(mid, availableSpace)
            if (cmp < 0) {
                lastBest = lo
                lo = mid + 1
            } else if (cmp > 0) {
                hi = mid - 1
                lastBest = hi
            } else {
                return mid
            }
        }
        return lastBest
    }

    override fun onTextChanged(text: CharSequence?, start: Int, before: Int, after: Int) {
        super.onTextChanged(text, start, before, after)
        reAdjust()
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        mTextCachedSizes.clear()
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        if (width != oldWidth || height != oldHeight) {
            reAdjust()
        }
    }

    companion object {
        private const val NO_LINE_LIMIT = -1
    }
}
