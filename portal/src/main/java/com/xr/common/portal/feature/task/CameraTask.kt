package com.xr.common.portal.feature.task

import android.os.Environment
import android.util.Log
import com.jeremyliao.liveeventbus.LiveEventBus
import com.metabounds.libglass.bluetooth.MetaGlassBtConnectListener
import com.metabounds.libglass.bluetooth.MetaGlassState
import com.metabounds.libglass.bluetooth.protocol.basic.BizType
import com.metabounds.libglass.bluetooth.protocol.basic.CmdError
import com.metabounds.libglass.v2.bean.CommDataHeadBean
import com.metabounds.libglass.v2.bean.camera.CameraHeadBean
import com.metabounds.libglass.v2.interfaces.MBCmdV2ReqMsgListener
import com.metabounds.libglass.v2.interfaces.MBCmdV2RspMsgListener
import com.metabounds.libglass.v2.interfaces.MBMediaStreamListener
import com.metabounds.libglass.v2.interfaces.MBV2ReadCameraListener
import com.metabounds.libglass.v2.mbEnum.MBCmdV2Type
import com.metabounds.libglass.v2.mbEnum.MBColorSpaceRGB
import com.metabounds.libglass.v2.mbEnum.MBImageFormat
import com.metabounds.libglass.v2.req.CmdV2CameraModuleReq
import com.metabounds.libglass.v2.req.CmdV2CameraVideoActionReq
import com.metabounds.libglass.v2.req.CmdV2Req
import com.metabounds.libglass.v2.rsp.CmdV2Rsp
import com.metabounds.libglass.v2.rsp.CmdV2StateRsp
import com.xr.common.middleware.base.BaseLibData
import com.xr.common.middleware.manager.MBDeviceManager
import com.xr.common.middleware.utils.FFmpegUtils
import com.xr.common.middleware.utils.SaveUtils
import com.xr.common.middleware.utils.SpeechFormatUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class CameraTask : MBV2ReadCameraListener, MBMediaStreamListener, MBCmdV2ReqMsgListener,
    MBCmdV2RspMsgListener, MetaGlassBtConnectListener {

    private val TAG = this::class.java.simpleName
    private var cameraModule = 0;//0:拍照， 1:录像

    private var h264File: File? = null
    private var h264OutputStream: FileOutputStream? = null
    private var pcmFile: File? = null
    private var pcmOutputStream: FileOutputStream? = null

    private var aacFile: File? = null
    private var mp4File: File? = null

    private var isFinish = false
    private var cameraHeadBean: CameraHeadBean? = null
    private var isError = false

    private val jpgOutputStream: ByteArrayOutputStream by lazy {
        ByteArrayOutputStream()
    }

    constructor() {
        prepare()
    }

    private fun prepare() {
        MBDeviceManager.getInstance().setCameraListener(this)
        MBDeviceManager.getInstance().setMediaStreamListener(this)
        MBDeviceManager.getInstance().addBTCmdReqListener(this)
        MBDeviceManager.getInstance().addBTConnectListener(this)
        MBDeviceManager.getInstance().addBTCmdRspListener(this)
    }

    fun release() {
        MBDeviceManager.getInstance().removeBTCmdReqListener(this)
        MBDeviceManager.getInstance().removeBTCmdRspListener(this)
        MBDeviceManager.getInstance().removeBTConnectListener(this)
        MBDeviceManager.getInstance().removeCameraListener(this)
        MBDeviceManager.getInstance().removeMediaStreamListener(this)

        jpgOutputStream.reset()
        jpgOutputStream.close()
        h264OutputStream?.close()
        pcmOutputStream?.close()
    }

    /**
     * 拍照流
     */
    override fun prepare(p0: CommDataHeadBean?, p1: CameraHeadBean?) {
        isError = false
        cameraHeadBean = p1

        if (cameraHeadBean?.imageFormat == MBImageFormat.IMAGE_FORMAT_JPEG) {//&& cameraHeadBean?.mbColorSpaceRGB == MBColorSpaceRGB.COLOR_SPACE_YCBCR
            //直接创建文件保存
            jpgOutputStream.reset()
        } else if (cameraHeadBean?.imageFormat == MBImageFormat.IMAGE_FORMAT_RAW && cameraHeadBean?.mbColorSpaceRGB == MBColorSpaceRGB.COLOR_SPACE_YUV) {
            //YUV422(YUYV)
        } else if (cameraHeadBean?.imageFormat == MBImageFormat.IMAGE_FORMAT_H264) {
        }
    }

    override fun recvData(index: Int, byteArray: ByteArray?) {
        if (cameraHeadBean?.imageFormat == MBImageFormat.IMAGE_FORMAT_JPEG) {
            jpgOutputStream.write(byteArray)
        } else if (cameraHeadBean?.imageFormat == MBImageFormat.IMAGE_FORMAT_RAW && cameraHeadBean?.mbColorSpaceRGB == MBColorSpaceRGB.COLOR_SPACE_YUV) {

        } else if (cameraHeadBean?.imageFormat == MBImageFormat.IMAGE_FORMAT_H264) {
            h264OutputStream?.write(byteArray)
        }
    }

    override fun finish(index: Int, byteArray: ByteArray?) {
        Log.i(TAG, "finish imageFormat:${cameraHeadBean?.imageFormat}")
        if (cameraHeadBean?.imageFormat == MBImageFormat.IMAGE_FORMAT_JPEG) {
            jpgOutputStream.write(byteArray)
            val isSuccess =
                SaveUtils.saveBytesToAlbum(BaseLibData.application, jpgOutputStream.toByteArray())
            Log.i(TAG, "Shot isSuccess:$isSuccess")
            jpgOutputStream.reset()
        } else if (cameraHeadBean?.imageFormat == MBImageFormat.IMAGE_FORMAT_RAW && cameraHeadBean?.mbColorSpaceRGB == MBColorSpaceRGB.COLOR_SPACE_YUV) {
        } else if (cameraHeadBean?.imageFormat == MBImageFormat.IMAGE_FORMAT_H264) {
            h264OutputStream?.write(byteArray)
        }
    }

    override fun error(p0: Int, p1: Int) {
        Log.i(TAG, "error")
    }

    /**
     * 音频流
     */
    override fun onStream(
        address: String?,
        bizType: BizType?,
        cmdType: Int,
        sourceId: Short,
        dst: Short,
        packType: Byte,
        index: Int,
        data: ByteArray?
    ) {
        data?.let {
            SpeechFormatUtils.unpackWQ(it).forEach { pcm ->
                pcmOutputStream?.write(pcm)
            }
        }
    }

    override fun onMetaGlassCmdReqMsg(p0: String?, p1: CmdV2Req<*>?) {
        Log.i(TAG, "onMetaGlassCmdReqMsg:${p1?.bizType};${p1?.cmdV2Type}")
        when (p1?.bizType) {
            BizType.BIZ_CAMERA_GROUP -> doCameraReq(p1)
            else -> {}
        }
    }

    override fun onMetaGlassCmdRspMsg(p0: String?, p1: CmdV2Rsp<*>?) {}

    override fun onMetaGlassBtConnectStateChange(p0: String?, p1: MetaGlassState?) {}

    private fun doCameraReq(cmdReq: CmdV2Req<*>) {
        when (cmdReq.cmdV2Type) {
            MBCmdV2Type.CMD_CAMERA_FUN_SWITCH.toInt() -> {
                MBDeviceManager.getInstance().sendCmd(
                    CmdV2StateRsp(
                        BizType.BIZ_CAMERA_GROUP,
                        MBCmdV2Type.CMD_CAMERA_FUN_SWITCH.toInt(),
                        CmdError.STATUS_SUCCESS
                    )
                )
                val module = (cmdReq as CmdV2CameraModuleReq).value
                Log.i(TAG, "CMD_CAMERA_FUN_SWITCH module:${module}")
                cameraModule = module
            }

            MBCmdV2Type.CMD_CAMERA_VIDEO_ACTION.toInt() -> {
                MBDeviceManager.getInstance().sendCmd(
                    CmdV2StateRsp(
                        BizType.BIZ_CAMERA_GROUP,
                        MBCmdV2Type.CMD_CAMERA_VIDEO_ACTION.toInt(),
                        CmdError.STATUS_SUCCESS
                    )
                )

                val action = (cmdReq as CmdV2CameraVideoActionReq).value
                Log.i(TAG, "CMD_CAMERA_VIDEO_ACTION action:${action}")
                if (action == 1) {
                    isFinish = false
                    //创建视频文件
                    val h264Dir =
                        BaseLibData.application.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
                    h264File = File(h264Dir, "${System.currentTimeMillis()}.h264")
                    if (!h264File!!.exists()) {
                        h264File!!.createNewFile()
                    }
                    h264OutputStream = h264File!!.outputStream()

                    //创建音频文件
                    val pcmDir =
                        BaseLibData.application.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                    pcmFile = File(pcmDir, "${System.currentTimeMillis()}.pcm")
                    if (!pcmFile!!.exists()) {
                        pcmFile!!.createNewFile()
                    }
                    pcmOutputStream = pcmFile!!.outputStream()

                } else {
                    if (isFinish) return
                    isFinish = true

                    h264OutputStream?.flush()
                    h264OutputStream?.close()
                    h264OutputStream = null

                    pcmOutputStream?.flush()
                    pcmOutputStream?.close()
                    pcmOutputStream = null

                    muxerMp4()
                }
            }
        }
    }

    private fun muxerMp4() {
        //pcm转aac
        val aacDir = BaseLibData.application.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        aacFile = File(aacDir, "${System.currentTimeMillis()}.aac")
        val aacCode = FFmpegUtils.getInstance().pcm2aac(pcmFile!!.path, aacFile!!.path)
        Log.d(TAG, "MuxerMp4 aacCode=$aacCode")

        if (aacCode == 0) {
            val mp4Dir = BaseLibData.application.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
            mp4File = File(mp4Dir, "${System.currentTimeMillis()}.mp4")
            val mp4Code = FFmpegUtils.getInstance()
                .h264Aac2mp4(h264File!!.path, aacFile!!.path, mp4File!!.path)
            Log.d(TAG, "MuxerMp4 mp4Code=$mp4Code")

            if (mp4Code == 0) {
                var isSuccess = SaveUtils.saveVideoToAlbum(BaseLibData.application, mp4File!!.path)
                Log.d(TAG, "Video isSuccess:$isSuccess")
            }
        }
    }

}