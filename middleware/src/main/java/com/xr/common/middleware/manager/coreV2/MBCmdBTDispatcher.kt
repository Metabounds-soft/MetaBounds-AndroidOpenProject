package com.xr.common.middleware.manager.coreV2

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.metabounds.libglass.bluetooth.protocol.basic.BizType
import com.metabounds.libglass.bluetooth.protocol.basic.MetaLanguage
import com.metabounds.libglass.bluetooth.protocol.bean.MetaGlassBattery
import com.metabounds.libglass.bluetooth.protocol.bean.MetaGlassVersion
import com.metabounds.libglass.v2.bean.MBV2ConfigBean
import com.metabounds.libglass.v2.interfaces.MBCmdV2ReqMsgListener
import com.metabounds.libglass.v2.interfaces.MBCmdV2RspMsgListener
import com.metabounds.libglass.v2.mbEnum.MBCmdV2Type
import com.metabounds.libglass.v2.req.CmdV2BatteryReq
import com.metabounds.libglass.v2.req.CmdV2GetConfigReq
import com.metabounds.libglass.v2.req.CmdV2PushConfigReq
import com.metabounds.libglass.v2.req.CmdV2Req
import com.metabounds.libglass.v2.rsp.CmdV2BatteryRsp
import com.metabounds.libglass.v2.rsp.CmdV2GetConfigRsp
import com.metabounds.libglass.v2.rsp.CmdV2GetVersionRsp
import com.metabounds.libglass.v2.rsp.CmdV2Rsp

class MBCmdBTDispatcher : MBCmdV2RspMsgListener, MBCmdV2ReqMsgListener {
    private val TAG = "MBCmdBTDispatcher"
    val mBattery = MutableLiveData(MetaGlassBattery(0, 0))
    val mBtName = MutableLiveData("")
    val mVersion = MutableLiveData(MetaGlassVersion(0, 0, 0))
    val mHardware = MutableLiveData(0)
    val mLanguage = MutableLiveData(MetaLanguage.META_LANGUAGE_EN)


    override fun onMetaGlassCmdRspMsg(p0: String?, p1: CmdV2Rsp<*>) {
        doRsp(p1)
    }

    override fun onMetaGlassCmdReqMsg(p0: String?, p1: CmdV2Req<*>) {
        doReq(p1)
    }


    private fun doRsp(p1: CmdV2Rsp<*>) {
        when (p1.bizType) {
            BizType.BIZ_BATTERY_GROUP -> {
                if (p1.cmdV2Type == MBCmdV2Type.CMD_BATTERY_STATUS.toInt()) {
                    val result = (p1 as CmdV2BatteryRsp).value
                    Log.i(TAG, "doRsp： Battery=${result?.value}")
                    mBattery.postValue(result)
                }
            }

            BizType.BIZ_DEVICE_INFO_GROUP -> {
                if (p1.cmdV2Type == MBCmdV2Type.CMD_DEVICE_INFO_GET_VERSION.toInt()) {
                    val result = (p1 as CmdV2GetVersionRsp).value
                    mVersion.postValue(result)
                } else if (p1.cmdV2Type == MBCmdV2Type.CMD_DEVICE_INFO_PULL_CONFIG.toInt()) {
                    val result: MBV2ConfigBean? = (p1 as CmdV2GetConfigRsp).value
                    result?.let {
                        Log.d(TAG, "language:${it.language}")
                        mLanguage.postValue(it.language)
                        mHardware.postValue(it.hardware)
                    }
                }

            }

            else -> {

            }
        }
    }


    private fun doReq(p1: CmdV2Req<*>) {
        when (p1.bizType) {
            BizType.BIZ_BATTERY_GROUP -> {
                if (p1.cmdV2Type == MBCmdV2Type.CMD_BATTERY_STATUS.toInt()) {
                    val result = (p1 as CmdV2BatteryReq).value
                    Log.i(TAG, "doReq： Battery=${result?.value}")
                    mBattery.postValue(result)
                }
            }

            BizType.BIZ_DEVICE_INFO_GROUP -> {
                if (p1.cmdV2Type == MBCmdV2Type.CMD_DEVICE_INFO_PULL_CONFIG.toInt()) {
                    val result: MBV2ConfigBean? = (p1 as CmdV2GetConfigReq).value
                    result?.let {
                        Log.d(TAG, "language:${it.language}")
                        mLanguage.postValue(it.language)
                        mHardware.postValue(it.hardware)
                    }
                } else if (p1.cmdV2Type == MBCmdV2Type.CMD_DEVICE_INFO_PUSH_CONFIG.toInt()) {
                    val result: MBV2ConfigBean? = (p1 as CmdV2PushConfigReq).value
                    result?.let {
                        Log.d(TAG, "language:${it.language}")
                        mLanguage.postValue(it.language)
                        mHardware.postValue(it.hardware)
                        mVersion.postValue(it.metaGlassVersion)
                    }
                }
            }

            else -> {

            }
        }
    }
}