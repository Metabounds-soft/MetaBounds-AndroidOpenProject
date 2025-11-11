package com.xr.common.middleware.manager.coreV2

import com.metabounds.libglass.bluetooth.protocol.basic.BizType
import com.metabounds.libglass.v2.interfaces.MBV2ReadCommDataListener
import com.metabounds.libglass.v2.mbEnum.MBPackType
import java.util.concurrent.ConcurrentLinkedQueue

class MBCommDataDispatcher : MBV2ReadCommDataListener {
    private val mListeners = ConcurrentLinkedQueue<MBV2ReadCommDataListener>()


    fun addListener(listener: MBV2ReadCommDataListener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener)
        }
    }


    fun removeListener(listener: MBV2ReadCommDataListener) {
        val iterator = mListeners.iterator()
        while (iterator.hasNext()) {
            val temp = iterator.next()
            if (temp == listener) {
                iterator.remove()
            }
        }
    }

    override fun data(p0: BizType, p1: Int, p2: MBPackType, p3: Int, p4: ByteArray) {
        mListeners.forEach {
            it.data(p0, p1, p2, p3, p4)
        }
    }

    override fun error(p0: BizType, p1: Int, p2: Int, p3: Int) {

    }
}