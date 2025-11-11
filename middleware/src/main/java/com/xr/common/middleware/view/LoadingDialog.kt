package com.xr.common.middleware.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.annotation.StyleRes
import com.xr.common.middleware.R

class LoadingDialog constructor(context: Context, @StyleRes themeResId: Int) :
    Dialog(context, themeResId) {

    constructor(context: Context) : this(context, R.style.dialog)

    private var tvMsg: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_loading)
        val attributes = window?.attributes
        attributes?.gravity = Gravity.CENTER
        window?.attributes = attributes
        setCanceledOnTouchOutside(false)

        tvMsg = findViewById(R.id.tv_msg)
    }


    fun setMsg(msg: String?) {
        if (msg.isNullOrEmpty()) {
            tvMsg?.visibility = View.GONE
        } else {
            tvMsg?.visibility = View.VISIBLE
            tvMsg?.text = msg

        }

    }

    override fun onBackPressed() {

    }
}