package com.xr.common.portal.feature.device.view.dialog

import android.content.Context
import android.util.Log
import android.view.View
import com.metabounds.libglass.v2.bean.MBDevice
import com.xr.common.middleware.base.DeviceType
import com.xr.common.middleware.base.getDeviceType
import com.xr.common.middleware.view.BaseDialog
import com.xr.common.portal.R
import com.xr.common.portal.databinding.DialogGlassBindBinding

class NewGlassBindDialog(val mContext: Context, val mbDevice: MBDevice) :
    BaseDialog<DialogGlassBindBinding>(mContext) {

    private val TAG = NewGlassBindDialog::class.java.simpleName
    private var newGlassBindDialogListener: NewGlassBindDialogListener? = null
    private var state = 0

    override fun getViewBinding(): DialogGlassBindBinding =
        DialogGlassBindBinding.inflate(layoutInflater)

    override fun isMarginBottom(): Boolean = false

    override fun initView() {
        Log.i(TAG, "initView: ")
        setCanceledOnTouchOutside(false)
        mViewBinding.btnCancel.setOnClickListener() {
            if (state == 2) {
                newGlassBindDialogListener?.onConfirm()
            } else {
                dismiss()
                newGlassBindDialogListener?.onCancel()
            }
        }
        mViewBinding.btnEnter.setOnClickListener {
            if (state == 2) {
                newGlassBindDialogListener?.onConfirm()
            } else {
                setConnecting()
                newGlassBindDialogListener?.onRefresh()
            }
        }

        mViewBinding.tvTitle.text = mbDevice.name
        initData()
    }

    private fun initData(isSuccess: Boolean = false) {
        Log.i(TAG, "initData: device=${mbDevice.sourceData.getDeviceType()},name=${mbDevice.name}")
        if (mbDevice.sourceData.getDeviceType() == DeviceType.G12X1) {
            mViewBinding.ivConnecting.setImageResource(if (isSuccess) R.drawable.ic_mozart_audio_connect_success else R.drawable.ic_audio_connecting)
        } else if (mbDevice.sourceData.getDeviceType() == DeviceType.G12S0) {
            mViewBinding.ivConnecting.setImageResource(if (isSuccess) R.drawable.ic_vel_connect_success else R.drawable.ic_vel_connecting)
        } else {
            mViewBinding.ivConnecting.setImageResource(if (isSuccess) R.drawable.ic_vel_connect_success else R.drawable.ic_vel_connecting)
        }
    }

    fun setConnecting() {
        Log.i(TAG, "setConnecting: ")
        state = 1
        mViewBinding.tvState.text =
            mContext.getString(com.xr.common.middleware.R.string.cofirm_glass_bind)
        mViewBinding.ivState.visibility = View.GONE
        mViewBinding.tvPiaring.visibility = View.VISIBLE
        mViewBinding.ivConnecting.visibility = View.VISIBLE
        mViewBinding.ivDisconnect.visibility = View.GONE
        mViewBinding.btnEnter.visibility = View.GONE
        mViewBinding.btnEnter.text = mContext.getString(com.xr.common.middleware.R.string.confirm)

        initData()
    }

    fun setConnectFail() {
        Log.i(TAG, "setConnectFail: ")
        state = 0
        mViewBinding.tvState.text =
            mContext.getString(com.xr.common.middleware.R.string.connect_fail)
        mViewBinding.ivState.visibility = View.VISIBLE
        mViewBinding.btnEnter.text =
            mContext.getString(com.xr.common.middleware.R.string.pair_glass_retry)
        mViewBinding.btnEnter.visibility = View.VISIBLE
        mViewBinding.tvPiaring.visibility = View.GONE
        mViewBinding.ivConnecting.visibility = View.GONE
        mViewBinding.ivDisconnect.visibility = View.VISIBLE

        if (mbDevice.sourceData.getDeviceType() == DeviceType.G12X1) {
            mViewBinding.ivDisconnect.setImageResource(R.drawable.ic_mozart_audio_disconnect)
        } else {
            mViewBinding.ivDisconnect.setImageResource(R.drawable.ic_vel_disconnect)
        }
    }

    fun setConnectSuccess() {
        Log.i(TAG, "setConnectSuccess: ")
        state = 2
        mViewBinding.tvState.text =
            mContext.getString(com.xr.common.middleware.R.string.connect_success)
        mViewBinding.ivState.visibility = View.GONE
        mViewBinding.tvPiaring.visibility = View.GONE
        mViewBinding.btnCancel.visibility = View.GONE
        mViewBinding.btnEnter.visibility = View.VISIBLE

        initData(true)
    }

    fun setOnNewGlassBindDialogListener(listener: NewGlassBindDialogListener) {
        this.newGlassBindDialogListener = listener
    }
}

interface NewGlassBindDialogListener {
    fun onCancel()
    fun onConfirm()
    fun onRefresh()
}