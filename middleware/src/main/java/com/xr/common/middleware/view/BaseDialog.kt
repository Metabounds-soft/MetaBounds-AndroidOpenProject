package com.xr.common.middleware.view


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.xr.common.middleware.R
import kotlin.math.max
import kotlin.math.min

abstract class BaseDialog<V : ViewBinding>(
    context: Context, val style: Int = R.style.DialogStyle, isMarginBottom: Boolean = true
) : Dialog(context, style) {
    constructor(context: Context) : this(context, R.style.DialogStyle, true)
    constructor(context: Context, dialogStyle: Int) : this(context, dialogStyle, true)
    constructor(context: Context, isMarginBottom: Boolean = true) : this(
        context, R.style.DialogStyle, isMarginBottom
    )

    protected val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
    private var isMarginBottom = isMarginBottom

    protected lateinit var mViewBinding: V


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        mViewBinding = getViewBinding()
        setContentView(mViewBinding.root)
//        val marginBottom = getAdjustNavigationBarHeight(mViewBinding.root)
        var marginBottom = SizeUtils.dp2px(32f)
        window?.run {
//            window?.setWindowAnimations(R.style.window_anim)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setBackgroundDrawableResource(android.R.color.transparent)
            decorView.setPadding(0, 0, 0, 0)
            setGravity(Gravity.BOTTOM)
            if (isMarginBottom()) {
                attributes.width = (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(48f))
                attributes.verticalMargin =
                    (marginBottom / (ScreenUtils.getScreenHeight() + 0.000f))
            } else {
                attributes.width = ScreenUtils.getScreenWidth()
                attributes.height = ViewGroup.LayoutParams.WRAP_CONTENT
            }

        }
        setCanceledOnTouchOutside(true)
        initView()
    }

    abstract fun getViewBinding(): V

    abstract fun initView()

    protected open fun isMarginBottom(): Boolean {
        return isMarginBottom
    }


    /**
     * Get adjust navigation bar height
     *
     * @param contentView
     * @return
     *
     * 获取Dialog距离底部需要调整的高度
     *
     */
    private fun getAdjustNavigationBarHeight(contentView: View): Int {
        if (true) {
            return SizeUtils.dp2px(32f)
        }
        //如果有虚拟按键
        if (isNavigationBarShown(contentView)) {
            //获取虚拟按键的高度
            val navigationBarHeight = compactGetNavigationBarHeight(contentView)
            //虚拟按键高度如果为0
            return if (navigationBarHeight == 0)
            //返回固定值32px
                SizeUtils.dp2px(32f)
            else
            //虚拟按键高度不为0，在虚拟按键高度上再增加固定值32px
                SizeUtils.dp2px(32f)
        }

        //没有虚拟按键，则直接返回固定值32px
        return SizeUtils.dp2px(32f)

    }


    /**
     * Has navigation bar
     *
     * @param contentView
     * @return
     * 判断是否有虚拟按键
     */
    private fun isNavigationBarShown(contentView: View): Boolean {
        val rootWindowInsets = ViewCompat.getRootWindowInsets(contentView)
        val navigationBarShown =
            rootWindowInsets?.isVisible(WindowInsetsCompat.Type.navigationBars())
        val id = context.resources.getIdentifier("config_showNavigationBar", "bool", "android")
        val navigationBarShownByAndroidId = id > 0 && context.resources.getBoolean(id)
        return navigationBarShownByAndroidId || navigationBarShown == true
    }

    /**
     * Compact get navigation bar height
     *
     * @param view
     * @return
     *
     *
     * 计算虚拟按键高度
     *
     */
    private fun compactGetNavigationBarHeight(view: View): Int {
        val rootWindowInsets = ViewCompat.getRootWindowInsets(view)
        val heightByWindowInsets =
            rootWindowInsets?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom
        //虚拟按键高度id
        val resourceId =
            context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        var heightById = 0
        if (resourceId > 0) {
            heightById = context.resources.getDimensionPixelSize(resourceId)
        }
        return when (Build.BRAND.uppercase()) {
            //华为、魅族
            "HUAWEI", "MEIZU" -> {
                if (heightByWindowInsets == null) {
                    heightById / 2
                } else {
                    min(heightByWindowInsets, heightById / 2)
                }
            }

            //OPPO全面屏的模式下，虚拟按键高度48
            //传统三键导航方式的虚拟按键高度是132
            "OPPO" -> {
                if (heightByWindowInsets == null) {
                    min(heightById, 48) / 2
                } else {
                    min(heightByWindowInsets, heightById / 2)
                }
            }

            //小米有两种虚拟按键，全面屏的模式下，虚拟按键高度56
            //传统三键导航方式的虚拟按键高度是156
            "XIAOMI" -> {
                if (heightByWindowInsets == null) {
                    max(heightById, 165 / 2)
                } else {
                    min(heightByWindowInsets, heightById)
                }
            }

            else -> {
                if (heightByWindowInsets == null) {
                    heightById
                } else {
                    min(heightByWindowInsets, heightById)
                }
            }
        }
    }

}