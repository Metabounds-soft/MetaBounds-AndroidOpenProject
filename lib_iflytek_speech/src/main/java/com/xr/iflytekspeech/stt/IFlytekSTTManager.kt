package com.xr.iflytekspeech.stt

import android.os.Bundle
import android.util.Log
import com.iflytek.cloud.InitListener
import com.iflytek.cloud.RecognizerListener
import com.iflytek.cloud.RecognizerResult
import com.iflytek.cloud.SpeechConstant
import com.iflytek.cloud.SpeechError
import com.iflytek.cloud.SpeechRecognizer
import com.xr.iflytekspeech.utils.IFlytekSpeechUtils
import com.xr.common.middleware.base.BaseLibData
import com.xr.common.middleware.speech.MetaSTTCallback
import com.xr.iflytekspeech.utils.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.util.LinkedList

class IFlytekSTTManager : RecognizerListener, InitListener {

    private val TAG = "IFlytekSTTManager"
    private var callbacks = LinkedList<MetaSTTCallback>()

    // 用HashMap存储听写结果
    private val mIatResults: HashMap<String, String> = LinkedHashMap()

    //    private var isAsrLast = false
    //语音识别对象
    private var mIat: SpeechRecognizer? = null
    private var languageCode = ""
    private var vadBos: Int = 5000
    private var vadEos: Int = 1000

    companion object {
        private val mInstance by lazy {
            IFlytekSTTManager()
        }

        fun getInstance(): IFlytekSTTManager {
            return mInstance
        }
    }

    init {
        mIat = SpeechRecognizer.createRecognizer(BaseLibData.application, this)
        mIat?.run {
            //设置语法ID和 SUBJECT 为空，以免因之前有语法调用而设置了此参数；或直接清空所有参数，具体可参考 DEMO 的示例。
            setParameter(SpeechConstant.CLOUD_GRAMMAR, null)
            setParameter(SpeechConstant.SUBJECT, null)
            //设置返回结果格式，目前支持json,xml以及plain 三种格式，其中plain为纯听写文本内容
            setParameter(SpeechConstant.RESULT_TYPE, "json")
            //此处engineType为“cloud”
            setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD)
            setParameter(SpeechConstant.ENGINE_MODE, "auto")
            //设置语音输入语言，zh_cn为简体中文
            setParameter(
                SpeechConstant.LANGUAGE, IFlytekSpeechUtils.getSpeechLanguageStr(languageCode)
            )
            //设置结果返回语言
            setParameter(
                SpeechConstant.ACCENT, IFlytekSpeechUtils.getSpeechLanguageStr(languageCode)
            )
            setParameter(SpeechConstant.VAD_ENABLE, "1")
            setParameter("dwa", "wpgs")
            //设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
            setParameter(SpeechConstant.ASR_PTT, "1")
            setParameter(SpeechConstant.VAD_BOS, vadBos.toString())
            //设置语音后端点:后端点静音检测时间，单位ms，即用户停止说话多长时间内即认为不再输入，
            //自动停止录音，范围{0~10000}
            setParameter(SpeechConstant.VAD_EOS, vadEos.toString())
            setParameter(SpeechConstant.DOMAIN, "iat")
        }
    }

    fun setLanguageCode(language: String) {
        languageCode = language

        Log.i(TAG, " setLanguageCode:$language")
        mIat?.run {
            //设置语音输入语言，zh_cn为简体中文
            setParameter(
                SpeechConstant.LANGUAGE, IFlytekSpeechUtils.getSpeechLanguageStr(languageCode)
            )
            //设置结果返回语言
            setParameter(
                SpeechConstant.ACCENT, IFlytekSpeechUtils.getSpeechLanguageStr(languageCode)
            )
        }
    }

    fun getLanguageCode(): String {
        return languageCode
    }

    fun startAudioRecorder() {
        Log.i(TAG, " startAudioRecorder ${mIat?.isListening}")
        cancel()
        mIat?.setParameter(SpeechConstant.AUDIO_SOURCE, "0")

        MainScope().launch(Dispatchers.IO) {
            delay(500)
            Log.i(TAG, " startListening ${mIat?.isListening}")
            mIat?.startListening(this@IFlytekSTTManager)
        }
    }

    fun stopAudioRecorder() {
        //开启VAD识别后，讯飞会自动判断结束
//        mIat?.stopListening()
    }

    fun sendAudioStream(data: ByteArray) {
//        logD("$TAG sendAudioStream ${mIat?.isListening}")
        mIat?.setParameter(SpeechConstant.AUDIO_SOURCE, "-1")
        if (mIat?.isListening != true) {
            Log.i(TAG, " sendAudioStream startListening")
            mIat?.startListening(this)
        }

        mIat?.writeAudio(data, 0, data.size)
    }

    fun stopAudioStream() {
        mIat?.stopListening()
    }

    private fun cancel() {
        mIatResults.clear()
        if (mIat?.isListening == true) {
            mIat?.cancel()
        }
    }

    fun releaseSTT() {
        mIatResults.clear()
        if (mIat?.isListening == true) {
            mIat?.cancel()
            mIat?.destroy()
        }

        callbacks.clear()
    }

    fun addMetaSTTCallback(listener: MetaSTTCallback) {
        synchronized(this) {
            if (!callbacks.contains(listener)) {
                callbacks.add(listener)
            }
        }
    }


    fun removeMetaSTTCallback(listener: MetaSTTCallback) {
        synchronized(this) {
            if (callbacks.contains(listener)) {
                callbacks.remove(listener)
            }
            Log.i(TAG, "removeMetaSTTCallback size:${callbacks.size}")
        }
    }

    override fun onVolumeChanged(p0: Int, p1: ByteArray?) {
    }

    override fun onBeginOfSpeech() {
        Log.i(TAG, " onBeginOfSpeech")
        callbacks.forEach { it.onStart() }
    }

    override fun onEndOfSpeech() {
        Log.i(TAG, " onEndOfSpeech")
        callbacks.forEach { it.onComplete() }
    }

    override fun onResult(result: RecognizerResult?, isLast: Boolean) {
        if (result == null && isLast) {
            callbacks.forEach { it.onResponse("", isLast) }
            return
        }
        val text: String = JsonParser.parseIatResult(result?.resultString)

        var sn: String? = null
        var pgs: String? = null
        var rg: String? = null
        // 读取json结果中的sn字段
        try {
            val resultJson = JSONObject(result?.resultString)
            sn = resultJson.optString("sn")
            pgs = resultJson.optString("pgs")
            rg = resultJson.optString("rg")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        //如果pgs是rpl就在已有的结果中删除掉要覆盖的sn部分
        if (pgs == "rpl") {
            val strings = rg!!.replace("[", "").replace("]", "").split(",".toRegex())
                .dropLastWhile { it.isEmpty() }.toTypedArray()
            val begin = strings[0].toInt()
            val end = strings[1].toInt()
            for (i in begin..end) {
                mIatResults.remove(i.toString() + "")
            }
        }

        mIatResults[sn!!] = text
        val resultBuffer = StringBuffer()
        for (key in mIatResults.keys) {
            resultBuffer.append(mIatResults[key])
        }

        Log.i(TAG, " onResult:$isLast, $resultBuffer")
        callbacks.forEach {
            it.onResponse(resultBuffer.toString(), isLast)
        }
        if (isLast) {
            mIatResults.clear()
        }
    }

    override fun onError(p0: SpeechError?) {
        Log.i(TAG, " onError:${p0?.errorCode},${p0?.errorDescription}")
        callbacks.forEach {
            p0?.message?.let { msg -> it.onError(msg) }
        }
        mIatResults.clear()
    }

    override fun onEvent(eventType: Int, p1: Int, p2: Int, p3: Bundle?) {
//        logD(TAG, "eventType：${eventType};p1:$p1;p2:$p2")
    }

    override fun onInit(p0: Int) {
        Log.i(TAG, "onInit:$p0")
    }
}