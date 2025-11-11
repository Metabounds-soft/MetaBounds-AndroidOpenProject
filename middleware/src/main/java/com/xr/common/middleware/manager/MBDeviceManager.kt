package com.xr.common.middleware.manager

import android.content.Context
import android.util.Log
import com.metabounds.libglass.bluetooth.MetaGlassBtConnectListener
import com.metabounds.libglass.bluetooth.MetaGlassCmdReqMsgListener
import com.metabounds.libglass.bluetooth.MetaGlassCmdRspMsgListener
import com.metabounds.libglass.bluetooth.MetaGlassConnectListener
import com.metabounds.libglass.bluetooth.MetaGlassState
import com.metabounds.libglass.bluetooth.protocol.basic.MetaGlassCmd
import com.metabounds.libglass.bluetooth.protocol.bean.ConnectParam
import com.metabounds.libglass.v2.MBPlatform
import com.metabounds.libglass.v2.bean.CommDataV2
import com.metabounds.libglass.v2.bean.MBDevice
import com.metabounds.libglass.v2.bean.MBProtocol
import com.metabounds.libglass.v2.bt.BTManager
import com.metabounds.libglass.v2.interfaces.MBCmdV2ReqMsgListener
import com.metabounds.libglass.v2.interfaces.MBCmdV2RspMsgListener
import com.metabounds.libglass.v2.interfaces.MBMediaStreamListener
import com.metabounds.libglass.v2.interfaces.MBV2ReadCameraListener
import com.metabounds.libglass.v2.interfaces.MBV2ReadCommDataListener
import com.metabounds.libglass.v2.mbEnum.MBCompany
import com.metabounds.libglass.v2.mbEnum.MBConnectivity
import com.xr.common.middleware.manager.coreV2.MBBTCmdReqDispatcher
import com.xr.common.middleware.manager.coreV2.MBBTCmdRspDispatcher
import com.xr.common.middleware.manager.coreV2.MBBTConnectDispatcher
import com.xr.common.middleware.manager.coreV2.MBBleCmdRspDispatcher
import com.xr.common.middleware.manager.coreV2.MBBleConnectDispatcher
import com.xr.common.middleware.manager.coreV2.MBCameraDispatcher
import com.xr.common.middleware.manager.coreV2.MBCmdBTDispatcher
import com.xr.common.middleware.manager.coreV2.MBCommDataDispatcher
import com.xr.common.middleware.manager.coreV2.MBMediaStreamDispatcher
import com.xr.common.middleware.manager.coreV2.MBBleCmdReqDispatcher

class MBDeviceManager {
    private var mContext: Context? = null
    private var mbDevice: MBDevice? = null
    private var mbPlatform: MBPlatform? = null
    private val bleConnectDispatcher by lazy {
        MBBleConnectDispatcher()
    }
    private val readCameraListener by lazy {
        MBCameraDispatcher()
    }

    private val readCommDataListener by lazy {
        MBCommDataDispatcher()
    }

    private val btConnectDispatcher by lazy {
        MBBTConnectDispatcher()
    }

    private val bleCmdReqDispatcher by lazy {
        MBBleCmdReqDispatcher()
    }

    private val bleCmdRspDispatcher by lazy {
        MBBleCmdRspDispatcher()
    }

    private val btCmdReqDispatcher by lazy {
        MBBTCmdReqDispatcher()
    }

    private val btCmdRspDispatcher by lazy {
        MBBTCmdRspDispatcher()
    }

    private val mbMediaStreamListener by lazy {
        MBMediaStreamDispatcher()
    }

    private val mbCmdBTDispatcher by lazy {
        MBCmdBTDispatcher()
    }

    companion object {
        private val mbDeviceManager: MBDeviceManager by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            MBDeviceManager()
        }

        fun getInstance(): MBDeviceManager {
            return mbDeviceManager
        }

    }

    fun setContext(context: Context) {
        this.mContext = context
    }

    fun buildDevice(
        name: String,
        address: String,
        mbConnectivity: MBConnectivity,
        mbProductType: Short,
        mbProtocolVersion: MBProtocol,
        companyType: MBCompany
    ) {
        if (mbPlatform != null && this.mbDevice?.btAddress == address) {
            return
        }
        mbDevice = MBDevice(name, address, address, mbConnectivity, mbProductType).apply {
            deviceIdentity = address
            this.mbProtocolVersion = mbProtocolVersion
            this.companyType = companyType
        }
        initConnect(mbDevice!!)
    }

    fun buildDevice(mbDevice: MBDevice) {
        if (mbPlatform != null && this.mbDevice?.deviceIdentity == mbDevice.deviceIdentity) {
            return
        }
        initConnect(mbDevice)
    }

    private fun initConnect(mbDevice: MBDevice) {
        mbPlatform?.disconnect()

        this.mbDevice = mbDevice
        mbPlatform = MBPlatform(mContext, mbDevice)
        mbPlatform?.run {
            Log.d(
                "MBDeviceManager", "buildDevice: ${mbDevice.mbConnectivity == MBConnectivity.BLE}"
            )
            if (mbDevice.mbConnectivity == MBConnectivity.BLE) {
                bleManager?.addConnectListener(bleConnectDispatcher)
                bleManager?.addCmdReqListener(bleCmdReqDispatcher)
                bleManager?.addCmdRspListener(bleCmdRspDispatcher)
            } else {
                btManager?.addConnectListener(btConnectDispatcher)
                btManager?.addCmdReqListener(btCmdReqDispatcher)
                btManager?.addCmdRspListener(btCmdRspDispatcher)
                btManager?.setCommDataListener(readCommDataListener)
                btManager?.setCameraListener(readCameraListener)
                btManager?.setMediaStreamListener(mbMediaStreamListener)
                btCmdReqDispatcher.addListener(mbCmdBTDispatcher)
                btCmdRspDispatcher.addListener(mbCmdBTDispatcher)
            }
        }
    }

    fun getBuildDevice(): MBDevice? {
        return mbDevice
    }


    fun setParams(param: ConnectParam) {
        mbPlatform?.setParam(param)
    }

    fun updateKey(key: Int) {
        mbPlatform?.btManager?.updateKey(key)
    }


    fun connectBle() {
        mbPlatform?.connectBle()
    }


    fun connectBT() {
        mbPlatform?.connectBT()
    }

    fun destroy() {
        btCmdReqDispatcher.removeListener(mbCmdBTDispatcher)
        btCmdRspDispatcher.removeListener(mbCmdBTDispatcher)
        mbPlatform?.btManager?.setCameraListener(null)
        mbPlatform?.btManager?.setMediaStreamListener(null)
        mbPlatform?.btManager?.setCommDataListener(null)
        mbPlatform?.disconnect()
        if (mbPlatform != null) {
            btConnectDispatcher.onMetaGlassBtConnectStateChange(
                null, MetaGlassState(MetaGlassState.State.DISCONNECT, MetaGlassState.Error.NO_ERROR)
            )
            bleConnectDispatcher.onMetaGlassConnectStateChange(
                null, MetaGlassState(MetaGlassState.State.DISCONNECT, MetaGlassState.Error.NO_ERROR)
            )
        }
        mbPlatform = null
        mbDevice = null
    }

    fun isConnected(): Boolean {
        return if (mbDevice?.mbConnectivity == MBConnectivity.BLE) {
            mbPlatform?.bleManager?.metaGlassState?.isConnected ?: false
        } else {
            mbPlatform?.btManager?.metaGlassState?.isConnected ?: false
        }

    }

    fun getMetaGlassState(): MetaGlassState {
        return mbPlatform?.btManager?.metaGlassState
            ?: MetaGlassState(MetaGlassState.State.DISCONNECT, MetaGlassState.Error.NO_ERROR)
    }

    fun addBleConnectListener(listener: MetaGlassConnectListener) {
        bleConnectDispatcher.addListener(listener)
    }

    fun removeConnectListener(listener: MetaGlassConnectListener) {
        bleConnectDispatcher.removeListener(listener)
    }

    fun addBTConnectListener(listener: MetaGlassBtConnectListener) {
        btConnectDispatcher.addListener(listener)
    }

    fun removeBTConnectListener(listener: MetaGlassBtConnectListener) {
        btConnectDispatcher.removeListener(listener)
    }

    fun addBleCmdReqListener(listener: MetaGlassCmdReqMsgListener) {
        bleCmdReqDispatcher.addListener(listener)
    }

    fun removeBleCmdReqListener(listener: MetaGlassCmdReqMsgListener) {
        bleCmdReqDispatcher.removeListener(listener)
    }

    fun addBleCmdRspListener(listener: MetaGlassCmdRspMsgListener) {
        bleCmdRspDispatcher.addListener(listener)
    }

    fun removeBleCmdRspListener(listener: MetaGlassCmdRspMsgListener) {
        bleCmdRspDispatcher.removeListener(listener)
    }

    fun addBTCmdReqListener(listener: MBCmdV2ReqMsgListener) {
        return btCmdReqDispatcher.addListener(listener)
    }

    fun removeBTCmdReqListener(listener: MBCmdV2ReqMsgListener) {
        return btCmdReqDispatcher.removeListener(listener)
    }

    fun addBTCmdRspListener(listener: MBCmdV2RspMsgListener) {
        return btCmdRspDispatcher.addListener(listener)
    }

    fun removeBTCmdRspListener(listener: MBCmdV2RspMsgListener) {
        return btCmdRspDispatcher.removeListener(listener)
    }


    fun sendCmd(cmd: MetaGlassCmd<*>) {
        mbPlatform?.sendCmd(cmd)
    }

    fun getBTManager(): BTManager? {
        return mbPlatform?.btManager
    }

    fun sendCommData(comm: CommDataV2) {
        mbPlatform?.btManager?.sendCommData(comm)
    }

    fun getDeviceIdentity(): String {
        return mbDevice?.deviceIdentity ?: ""
    }

    fun getDeviceToken(): Int {
        return mbPlatform?.btManager?.deviceToken ?: 0
    }

    fun unpair() {
        mbPlatform?.btManager?.unpair()
    }


    fun setCameraListener(listener: MBV2ReadCameraListener) {
        readCameraListener.addListener(listener)
    }

    fun removeCameraListener(listener: MBV2ReadCameraListener) {
        readCameraListener.removeListener(listener)
    }

    fun setCommDataListener(listener: MBV2ReadCommDataListener) {
        readCommDataListener.addListener(listener)
    }

    fun removeCommDataListener(listener: MBV2ReadCommDataListener) {
        readCommDataListener.removeListener(listener)
    }

    fun setMediaStreamListener(listener: MBMediaStreamListener?) {
        if (listener != null) {
            mbMediaStreamListener.addListener(listener)
        }
    }

    fun removeMediaStreamListener(listener: MBMediaStreamListener?) {
        if (listener != null) {
            mbMediaStreamListener.removeListener(listener)
        }
    }

    fun getBTCmdDispatcher(): MBCmdBTDispatcher {
        return mbCmdBTDispatcher
    }

    fun updateMTU(mtu: Short) {
        mbPlatform?.btManager?.updateMTU(mtu)
    }
}