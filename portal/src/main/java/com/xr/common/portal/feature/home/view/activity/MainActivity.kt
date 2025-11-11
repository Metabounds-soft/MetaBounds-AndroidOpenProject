package com.xr.common.portal.feature.home.view.activity

import android.content.Intent
import android.os.Looper
import android.view.LayoutInflater
import com.blankj.utilcode.util.ToastUtils
import com.iflytek.cloud.SpeechConstant
import com.iflytek.cloud.SpeechUtility
import com.jeremyliao.liveeventbus.LiveEventBus
import com.xr.common.middleware.R
import com.xr.common.middleware.base.DeviceType
import com.xr.common.middleware.base.LiveEventBusKey
import com.xr.common.middleware.base.getDeviceType
import com.xr.common.middleware.manager.MBDeviceManager
import com.xr.common.middleware.utils.SpeechFormatUtils
import com.xr.common.middleware.utils.checkDeviceConnect
import com.xr.common.middleware.view.BaseActivity
import com.xr.common.middleware.viewmodel.BaseViewModel
import com.xr.common.portal.databinding.ActivityMainBinding
import com.xr.common.portal.feature.device.view.activity.GlassDeviceBindActivity
import com.xr.common.portal.feature.device.view.activity.GlassDeviceUnbindActivity
import com.xr.common.portal.feature.device.view.activity.GlassRemoteControlActivity
import com.xr.common.portal.feature.task.MozartTaskManager
import com.xr.common.portal.feature.translate.view.TranslateSimultaneousActivity

/**
 * Description:
 * CreateDate:     2023/5/11 11:44
 * Author:         agg
 */
class MainActivity : BaseActivity<ActivityMainBinding, BaseViewModel>() {

    private var isShooting = false

    override fun initBinding(inflater: LayoutInflater): ActivityMainBinding =
        ActivityMainBinding.inflate(inflater)

    override fun initData() {
        SpeechFormatUtils.initOpusCodec()
        MozartTaskManager.getInstance().startTask()
        Looper.getMainLooper().queue.addIdleHandler {
            /**讯飞语音初始化*/
            SpeechUtility.createUtility(
                this@MainActivity.application, SpeechConstant.APPID + "=8e1bb4a6"
            )

            // 返回false表示只处理一次，true则会继续接收
            false
        }
    }

    override fun initViewModel() {
        LiveEventBus.get<Boolean>(LiveEventBusKey.UNBIND_SUCCESS).observe(this) {
            ToastUtils.showShort(getString(R.string.unbind_success))
        }
        LiveEventBus.get<Boolean>(LiveEventBusKey.BIND_SUCCESS).observe(this) {
            when (MBDeviceManager.getInstance().getBuildDevice()?.sourceData?.getDeviceType()) {
                DeviceType.G12S0, DeviceType.G12X1 -> {
                }

                else -> {
                }
            }
        }
    }

    override fun initView() {
        binding.addGlass.setOnClickListener {
            startActivity(Intent(this, GlassDeviceBindActivity::class.java))
        }
        binding.unbindGlass.setOnClickListener {
            checkDeviceConnect {
                startActivity(
                    Intent(this, GlassDeviceUnbindActivity::class.java)
                )
            }
        }
        binding.simultaneous.setOnClickListener {
            checkDeviceConnect {
                startActivity(
                    Intent(this, TranslateSimultaneousActivity::class.java)
                )
            }
        }
        binding.remoteControl.setOnClickListener {
            checkDeviceConnect {
                startActivity(
                    Intent(this, GlassRemoteControlActivity::class.java)
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MozartTaskManager.getInstance().destroy()
        MBDeviceManager.getInstance().destroy()
        SpeechFormatUtils.releaseOpusCodec()
    }

}