package com.xr.common.middleware.utils

import android.util.Log
import com.metabounds.libglass.utils.EndianUtils
import com.theeasiestway.opus.Constants
import com.theeasiestway.opus.Opus

object SpeechFormatUtils {

    private val TAG = SpeechFormatUtils::class.java.simpleName
    private val codec: Opus = Opus()
    private var isInit = false
    var startIndex = 14//前6个字节为包头 52 03 xx xx xx xx,4位时间戳

    enum class SpeechFormat {
        PCM, OPUS, WQ_OPUS
    }

    //一帧一帧解码
    fun opus2Pcm(encoded: ByteArray, frameSize: Constants.FrameSize): ByteArray {
        val length = encoded[3].toInt()

        val resultEncodedBytes = encoded.copyOfRange(8, 8 + length)
        val decoded: ByteArray = codec.decode(resultEncodedBytes, frameSize) ?: return ByteArray(0)

        return decoded
    }

    fun getOpusCodec(): Opus {
        return codec
    }

    fun initOpusCodec() {
        codec.decoderInit(Constants.SampleRate._16000(), Constants.Channels.mono())
        isInit = true
    }

    fun releaseOpusCodec() {
        if (isInit) {
            codec.decoderRelease()

            isInit = false
        }
    }

    /** 解包物奇（OPUS） **/
    fun unpackWQ(bytes: ByteArray): ArrayList<ByteArray> {
        val list = arrayListOf<ByteArray>()

        //前6个字节为包头 52 03 xx xx xx xx
        var position = startIndex//6+4(时间戳ts)
        //2个字节 帧数
        val frameCount = EndianUtils.le2BytesToShort(bytes.copyOfRange(position, position + 2))
        position += 2

        //每一帧的格式，数据长度+数据
        for (i in 0 until frameCount) {
            val size = bytes[position].toInt()
            position++
            if (position >= position + size) {
                Log.e(
                    TAG,
                    "SpeechFormatUtils.unpackWQ size error $position > ${position + size}, bytes = ${bytes.joinToString()}  "
                )
                return arrayListOf()
            }
            val frames = bytes.copyOfRange(position, position + size)
            if (frames.size < 3) {
                Log.e(
                    TAG,
                    "SpeechFormatUtils.unpackWQ frames size error frames = ${frames.joinToString()}  "
                )
                return arrayListOf()
            }
            //opus解码pcm
            val length = frames[3].toInt()
            if (frames.size < 8 + length || 8 > 8 + length) {
                Log.e(
                    TAG,
                    "SpeechFormatUtils.unpackWQ length error frames = ${frames.joinToString()}  "
                )
                return arrayListOf()
            }
            val resultEncodedBytes = frames.copyOfRange(8, 8 + length)
            val decoded: ByteArray? = codec.decode(
                resultEncodedBytes, Constants.FrameSize._640()
            )
            if (decoded == null) {
                Log.e(TAG, "SpeechFormatUtils.unpackWQ decoded is null")
            } else {
                list.add(decoded)
            }

            position += size
        }

        return list
    }
}