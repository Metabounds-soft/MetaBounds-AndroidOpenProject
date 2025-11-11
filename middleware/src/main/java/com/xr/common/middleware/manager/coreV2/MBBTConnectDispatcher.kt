package com.xr.common.middleware.manager.coreV2

import android.util.Log
import com.metabounds.libglass.bluetooth.MetaGlassBtConnectListener
import com.metabounds.libglass.bluetooth.MetaGlassState
import com.metabounds.libglass.v2.req.CmdV2GetConfigReq
import com.metabounds.libglass.v2.req.CmdV2GetVersionReq
import com.metabounds.libglass.v2.req.CmdV2SetTimeReq
import com.xr.common.middleware.manager.MBDeviceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue

class MBBTConnectDispatcher : MetaGlassBtConnectListener {
    private var state: MetaGlassState? =
        MetaGlassState(MetaGlassState.State.DISCONNECT, MetaGlassState.Error.NO_ERROR)
    private val mListeners = ConcurrentLinkedQueue<MetaGlassBtConnectListener>()


    fun addListener(listener: MetaGlassBtConnectListener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener)
        }
        state?.let {
            listener.onMetaGlassBtConnectStateChange("", it)
        }
    }

    fun removeListener(listener: MetaGlassBtConnectListener) {
        val iterator = mListeners.iterator()
        while (iterator.hasNext()) {
            val temp = iterator.next()
            if (temp == listener) {
                iterator.remove()
            }
        }
    }


    override fun onMetaGlassBtConnectStateChange(p0: String?, p1: MetaGlassState?) {
        state = p1
        p1?.let {
            MainScope().launch(Dispatchers.Main) {
                Log.d("MBBTConnectDispatcher", "MetaGlassState:${p1}")
                mListeners.forEach {
                    it.onMetaGlassBtConnectStateChange(p0, p1)
                }
            }
        }
        if (p1?.isConnected == true) {
            sendCmd()
        }
    }


    private fun sendCmd() {
        Log.d(
            "MBBTConnectDispatcher", "MBBTConnceted CmdV2SetTimeReq ${System.currentTimeMillis()}"
        )
        MBDeviceManager.getInstance().sendCmd(CmdV2SetTimeReq(System.currentTimeMillis()))
        MBDeviceManager.getInstance().sendCmd(CmdV2GetVersionReq())
        MBDeviceManager.getInstance().sendCmd(CmdV2GetConfigReq())
    }
}