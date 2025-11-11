package com.xr.iflytekspeech.tts

import android.os.Bundle
import android.util.Log
import com.iflytek.cloud.InitListener
import com.iflytek.cloud.SpeechConstant
import com.iflytek.cloud.SpeechError
import com.iflytek.cloud.SpeechSynthesizer
import com.iflytek.cloud.SynthesizerListener
import com.xr.iflytekspeech.utils.IFlytekSpeechUtils
import com.xr.common.middleware.base.BaseLibData
import com.xr.common.middleware.model.bean.StandardLanguage
import com.xr.common.middleware.speech.MetaTTSCallback
import java.util.LinkedList

class IFlytekTTSManager : SynthesizerListener, InitListener {
    private val TAG = "IFlytekTTSManager"
    private var callbacks = LinkedList<MetaTTSCallback>()
    private var mTts: SpeechSynthesizer? = null

    companion object {
        private val mInstance by lazy {
            IFlytekTTSManager()
        }

        fun getInstance(): IFlytekTTSManager {
            return mInstance
        }
    }

    init {
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(BaseLibData.application, this)
        setParam()
    }

    private fun setParam() {
        // 清空参数
        mTts?.setParameter(SpeechConstant.PARAMS, null)
        // 根据合成引擎设置相应参数
        mTts?.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD)
        // 支持实时音频返回，仅在 synthesizeToUri 条件下支持
        mTts?.setParameter(SpeechConstant.TTS_DATA_NOTIFY, "1")
        //	mTts.setParameter(SpeechConstant.TTS_BUFFER_TIME,"1");

        // 设置在线合成发音人
        mTts?.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan")
        //设置合成语速
        mTts?.setParameter(
            SpeechConstant.SPEED, "60"
        )
        //设置合成音调
        mTts?.setParameter(
            SpeechConstant.PITCH, "50"
        )
        //设置合成音量
        mTts?.setParameter(
            SpeechConstant.VOLUME, "100"
        )

        //设置播放器音频流类型
        mTts?.setParameter(
            SpeechConstant.STREAM_TYPE, "3"
        )
        // 设置播放合成音频打断音乐播放，默认为true
        mTts?.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true")

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
//        mTts?.setParameter(SpeechConstant.AUDIO_FORMAT, "pcm")
//        mTts?.setParameter(
//            SpeechConstant.TTS_AUDIO_PATH,
//            getExternalFilesDir("msc").getAbsolutePath() + "/tts.pcm"
//        )
    }

    private fun reVoicerForLanguage(language: String) {
        when (language) {
            StandardLanguage.ENGLISH.code -> setVoicer("x4_enus_ryan_assist")
            else -> setVoicer("xiaoyan")
        }
    }

    fun setVoicer(voicer: String) {
        Log.i(TAG, "setVoicer:$voicer")
        // 设置在线合成发音人
        mTts?.setParameter(SpeechConstant.VOICE_NAME, voicer)
    }

    fun setAudioFormat() {
        mTts?.setParameter(SpeechConstant.AUDIO_FORMAT, "pcm")
    }

    fun setSavePath(path: String) {
        mTts?.setParameter(SpeechConstant.TTS_AUDIO_PATH, path)
    }

    fun startTTS(content: String, language: String) {
        Log.i(TAG, "startTTS content:$content, language:$language")
        reVoicerForLanguage(language)
        mTts?.setParameter(
            SpeechConstant.LANGUAGE, IFlytekSpeechUtils.getSpeechLanguageStr(language)
        )
        mTts?.startSpeaking(content, this)
    }

    fun stopTTS() {
        Log.i(TAG, "stopTTS")
        mTts?.stopSpeaking()
    }

    fun addMetaTTSCallback(listener: MetaTTSCallback) {
        synchronized(this) {
            if (!callbacks.contains(listener)) {
                callbacks.add(listener)
            }
        }
    }


    fun removeMetaTTSCallback(listener: MetaTTSCallback) {
        synchronized(this) {
            if (callbacks.contains(listener)) {
                callbacks.remove(listener)
            }
            Log.i(TAG, "removeMetaTTSCallback size:${callbacks.size}")
        }
    }

    fun release() {
        mTts?.stopSpeaking()
        // 退出时释放连接
        mTts?.destroy()
        mTts = null

        callbacks.clear()
    }

    override fun onInit(code: Int) {
        Log.i(TAG, "IFlytekTTSManager onInit $code")
    }

    override fun onSpeakBegin() {
        Log.i(TAG, "onSpeakBegin")
        callbacks.forEach { it.onStart() }
    }

    override fun onBufferProgress(percent: Int, beginPos: Int, endPos: Int, info: String?) {
    }

    override fun onSpeakPaused() {
    }

    override fun onSpeakResumed() {
    }

    override fun onSpeakProgress(percent: Int, beginPos: Int, endPos: Int) {
//        LogUtil.e("iFlytekTTSManager onSpeakProgress-$percent, $beginPos, $endPos")
    }

    override fun onCompleted(error: SpeechError?) {
        Log.i(TAG, "onCompleted")
        callbacks.forEach { it.onCompletion() }
    }

    override fun onEvent(eventType: Int, arg1: Int, arg2: Int, obj: Bundle?) {

    }
}