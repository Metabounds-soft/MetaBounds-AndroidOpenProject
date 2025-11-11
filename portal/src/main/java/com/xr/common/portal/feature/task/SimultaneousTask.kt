package com.xr.common.portal.feature.task

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.metabounds.libglass.bluetooth.protocol.basic.BizType
import com.metabounds.libglass.bluetooth.protocol.basic.CmdError
import com.metabounds.libglass.v2.bean.comm.CommSimulContent
import com.metabounds.libglass.v2.interfaces.MBCmdV2ReqMsgListener
import com.metabounds.libglass.v2.interfaces.MBCmdV2RspMsgListener
import com.metabounds.libglass.v2.interfaces.MBMediaStreamListener
import com.metabounds.libglass.v2.mbEnum.MBCmdV2Type
import com.metabounds.libglass.v2.req.CmdV2Req
import com.metabounds.libglass.v2.req.CmdV2SimulActionReq
import com.metabounds.libglass.v2.req.CmdV2SimulPickUpReq
import com.metabounds.libglass.v2.rsp.CmdV2Rsp
import com.metabounds.libglass.v2.rsp.CmdV2StateRsp
import com.tencent.mmkv.MMKV
import com.xr.common.middleware.manager.MBDeviceManager
import com.xr.common.middleware.model.bean.StandardLanguage
import com.xr.common.middleware.speech.MetaSTTCallback
import com.xr.common.middleware.speech.MetaTranslateCallback
import com.xr.common.portal.feature.translate.model.MetaSpeechManager
import com.xr.common.portal.feature.translate.viewmodel.TranslateViewModel

/**
 * Description:
 * CreateDate:     2025/10/17 10:22
 * Author:         agg
 */
class SimultaneousTask : MBMediaStreamListener, MBCmdV2ReqMsgListener, MBCmdV2RspMsgListener {

    companion object {
        val SIMULTANEOUS_SOURCE = "SIMULTANEOUS_SOURCE"
        val SIMULTANEOUS_TARGET = "SIMULTANEOUS_TARGET"
        val SIMULTANEOUS_SOURCE_DISPLAY = "SIMULTANEOUS_SOURCE_DISPLAY"
    }

    private val TAG = this::class.java.simpleName
    private val SIMULTANEOUS_SOURCE_DISPLAY = "SIMULTANEOUS_SOURCE_DISPLAY"
    private val MSG_STT_FINAL = 1
    private val sourceDisplay = MMKV.defaultMMKV().decodeBool(SIMULTANEOUS_SOURCE_DISPLAY, true)

    private var messageId: Byte = 0
    private var languageCodeSource: String =
        MMKV.defaultMMKV().decodeString(SIMULTANEOUS_SOURCE, StandardLanguage.ENGLISH.code)
            .toString()
    private var languageCodeTarget: String =
        MMKV.defaultMMKV().decodeString(SIMULTANEOUS_TARGET, StandardLanguage.CHINESE.code)
            .toString()
    private var isSpeeching = false
    private var isGlassBackground = false
    private var mViewModel = TranslateViewModel()

    private var mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MSG_STT_FINAL -> {
                    Log.i(TAG, "MSG_STT_FINAL")
                    if (isGlassBackground) return
                    MetaSpeechManager.getInstance().stopAudioStream()
                }
            }
        }
    }

    private val sttCallback = object : MetaSTTCallback {
        override fun onStart() {}
        override fun onComplete() {}

        override fun onResponse(result: String, isFinal: Boolean) {
            if (isGlassBackground) return

            mHandler.removeMessages(MSG_STT_FINAL)
            Log.i(TAG, "msgId:$messageId, isFinal:${isFinal}, result:${result}")
            if (isFinal) {
                mHandler.sendEmptyMessage(MSG_STT_FINAL)
                if (result.isEmpty()) {
                    nextRecord()
                }
            } else {
                if (result.isNotEmpty()) {
                    mHandler.sendEmptyMessageDelayed(MSG_STT_FINAL, 2000)
                }
            }
            if (result.isNotEmpty()) {
                languageCodeTarget = MMKV.defaultMMKV()
                    .decodeString(SIMULTANEOUS_TARGET, StandardLanguage.CHINESE.code).toString()
                translate(result, isFinal)
            }
        }

        override fun onError(message: String) {
            if (isGlassBackground) return

            Log.e(TAG, "onError:${message}")
            mHandler.sendEmptyMessage(MSG_STT_FINAL)
            nextRecord()
        }
    }

    private val translateCallback = object : MetaTranslateCallback {
        override fun onTranslated(msgId: Byte, isFinal: Boolean, text: String, trans: String) {
            if (isGlassBackground) return

            MBDeviceManager.getInstance()
                .sendCommData(CommSimulContent(msgId, if (sourceDisplay) text else "", trans))
            Log.i(TAG, "Translated: msgId:$msgId, isFinal:$isFinal, text:$text, dst:$trans")
            if (isFinal) nextRecord()
        }

        override fun onError() {
            if (isGlassBackground) return

            nextRecord()
        }
    }

    init {
        MetaSpeechManager.getInstance().addSTTCallback(sttCallback)
        MetaSpeechManager.getInstance().addTranslateCallback(translateCallback)
        MBDeviceManager.getInstance().setMediaStreamListener(this)
        MBDeviceManager.getInstance().addBTCmdReqListener(this)
        MBDeviceManager.getInstance().addBTCmdRspListener(this)

        languageCodeSource =
            MMKV.defaultMMKV().decodeString(SIMULTANEOUS_SOURCE, StandardLanguage.ENGLISH.code)
                .toString()
        languageCodeTarget =
            MMKV.defaultMMKV().decodeString(SIMULTANEOUS_TARGET, StandardLanguage.CHINESE.code)
                .toString()
        MetaSpeechManager.getInstance().setSttLanguageCode(languageCodeSource)

        isGlassBackground = false
    }

    override fun onStream(
        p0: String?,
        p1: BizType?,
        p2: Int,
        p3: Short,
        p4: Short,
        p5: Byte,
        p6: Int,
        byteArray: ByteArray?
    ) {
        Log.i(TAG, "onStream isSpeeching:$isSpeeching, isGlassBackground:$isGlassBackground")
        if (isSpeeching && !isGlassBackground) {
            byteArray?.let {
                MetaSpeechManager.getInstance().sendAudioStream(it, source = 8)
            }
        }
    }

    override fun onMetaGlassCmdReqMsg(p0: String?, p1: CmdV2Req<*>) {
        if (p1.bizType == BizType.BIZ_SI_GROUP) {
            Log.i(TAG, "onMetaGlassCmdReqMsg cmdV2Type:${p1.cmdV2Type}, value:${p1.value}")
            when (p1.cmdV2Type.toByte()) {
                MBCmdV2Type.CMD_SI_MIC_CONTROL -> {
                    MBDeviceManager.getInstance().sendCmd(
                        CmdV2StateRsp(
                            BizType.BIZ_SI_GROUP,
                            MBCmdV2Type.CMD_SI_MIC_CONTROL.toInt(),
                            CmdError.STATUS_SUCCESS
                        )
                    )

                    val status = (p1 as CmdV2SimulPickUpReq).value

                    isSpeeching = status == 1
                    Log.d(TAG, "isSpeeching:$isSpeeching")
                    if (!isSpeeching) MetaSpeechManager.getInstance().stopAudioStream()
                }

                MBCmdV2Type.CMD_SI_ACTION -> {
                    MBDeviceManager.getInstance().sendCmd(
                        CmdV2StateRsp(
                            BizType.BIZ_SI_GROUP,
                            MBCmdV2Type.CMD_SI_ACTION.toInt(),
                            CmdError.STATUS_SUCCESS
                        )
                    )
                    val action = (p1 as CmdV2SimulActionReq).value
                    Log.i(TAG, "SimultaneousAction action:${action}")
                    if (action == 2 || action == 3) {//3:(返回前台) 2:(进入后台) 1:(进入) 0:(退出)
                        isGlassBackground = action == 2
                    }
                }
            }
        }
    }

    override fun onMetaGlassCmdRspMsg(p0: String?, p1: CmdV2Rsp<*>) {
        Log.i(TAG, "onMetaGlassCmdRspMsg bizType:${p1.bizType}, cmdV2Type:${p1.cmdV2Type}")
    }

    fun release() {
        MetaSpeechManager.getInstance().stopAudioStream()
        MetaSpeechManager.getInstance().removeSTTCallback(sttCallback)
        MetaSpeechManager.getInstance().removeTranslateCallback(translateCallback)
        MBDeviceManager.getInstance().removeBTCmdReqListener(this)
        MBDeviceManager.getInstance().removeBTCmdRspListener(this)
        MBDeviceManager.getInstance().removeMediaStreamListener(this)

        messageId = 1
        isGlassBackground = false
    }

    private fun nextRecord() {
        if (messageId < 127) messageId++ else messageId = 0
    }

    private fun translate(result: String, isLast: Boolean) {
        if (isGlassBackground) return

        mViewModel.simultaneousTranslate(
            messageId, languageCodeSource, languageCodeTarget, result, isLast
        ) { msgId, text, trans, isFinal ->
            MBDeviceManager.getInstance()
                .sendCommData(CommSimulContent(msgId, if (sourceDisplay) text else "", trans))
            Log.i(TAG, "Translated: msgId:$msgId, isFinal:$isFinal, text:$text, trans:$trans")
            if (isFinal) nextRecord()
        }
    }

}