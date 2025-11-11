package com.xr.common.portal.feature.task

import android.util.Log
import com.metabounds.libglass.bluetooth.MetaGlassBtConnectListener
import com.metabounds.libglass.bluetooth.MetaGlassState
import com.metabounds.libglass.bluetooth.protocol.basic.BizType
import com.metabounds.libglass.bluetooth.protocol.basic.CmdError
import com.metabounds.libglass.v2.interfaces.MBCmdV2ReqMsgListener
import com.metabounds.libglass.v2.interfaces.MBCmdV2RspMsgListener
import com.metabounds.libglass.v2.mbEnum.MBCmdV2Type
import com.metabounds.libglass.v2.mbEnum.MBWIFIType
import com.metabounds.libglass.v2.req.CmdV2CameraActionReq
import com.metabounds.libglass.v2.req.CmdV2Req
import com.metabounds.libglass.v2.req.CmdV2SimulActionReq
import com.metabounds.libglass.v2.req.CmdV2WifiInfoReq
import com.metabounds.libglass.v2.req.CmdV2WifiONReq
import com.metabounds.libglass.v2.rsp.CmdV2Rsp
import com.metabounds.libglass.v2.rsp.CmdV2StateRsp
import com.metabounds.libglass.v2.rsp.CmdV2WifiInfoRsp
import com.xr.common.middleware.easynet.manager.EasyNetManager
import com.xr.common.middleware.manager.MBDeviceManager

class MozartTaskManager : MBCmdV2ReqMsgListener, MBCmdV2RspMsgListener, MetaGlassBtConnectListener {

    private var cameraTask: CameraTask? = null
    private var simultaneousTask: SimultaneousTask? = null

    companion object {
        private val TAG = MozartTaskManager::class.java.simpleName
        private val mInstance by lazy { MozartTaskManager() }
        fun getInstance(): MozartTaskManager = mInstance
    }

    fun startTask() {
        Log.i(TAG, "startTask: ")
        MBDeviceManager.getInstance().addBTConnectListener(this)
        MBDeviceManager.getInstance().addBTCmdReqListener(this)
        MBDeviceManager.getInstance().addBTCmdRspListener(this)
    }

    fun destroy() {
        Log.i(TAG, "destroy: ")
        MBDeviceManager.getInstance().removeBTCmdReqListener(this)
        MBDeviceManager.getInstance().removeBTConnectListener(this)
        MBDeviceManager.getInstance().removeBTCmdRspListener(this)
        simultaneousTask?.release()
        simultaneousTask = null
    }

    override fun onMetaGlassCmdReqMsg(p0: String?, p1: CmdV2Req<*>) {
        Log.i(TAG, "onMetaGlassCmdReqMsg:${p1.bizType};${p1.cmdV2Type}")
        when (p1.bizType) {
            BizType.BIZ_CAMERA_GROUP -> doCameraAction(p1)
            BizType.BIZ_SI_GROUP -> doSimultaneousAction(p1)
            else -> {}
        }
    }

    override fun onMetaGlassCmdRspMsg(p0: String?, p1: CmdV2Rsp<*>) {
        when (p1.bizType) {
            BizType.CMD_CONNECT -> doWifiAction(p1)
            else -> {}
        }
    }

    override fun onMetaGlassBtConnectStateChange(p0: String?, p1: MetaGlassState) {
        if (p1.isConnected) {
            //如果P2P没有连接过就进行连接
            if (EasyNetManager.getInstance().isAttemptToConnect()) {
                MBDeviceManager.getInstance().sendCmd(
                    CmdV2WifiONReq(
                        BizType.CMD_CONNECT, 34, null, MBWIFIType.get(2), 1, 0, "", ""
                    )
                )
            }
        }
    }

    private fun doWifiAction(cmd: CmdV2Rsp<*>) {
        if (cmd.bizType == BizType.CMD_CONNECT && cmd.cmdV2Type == MBCmdV2Type.CMD_CONNECTION_WIFI_ON.toInt()) {
            Log.i(TAG, "CMD_CONNECTION_WIFI_ON value = ${(cmd as CmdV2StateRsp).value}")
            if (cmd.value) {
                MBDeviceManager.getInstance().sendCmd(CmdV2WifiInfoReq())
            } else {
                Log.e(TAG, "CMD_CONNECTION_WIFI_ON FAILURE")
            }
        } else if (cmd.bizType == BizType.CMD_CONNECT && cmd.cmdV2Type == MBCmdV2Type.CMD_CONNECTION_WIFI_OFF.toInt()) {
            Log.i(TAG, "GlassConnect CmdRspMsg CMD_CONNECTION_WIFI_OFF")
        } else if (cmd.bizType == BizType.CMD_CONNECT && cmd.cmdV2Type == MBCmdV2Type.CMD_CONNECTION_WIFI_INFO.toInt()) {
            val wifiInfo = (cmd as CmdV2WifiInfoRsp).value
            if (wifiInfo == null) {
                Log.e(TAG, "wifiInfo == null")
            } else {
                Log.i(
                    TAG,
                    "GlassConnect CmdRspMsg CMD_CONNECTION_WIFI_INFO ${wifiInfo.mac} ${wifiInfo.ip} ${wifiInfo.port}"
                )
                EasyNetManager.getInstance()
                    .openP2pConnect(wifiInfo.mac, wifiInfo.ip, wifiInfo.port)
            }
        }
    }

    private fun doCameraAction(cmdReq: CmdV2Req<*>) {
        when (cmdReq.cmdV2Type) {
            MBCmdV2Type.CMD_CAMERA_ACTION.toInt() -> {
                MBDeviceManager.getInstance().sendCmd(
                    CmdV2StateRsp(
                        BizType.BIZ_CAMERA_GROUP,
                        MBCmdV2Type.CMD_CAMERA_ACTION.toInt(),
                        CmdError.STATUS_SUCCESS
                    )
                )
                val action = (cmdReq as CmdV2CameraActionReq).value
                Log.i(TAG, "CmdV2CameraActionReq action:${action}")
                if (action == 1) {
                    cameraTask?.release()
                    cameraTask = CameraTask()
                } else {
                    cameraTask?.release()
                    cameraTask = null
                }
            }
        }
    }

    private fun doSimultaneousAction(cmdReq: CmdV2Req<*>) {
        when (cmdReq.cmdV2Type) {
            MBCmdV2Type.CMD_SI_ACTION.toInt() -> {
                MBDeviceManager.getInstance().sendCmd(
                    CmdV2StateRsp(
                        BizType.BIZ_SI_GROUP,
                        MBCmdV2Type.CMD_SI_ACTION.toInt(),
                        CmdError.STATUS_SUCCESS
                    )
                )
                val action = (cmdReq as CmdV2SimulActionReq).value
                Log.i(TAG, "CMD_SI_ACTION action:${action}")
                if (action == 1) {
                    simultaneousTask?.release()
                    simultaneousTask = SimultaneousTask()
                } else if (action == 0) {
                    simultaneousTask?.release()
                    simultaneousTask = null
                }
            }
        }
    }

}