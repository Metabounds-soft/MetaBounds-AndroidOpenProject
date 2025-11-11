package com.xr.common.middleware.manager.coreV2

import android.util.Log
import com.metabounds.libglass.bluetooth.MetaGlassConnectListener
import com.metabounds.libglass.bluetooth.MetaGlassState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue

class MBBleConnectDispatcher : MetaGlassConnectListener {
    private var state: MetaGlassState? =
        MetaGlassState(MetaGlassState.State.DISCONNECT, MetaGlassState.Error.NO_ERROR)
    private val mListeners = ConcurrentLinkedQueue<MetaGlassConnectListener>()


    fun addListener(listener: MetaGlassConnectListener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener)
        }
        listener.onMetaGlassConnectStateChange("", state)
    }

    fun removeListener(listener: MetaGlassConnectListener) {
        val iterator = mListeners.iterator()
        while (iterator.hasNext()) {
            val temp = iterator.next()
            if (temp == listener) {
                iterator.remove()
            }
        }
    }

    override fun onMetaGlassConnectStateChange(p0: String?, p1: MetaGlassState?) {
        state = p1
        p1?.let {
            Log.e("TGA", "MBBleConnectDispatcher:${p1}")
            MainScope().launch(Dispatchers.Main) {
                mListeners.forEach {
                    it.onMetaGlassConnectStateChange(p0, p1)
                }
            }
        }
    }
}