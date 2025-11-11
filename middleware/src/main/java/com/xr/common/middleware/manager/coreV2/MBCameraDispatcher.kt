package com.xr.common.middleware.manager.coreV2

import android.util.Log
import com.metabounds.libglass.v2.bean.CommDataHeadBean
import com.metabounds.libglass.v2.bean.camera.CameraHeadBean
import com.metabounds.libglass.v2.interfaces.MBV2ReadCameraListener
import java.util.concurrent.ConcurrentLinkedQueue

class MBCameraDispatcher : MBV2ReadCameraListener {
    private val mListeners = ConcurrentLinkedQueue<MBV2ReadCameraListener>()


    override fun prepare(p0: CommDataHeadBean?, p1: CameraHeadBean?) {
        Log.d("MBCameraDispatcher", "recv prepare:${mListeners.size}")
        mListeners.forEach {
            it.prepare(p0, p1)
        }
    }

    override fun recvData(p0: Int, p1: ByteArray?) {
        mListeners.forEach {
            it.recvData(p0, p1)
        }
    }

    override fun finish(p0: Int, p1: ByteArray?) {
        mListeners.forEach {
            it.finish(p0, p1)
        }
    }

    override fun error(p0: Int, p1: Int) {
        mListeners.forEach {
            it.error(p0, p1)
        }
    }


    fun addListener(listener: MBV2ReadCameraListener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener)
        }
    }


    fun removeListener(listener: MBV2ReadCameraListener) {
        val iterator = mListeners.iterator()
        while (iterator.hasNext()) {
            val temp = iterator.next()
            if (temp == listener) {
                iterator.remove()
            }
        }
    }
}