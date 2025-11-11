package com.xr.common.middleware.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.IntDef
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.xr.common.middleware.R

class VisionTextView : AppCompatTextView {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val attr = context.obtainStyledAttributes(attrs, R.styleable.VisionTextView)
        parseAttributes(attr)
        attr.recycle()

    }

    companion object {
        private const val DEFAULT_NONE_VALUE = View.NO_ID

//        private const val PING_FANG_TYPE_FACE = "PingFang Medium.ttf"

        const val DIR_START = 0
        const val DIR_TOP = 1
        const val DIR_END = 2
    }

    @IntDef(DIR_START, DIR_TOP, DIR_END)
    @Retention(AnnotationRetention.SOURCE)
    annotation class DrawDir


//    private val pingFangTypeFace by lazy {
//        Typeface.createFromAsset(
//            context.assets,
//            PING_FANG_TYPE_FACE
//        )
//    }

    private fun parseAttributes(attr: TypedArray) {
        val drawStartW = attr.getDimensionPixelOffset(
            R.styleable.VisionTextView_draw_start_width, DEFAULT_NONE_VALUE
        )
        val drawStartH = attr.getDimensionPixelOffset(
            R.styleable.VisionTextView_draw_start_height, DEFAULT_NONE_VALUE
        )
        val drawEndW = attr.getDimensionPixelOffset(
            R.styleable.VisionTextView_draw_end_width, DEFAULT_NONE_VALUE
        )
        val drawEndH = attr.getDimensionPixelOffset(
            R.styleable.VisionTextView_draw_end_height, DEFAULT_NONE_VALUE
        )
        val drawTopW = attr.getDimensionPixelOffset(
            R.styleable.VisionTextView_draw_top_width, DEFAULT_NONE_VALUE
        )
        val drawTopH = attr.getDimensionPixelOffset(
            R.styleable.VisionTextView_draw_top_height, DEFAULT_NONE_VALUE
        )
        resizeSelfDrawable(DIR_START, drawStartW, drawStartH)
        resizeSelfDrawable(DIR_TOP, drawTopW, drawTopH)
        resizeSelfDrawable(DIR_END, drawEndW, drawEndH)

//        val isUsePingFangTypeFace =
//            attr.getBoolean(R.styleable.VisionTextView_use_ping_fang_type_face, false)
//        if (isUsePingFangTypeFace) {
//            typeface = pingFangTypeFace
//        }

        val isBold = attr.getBoolean(R.styleable.VisionTextView_is_bold, false)
        if (isBold) {
            paint.isFakeBoldText = true
        }
    }

    private fun resizeDrawableIfNeeded(
        drawable: Drawable?, w: Int, h: Int, onResized: (resizedDrawable: Drawable) -> Unit
    ) {
        if (w < 0 || h < 0 || drawable == null) return
        drawable.setBounds(0, 0, w, h)
        onResized.invoke(drawable)
    }


    fun setDraw(@DrawDir dir: Int, drawWhat: Int, w: Int, h: Int) {
        val draw = ContextCompat.getDrawable(context, drawWhat)
        setDraw(dir, draw, w, h)
    }

    fun setDraw(@DrawDir dir: Int, drawable: Drawable?, w: Int, h: Int) {
        resizeDrawableIfNeeded(drawable, w, h) {
            when (dir) {
                DIR_START -> {
                    setCompoundDrawables(
                        it, compoundDrawables[1], compoundDrawables[2], compoundDrawables[3]
                    )
                }

                DIR_TOP -> {
                    setCompoundDrawables(
                        compoundDrawables[0], it, compoundDrawables[2], compoundDrawables[3]
                    )
                }

                DIR_END -> {
                    setCompoundDrawables(
                        compoundDrawables[0], compoundDrawables[1], it, compoundDrawables[3]
                    )
                }
            }
        }
    }

    fun resizeSelfDrawable(@DrawDir dir: Int, w: Int, h: Int) {
        if (compoundDrawables.isNullOrEmpty()) return
        setDraw(dir, compoundDrawables[dir], w, h)
    }


    override fun setTextSize(size: Float) {
        super.setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
    }


    fun setBold(isBold: Boolean) {
        paint.isFakeBoldText = isBold
    }


    fun removeDraw(@DrawDir dir: Int) {
        compoundDrawables.apply {
            set(dir, null)
            setCompoundDrawables(this[0], this[1], this[2], this[3])
        }
    }

}