package com.xr.common.middleware.manager.coreV2

import android.util.Log
import com.metabounds.libglass.v2.interfaces.MBCmdV2ReqMsgListener
import com.metabounds.libglass.v2.req.CmdV2Req
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue

class MBBTCmdReqDispatcher : MBCmdV2ReqMsgListener {
    private val mListeners = ConcurrentLinkedQueue<MBCmdV2ReqMsgListener>()


    fun addListener(listener: MBCmdV2ReqMsgListener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener)
        }
        Log.d("MBBTCmdReqDispatcher", "MBBTCmdReqDispatcher addListener size:${mListeners.size}")
    }

    fun removeListener(listener: MBCmdV2ReqMsgListener) {
        val iterator = mListeners.iterator()
        while (iterator.hasNext()) {
            val temp = iterator.next()
            if (temp == listener) {
                iterator.remove()
            }
        }
        Log.d("MBBTCmdReqDispatcher", "MBBTCmdReqDispatcher removeListener size:${mListeners.size}")
    }


    override fun onMetaGlassCmdReqMsg(p0: String?, p1: CmdV2Req<*>?) {
        p1?.let {
            Log.d(
                "MBBTCmdReqDispatcher",
                "p1:" + p1.bizType + "  ;cmdType：" + p1.cmdV2Type + ";mListeners size:${mListeners.size}"
            )
            MainScope().launch(Dispatchers.Main) {
                mListeners.forEach {
                    it.onMetaGlassCmdReqMsg(p0, p1)
                }
            }
        }
    }
}