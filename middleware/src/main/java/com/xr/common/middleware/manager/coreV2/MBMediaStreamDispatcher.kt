package com.xr.common.middleware.manager.coreV2

import com.metabounds.libglass.bluetooth.protocol.basic.BizType
import com.metabounds.libglass.v2.interfaces.MBMediaStreamListener
import java.util.concurrent.ConcurrentLinkedQueue

class MBMediaStreamDispatcher : MBMediaStreamListener {
    private val mListeners = ConcurrentLinkedQueue<MBMediaStreamListener>()

    override fun onStream(
        p0: String?, p1: BizType?, p2: Int, p3: Short, p4: Short, p5: Byte, p6: Int, p7: ByteArray?
    ) {
        mListeners.forEach {
            it.onStream(p0, p1, p2, p3, p4, p5, p6, p7)
        }
    }


    fun addListener(listener: MBMediaStreamListener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener)
        }
    }

    fun removeListener(listener: MBMediaStreamListener) {
        val iterator = mListeners.iterator()
        while (iterator.hasNext()) {
            val temp = iterator.next()
            if (temp == listener) {
                iterator.remove()
            }
        }
    }
}