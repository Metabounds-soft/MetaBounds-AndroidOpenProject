package com.xr.common.middleware.manager.coreV2

import android.util.Log
import com.metabounds.libglass.v2.interfaces.MBCmdV2RspMsgListener
import com.metabounds.libglass.v2.rsp.CmdV2Rsp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue

class MBBTCmdRspDispatcher : MBCmdV2RspMsgListener {
    private val mListeners = ConcurrentLinkedQueue<MBCmdV2RspMsgListener>()


    fun addListener(listener: MBCmdV2RspMsgListener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener)
        }
    }

    fun removeListener(listener: MBCmdV2RspMsgListener) {
        val iterator = mListeners.iterator()
        while (iterator.hasNext()) {
            val temp = iterator.next()
            if (temp == listener) {
                iterator.remove()
            }
        }
    }

    override fun onMetaGlassCmdRspMsg(p0: String?, p1: CmdV2Rsp<*>?) {
        p1?.let {
            MainScope().launch(Dispatchers.Main) {
                Log.d("MBBTCmdRspDispatcher", "bizType:${p1.bizType};cmdV2Type:${p1.cmdV2Type}")
                mListeners.forEach {
                    it.onMetaGlassCmdRspMsg(p0, p1)
                }
            }
        }
    }
}