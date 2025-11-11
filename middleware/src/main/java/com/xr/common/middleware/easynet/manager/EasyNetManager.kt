package com.xr.common.middleware.easynet.manager

import android.app.Application
import android.util.Log
import com.metabounds.easynet.auxiliary.EasynetStatus
import com.metabounds.easynet.client.EasynetClient
import com.metabounds.easynet.listener.EasynetFragmentTracker
import com.metabounds.easynet.listener.EasynetProgressTracker
import com.xr.common.middleware.easynet.listener.MetaEasyNetClientListener
import com.metabounds.easynet.option.EasynetOption
import com.metabounds.easynet.option.Option
import com.xr.common.middleware.easynet.p2p.WifiP2pConnect
import com.metabounds.easynet.schedule.EasynetFuture
import com.metabounds.easynet.session.EasynetSession
import com.metabounds.netprotocol.message.Message
import com.metabounds.netprotocol.message.Status
import com.metabounds.netprotocol.payload.FileMetadata
import com.metabounds.netprotocol.payload.FileType

class EasyNetManager {
    interface EasyNetConnectState {
        fun onConnectSuccess(option: EasynetOption?)
        fun onConnectFailure(status: EasynetStatus)
        fun onSessionActive(session: EasynetSession)
        fun onSessionInactive(status: EasynetStatus)
    }

    interface MessageOperationComplete {
        fun onOperationComplete(future: EasynetFuture)
    }

    interface FilePullProgress {
        fun onOperationProgress(progress: Float)
    }

    interface EasyNetMessageArrive {
        fun onMessageArrived(msg: Message)
    }

    interface WifiP2pConnectState {
        fun onSuccess()
        fun onFailure(reason: String)
    }

    private var fragmentTotal = 0L
    private var fragmentAcceptedLength = 0L
    private var session: EasynetSession? = null
    private var TAG = this.javaClass.simpleName
    private var application: Application? = null
    private val wifiP2pConnect: WifiP2pConnect by lazy { WifiP2pConnect(this.application!!.applicationContext) }
    internal val wifiP2pConnectStateList: MutableSet<WifiP2pConnectState> = mutableSetOf()
    internal val easyNetConnectStateList: MutableSet<EasyNetConnectState> = mutableSetOf()
    internal val easyNetMessageArriveList: MutableSet<EasyNetMessageArrive> = mutableSetOf()
    private val filePullProgressList: MutableSet<FilePullProgress> = mutableSetOf()
    private val messageOperationCompleteList: MutableSet<MessageOperationComplete> = mutableSetOf()
    private val easynetFragmentTrackerList: MutableSet<EasynetFragmentTracker> = mutableSetOf()

    companion object {
        private val mInstance by lazy {
            EasyNetManager()
        }

        fun getInstance(): EasyNetManager {
            return mInstance
        }
    }

    fun initApplication(application: Application) {
        this.application = application
    }

    private val easyNetClientListener by lazy { MetaEasyNetClientListener() }

    private val bootstrap by lazy {
        EasynetClient(application, easyNetClientListener)
    }

    fun addFilePullProgress(filePullProgress: FilePullProgress) {
        filePullProgressList.add(filePullProgress)
    }

    fun removeFilePullProgress(filePullProgress: FilePullProgress) {
        filePullProgressList.remove(filePullProgress)
    }

    fun addMessageOperationComplete(messageOperationComplete: MessageOperationComplete) {
        messageOperationCompleteList.add(messageOperationComplete)
    }

    fun removeMessageOperationComplete(messageOperationComplete: MessageOperationComplete) {
        messageOperationCompleteList.remove(messageOperationComplete)
    }

    fun addEasyNetConnectState(easyNetConnectState: EasyNetConnectState) {
        easyNetConnectStateList.add(easyNetConnectState)
    }

    fun removeEasyNetConnectState(easyNetConnectState: EasyNetConnectState) {
        easyNetConnectStateList.remove(easyNetConnectState)
    }

    fun addEasyNetMessageArrive(easyNetMessageArrive: EasyNetMessageArrive) {
        easyNetMessageArriveList.add(easyNetMessageArrive)
    }

    fun removeEasyNetMessageArrive(easyNetMessageArrive: EasyNetMessageArrive) {
        easyNetMessageArriveList.remove(easyNetMessageArrive)
    }

    fun addWifiP2pConnectState(wifiP2pConnectState: WifiP2pConnectState) {
        wifiP2pConnectStateList.add(wifiP2pConnectState)
    }

    fun removeWifiP2pConnectState(wifiP2pConnectState: WifiP2pConnectState) {
        wifiP2pConnectStateList.remove(wifiP2pConnectState)
    }

    fun addEasynetFragmentTracker(easynetFragmentTracker: EasynetFragmentTracker) {
        easynetFragmentTrackerList.add(easynetFragmentTracker)
    }

    fun removeEasynetFragmentTracker(easynetFragmentTracker: EasynetFragmentTracker) {
        easynetFragmentTrackerList.remove(easynetFragmentTracker)
    }

    fun isActive(): Boolean {
        return if (session == null) {
            false
        } else {
            session?.isActive == true
        }
    }

    fun setSession(session: EasynetSession?) {
        this.session = session
    }

    fun startClient(ipAddr: String, port: Int) {
        Log.d(TAG, "startClient $ipAddr:$port")
        try {
            if (isActive()) {
                easyNetConnectStateList.forEach {
                    session?.let { it1 -> it.onSessionActive(it1) }
                }
            } else {
                bootstrap.shutdown()
                bootstrap.option(Option.REMOTE_IP_ADDR, ipAddr)
                bootstrap.option(Option.REMOTE_IP_PORT, port)
                bootstrap.option(Option.LITTLE_ENDIAN, true)
                bootstrap.bootstrap()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopClient() {
        try {
            bootstrap.shutdown()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendMessage(msg: Message, timeout: Long = 60000) {
        if (session != null && session!!.isActive) {
            session!!.delivery(msg).addListener { future: EasynetFuture ->
                messageOperationCompleteList.forEach {
                    it.onOperationComplete(future)
                }
            }.addProgressTracker(object : EasynetProgressTracker {
                private val MIN_DELTA_PERCENT = 5.0f
                private var total: Long = 0
                private var type: Int = 0

                override fun onStart(session: Int, total: Long, reserve: Int) {
                    this.total = total
                    this.type = reserve
                }

                override fun onProgress(session: Int, progress: Long, reserve: Int) {
                    val current = progress.toFloat() / total * 100
                    if (type == FileType.FT_VIDEO.value() || type == FileType.FT_IMAGE.value()) {
                        filePullProgressList.forEach {
                            it.onOperationProgress(current)
                        }
                    }
                }

                override fun onComplete(session: Int, reserve: Int) {
                }
            }).addFragmentTracker(object : EasynetFragmentTracker {
                override fun onStart(session: Int, obj: Any?, reserver: Int) {
                    fragmentAcceptedLength = 0L
                    if (obj is FileMetadata) {
                        if (obj.type == FileType.FT_VIDEO) {
                            fragmentTotal = obj.size
                        }
                    }
                    easynetFragmentTrackerList.forEach {
                        it.onStart(session, obj, reserver)
                    }
                }

                override fun onFragment(session: Int, data: ByteArray?, offset: Int, length: Int) {
                    fragmentAcceptedLength += length
                    easynetFragmentTrackerList.forEach {
                        it.onFragment(session, data, offset, length)
                    }
                    if (fragmentTotal != 0L) {
                        filePullProgressList.forEach {
                            it.onOperationProgress((fragmentAcceptedLength / fragmentTotal.toFloat()) * 100)
                        }
                    }
                }

                override fun onComplete(session: Int) {
                    easynetFragmentTrackerList.forEach {
                        it.onComplete(session)
                    }
                }

                override fun onException(session: Int, status: Status?) {
                    easynetFragmentTrackerList.forEach {
                        it.onException(session, status)
                    }
                }
            })
        }
    }

    fun isAttemptToConnect(): Boolean {
        return wifiP2pConnect.isAttemptToConnect
    }

    fun removeAttemptToConnectTag() {
        wifiP2pConnect.isAttemptToConnect = false
    }

    fun openP2pConnect(macAddress: String, ip: String, port: Int) {
        wifiP2pConnect.removeGroup()
        wifiP2pConnect.connect(macAddress, ip, port)
    }

    fun closeP2pConnect() {
        try {
            wifiP2pConnect.disConnect()
            stopClient()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}