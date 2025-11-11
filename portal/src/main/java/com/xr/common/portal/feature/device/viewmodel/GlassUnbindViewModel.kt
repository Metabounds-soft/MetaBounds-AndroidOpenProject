package com.xr.common.portal.feature.device.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.metabounds.libglass.bluetooth.protocol.basic.BizType
import com.metabounds.libglass.v2.interfaces.MBCmdV2RspMsgListener
import com.metabounds.libglass.v2.mbEnum.MBCmdV2Type
import com.metabounds.libglass.v2.req.CmdV2Disconnect
import com.metabounds.libglass.v2.req.CmdV2Unbind
import com.metabounds.libglass.v2.rsp.CmdV2Rsp
import com.xr.common.middleware.viewmodel.BaseViewModel
import com.xr.common.middleware.manager.MBDeviceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GlassUnbindViewModel : BaseViewModel(), MBCmdV2RspMsgListener {

    val unbindDeviceObs = MutableLiveData<Boolean>()

    private val TAG = GlassUnbindViewModel::class.java.simpleName
    private var address = ""
    private var job: Job? = null
    private var isUnbinding = false

    override fun onMetaGlassCmdRspMsg(p0: String?, p1: CmdV2Rsp<*>) {
        if (p1.bizType == BizType.CMD_CONNECT && p1.cmdV2Type == MBCmdV2Type.CMD_CONNECTION_UNBIND.toInt()) {
            MBDeviceManager.getInstance().sendCmd(CmdV2Disconnect())
            Log.i(TAG, "onMetaGlassCmdRspMsg: job")
            job?.cancel()
            job = viewModelScope.launch(Dispatchers.IO) {
                MBDeviceManager.getInstance().unpair()
                MBDeviceManager.getInstance().destroy()
                saveUnbindDeviceData(address)
                isUnbinding = false
            }
        }
    }

    fun initData() {
        Log.i(TAG, "initData: ")
        MBDeviceManager.getInstance().addBTCmdRspListener(this)
    }

    fun unbindMozartDevice(address: String) {
        Log.i(TAG, "unbindMozartDevice: address=$address")
        this.address = address
        if (MBDeviceManager.getInstance().isConnected()) {
            MBDeviceManager.getInstance().sendCmd(CmdV2Unbind())
        }
        job = viewModelScope.launch(Dispatchers.IO) {
            Log.i(TAG, "unbindMozartDevice: job")
            delay(500)

            MBDeviceManager.getInstance().unpair()
            MBDeviceManager.getInstance().destroy()
            saveUnbindDeviceData(address)
            isUnbinding = false
        }
    }

    fun onDestroy() {
        Log.i(TAG, "onDestroy: ")
        MBDeviceManager.getInstance().removeBTCmdRspListener(this)
        job?.cancel()
    }

    private fun saveUnbindDeviceData(address: String) {
        Log.i(TAG, "saveUnbindDeviceData: address=$address")
        unbindDeviceObs.postValue(true)
    }

}