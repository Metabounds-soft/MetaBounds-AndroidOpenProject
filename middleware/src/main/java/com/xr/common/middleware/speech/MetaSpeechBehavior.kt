package com.xr.common.middleware.speech

import java.util.Locale

interface MetaSpeechBehavior {
    fun setSttLanguageCode(language: String)
    fun currSttLanguageCode(): String
    fun startAudioRecorder()
    fun stopAudioRecorder()
    fun sendAudioStream(data: ByteArray)
    fun stopAudioStream()
    fun releaseSTT()
    fun addSTTCallback(metaSTTCallback: MetaSTTCallback)
    fun removeSTTCallback(metaSTTCallback: MetaSTTCallback)

    fun startTTS(content: String, language: String = Locale.ENGLISH.language)
    fun stopTTS()
    fun releaseTTS()
    fun addTTSCallback(metaTTSCallback: MetaTTSCallback)
    fun removeTTSCallback(metaTTSCallback: MetaTTSCallback)

    fun translate(
        msgId: Byte, isFinal: Boolean, content: String, language: String = Locale.ENGLISH.language
    )

    fun addTranslateCallback(metaTranslateCallback: MetaTranslateCallback)
    fun removeTranslateCallback(metaTranslateCallback: MetaTranslateCallback)
}