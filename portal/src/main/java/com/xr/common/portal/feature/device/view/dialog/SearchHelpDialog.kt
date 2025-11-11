package com.xr.common.portal.feature.device.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Html
import android.view.Gravity
import android.view.LayoutInflater
import com.blankj.utilcode.util.ScreenUtils
import com.xr.common.portal.R
import com.xr.common.portal.databinding.DialogGlassSearchHelpBinding

class SearchHelpDialog(context: Context) :
    Dialog(context, com.xr.common.middleware.R.style.DialogStyle) {

    private var onAgain: (() -> Unit?)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view =
            LayoutInflater.from(context).inflate(R.layout.dialog_glass_search_help, null, false)
        val viewBind = DialogGlassSearchHelpBinding.bind(view)
        setContentView(viewBind.root)

        window?.run {
            attributes?.width = ScreenUtils.getScreenWidth()
            attributes.gravity = Gravity.BOTTOM
        }

        viewBind.tvContent.text =
            Html.fromHtml(context.getString(com.xr.common.middleware.R.string.glass_search_help))

        viewBind.btnSearchAgain.setOnClickListener {
            dismiss()
            onAgain?.invoke()
        }
        viewBind.ivClose.setOnClickListener {
            dismiss()
        }
    }


    fun setOnAgainClickListener(onAgain: () -> Unit) {
        this.onAgain = onAgain
    }

}