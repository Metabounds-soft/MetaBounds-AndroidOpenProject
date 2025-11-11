package com.xr.common.portal.feature.translate.model

import android.util.Log
import com.metabounds.libglass.utils.EndianUtils
import com.theeasiestway.opus.Constants
import com.xr.common.middleware.speech.MetaSTTCallback
import com.xr.common.middleware.speech.MetaSpeechBehavior
import com.xr.common.middleware.speech.MetaTTSCallback
import com.xr.common.middleware.speech.MetaTranslateCallback
import com.xr.common.middleware.utils.SpeechFormatUtils
import com.xr.common.middleware.utils.SpeechFormatUtils.SpeechFormat
import com.xr.common.middleware.utils.SpeechFormatUtils.opus2Pcm
import com.xr.iflytekspeech.IFlyTekSpeechBehavior
import java.util.Locale

/**
 * Description:
 * CreateDate:     2025/10/17 10:35
 * Author:         agg
 */
class MetaSpeechManager {

    private val TAG = this::class.java.simpleName
    private var speechBehavior: MetaSpeechBehavior = IFlyTekSpeechBehavior()
    private var printTime = 0L

    companion object {
        private val mInstance by lazy { MetaSpeechManager() }
        fun getInstance(): MetaSpeechManager = mInstance
    }

    fun setSttLanguageCode(language: String) {
        speechBehavior.setSttLanguageCode(language)
    }

    fun currSttLanguageCode(): String {
        return speechBehavior.currSttLanguageCode()
    }

    fun startAudioRecorder() {
        speechBehavior.startAudioRecorder()
    }

    fun stopAudioRecorder() {
        speechBehavior.stopAudioRecorder()
    }

    fun sendAudioStream(data: ByteArray, format: SpeechFormat = SpeechFormat.PCM, source: Int = 0) {
        val currTime = System.currentTimeMillis()
        if (currTime - printTime > 2000) {
            printTime = currTime
            Log.i(TAG, "AudioStream source:$source, format:${format.name}")
        }
        var speechFormat = format
        if (data.size > 8 && data[0] == 0x52.toByte() && data[1] == 0x03.toByte()) {
            speechFormat = SpeechFormat.WQ_OPUS
        }
        when (speechFormat) {
            SpeechFormat.PCM -> speechBehavior.sendAudioStream(data)
            SpeechFormat.OPUS -> {
                val decoded = opus2Pcm(data, Constants.FrameSize._640())
                speechBehavior.sendAudioStream(decoded)
            }

            SpeechFormat.WQ_OPUS -> {
                unpackWQSend(data)
            }
        }
    }

    fun stopAudioStream() {
        speechBehavior.stopAudioStream()
    }

    fun releaseSTT() {
        speechBehavior.releaseSTT()
        SpeechFormatUtils.releaseOpusCodec()
    }

    fun addSTTCallback(metaSTTCallback: MetaSTTCallback) {
        speechBehavior.addSTTCallback(metaSTTCallback)
    }

    fun removeSTTCallback(metaSTTCallback: MetaSTTCallback) {
        speechBehavior.removeSTTCallback(metaSTTCallback)
    }

    fun startTTS(content: String, language: String = Locale.ENGLISH.language) {
        speechBehavior.startTTS(content, language)
    }

    fun stopTTS() {
        speechBehavior.stopTTS()
    }

    fun releaseTTS() {
        speechBehavior.releaseTTS()
    }

    fun addTTSCallback(metaTTSCallback: MetaTTSCallback) {
        speechBehavior.addTTSCallback(metaTTSCallback)
    }

    fun removeTTSCallback(metaTTSCallback: MetaTTSCallback) {
        speechBehavior.removeTTSCallback(metaTTSCallback)
    }

    fun translate(
        msgId: Byte, isFinal: Boolean, content: String, language: String = Locale.ENGLISH.language
    ) {
        speechBehavior.translate(msgId, isFinal, content, language)
    }

    fun addTranslateCallback(metaTranslateCallback: MetaTranslateCallback) {
        speechBehavior.addTranslateCallback(metaTranslateCallback)
    }

    fun removeTranslateCallback(metaTranslateCallback: MetaTranslateCallback) {
        speechBehavior.removeTranslateCallback(metaTranslateCallback)
    }

    /** 解包物奇（OPUS），并发送 **/
    private fun unpackWQSend(bytes: ByteArray) {
        Log.i(TAG, "unpackWQSend: ${bytes.size}")
        //前6个字节为包头 52 03 xx xx xx xx
        //前1、2位是头，3、4位是总长，5、6是index
        var position = SpeechFormatUtils.startIndex//6+4(ts)
        //2个字节 帧数
        val frameCount = EndianUtils.le2BytesToShort(bytes.copyOfRange(position, position + 2))
        position += 2

        //每一帧的格式，数据长度+数据
        for (i in 0 until frameCount) {
            val size = bytes[position].toInt()
            position++

            val frames = bytes.copyOfRange(position, position + size)

            val decoded = opus2Pcm(frames, Constants.FrameSize._640())
            speechBehavior.sendAudioStream(decoded)
            position += size
        }
    }

}