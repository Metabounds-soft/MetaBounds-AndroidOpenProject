package com.xr.common.middleware.manager.coreV2

import com.metabounds.libglass.bluetooth.MetaGlassCmdRspMsgListener
import com.metabounds.libglass.bluetooth.protocol.response.CmdRsp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue

class MBBleCmdRspDispatcher : MetaGlassCmdRspMsgListener {
    private val mListeners = ConcurrentLinkedQueue<MetaGlassCmdRspMsgListener>()


    fun addListener(listener: MetaGlassCmdRspMsgListener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener)
        }
    }

    fun removeListener(listener: MetaGlassCmdRspMsgListener) {
        val iterator = mListeners.iterator()
        while (iterator.hasNext()) {
            val temp = iterator.next()
            if (temp == listener) {
                iterator.remove()
            }
        }
    }

    override fun onMetaGlassCmdRspMsg(p0: String?, p1: CmdRsp<*>?) {
        p1?.let {
            MainScope().launch(Dispatchers.Main) {
                mListeners.forEach {
                    it.onMetaGlassCmdRspMsg(p0, p1)
                }
            }
        }
    }
}