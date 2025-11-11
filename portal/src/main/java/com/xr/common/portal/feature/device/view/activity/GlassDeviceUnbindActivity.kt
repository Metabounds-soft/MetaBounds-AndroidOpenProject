package com.xr.common.portal.feature.device.view.activity

import android.view.LayoutInflater
import com.jeremyliao.liveeventbus.LiveEventBus
import com.xr.common.middleware.view.BaseActivity
import com.xr.common.middleware.R
import com.xr.common.middleware.base.LiveEventBusKey
import com.xr.common.portal.databinding.ActivityGlassDeviceUnbindBinding
import com.xr.common.middleware.manager.MBDeviceManager
import com.xr.common.portal.feature.device.viewmodel.GlassUnbindViewModel

class GlassDeviceUnbindActivity :
    BaseActivity<ActivityGlassDeviceUnbindBinding, GlassUnbindViewModel>() {

    override fun initBinding(inflater: LayoutInflater): ActivityGlassDeviceUnbindBinding =
        ActivityGlassDeviceUnbindBinding.inflate(layoutInflater)

    override fun initData() {
        viewModel.initData()
    }

    override fun initViewModel() {
        viewModel.unbindDeviceObs.observe(this) {
            if (it) {
                LiveEventBus.get<Boolean>(LiveEventBusKey.UNBIND_SUCCESS).post(true)
                finish()
            }
        }
    }

    override fun initView() {
        binding.header.title.setText(R.string.unbind_glass)
        binding.header.ivBack.setOnClickListener { finish() }
        binding.unbindCancle.setOnClickListener { finish() }
        binding.unbindConfirm.setOnClickListener {
            viewModel.unbindMozartDevice(MBDeviceManager.getInstance().getDeviceIdentity())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onDestroy()
    }

}