package com.xr.common.middleware.manager.coreV2

import com.metabounds.libglass.bluetooth.MetaGlassCmdReqMsgListener
import com.metabounds.libglass.bluetooth.protocol.request.CmdReq
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue

class MBBleCmdReqDispatcher : MetaGlassCmdReqMsgListener {
    private val mListeners = ConcurrentLinkedQueue<MetaGlassCmdReqMsgListener>()


    fun addListener(listener: MetaGlassCmdReqMsgListener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener)
        }
    }

    fun removeListener(listener: MetaGlassCmdReqMsgListener) {
        val iterator = mListeners.iterator()
        while (iterator.hasNext()) {
            val temp = iterator.next()
            if (temp == listener) {
                iterator.remove()
            }
        }
    }

    override fun onMetaGlassCmdReqMsg(p0: String?, p1: CmdReq<*>?) {
        p1?.let {
            MainScope().launch(Dispatchers.Main) {
                mListeners.forEach {
                    it.onMetaGlassCmdReqMsg(p0, p1)
                }
            }
        }
    }
}