package com.xr.common.portal.feature.device.view.dialog

import android.content.Context
import android.view.View
import com.xr.common.middleware.view.BaseDialog
import com.xr.common.portal.databinding.DialogConfirmBinding

class ConfirmDialog(context: Context) : BaseDialog<DialogConfirmBinding>(context) {

    private var onConfirm: (() -> Unit?)? = null
    private var onCancel: (() -> Unit?)? = null
    private var isSelect = false
    override fun getViewBinding(): DialogConfirmBinding {
        return DialogConfirmBinding.inflate(layoutInflater)
    }

    override fun initView() {
        mViewBinding.btnConfirm.setOnClickListener {
            onConfirm?.invoke()
        }
        mViewBinding.btnCancel.setOnClickListener {
            if (onCancel == null) {
                dismiss()
            } else {
                onCancel?.invoke()
            }
        }
    }

    fun setOnConfirm(confirm: () -> Unit): ConfirmDialog {
        this.onConfirm = confirm
        return this
    }

    fun setOnCancel(cancel: () -> Unit): ConfirmDialog {
        this.onCancel = cancel
        return this
    }

    fun setTitle(str: String?): ConfirmDialog {
        mViewBinding.tvTitle.text = str
        if (str.isNullOrEmpty()) {
            mViewBinding.tvTitle.visibility = View.GONE
        } else {
            mViewBinding.tvTitle.visibility = View.VISIBLE
        }
        return this
    }

    fun setContent(str: String): ConfirmDialog {
        mViewBinding.tvContent.text = str
        if (str.isNullOrEmpty()) {
            mViewBinding.tvContent.visibility = View.GONE
        } else {
            mViewBinding.tvContent.visibility = View.VISIBLE
        }
        return this
    }

    fun setContentVisible(boolean: Boolean): ConfirmDialog {
        mViewBinding.tvContent.visibility = if (boolean) View.VISIBLE else View.GONE
        return this
    }

    fun setCancel(str: String): ConfirmDialog {
        mViewBinding.btnCancel.text = str
        return this
    }

    fun setConfirm(str: String): ConfirmDialog {
        mViewBinding.btnConfirm.text = str
        return this
    }

    fun setConfirmVisible(boolean: Boolean): ConfirmDialog {
        mViewBinding.btnConfirm.visibility = if (boolean) View.VISIBLE else View.GONE
        return this
    }

    fun setCancelVisible(boolean: Boolean): ConfirmDialog {
        mViewBinding.btnCancel.visibility = if (boolean) View.VISIBLE else View.GONE
        return this
    }

    fun setConfirmBg(resId: Int): ConfirmDialog {
        mViewBinding.btnConfirm.setBackgroundResource(resId)
        return this
    }

    fun setConfirmTextColor(colorId: Int): ConfirmDialog {
        mViewBinding.btnConfirm.setTextColor(context.resources.getColor(colorId, null))
        return this
    }

    fun setRadioText(str: String): ConfirmDialog {
        mViewBinding.tvRadio.text = str
        if (str.isNullOrEmpty()) {
            mViewBinding.tvRadio.visibility = View.GONE
        } else {
            mViewBinding.tvRadio.visibility = View.VISIBLE
        }

        return this
    }

    fun getRadioState(): Boolean {
        return mViewBinding.tvRadio.isChecked
    }
}