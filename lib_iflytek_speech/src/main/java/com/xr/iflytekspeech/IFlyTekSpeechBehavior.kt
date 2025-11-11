package com.xr.iflytekspeech

import com.xr.iflytekspeech.stt.IFlytekSTTManager
import com.xr.iflytekspeech.tts.IFlytekTTSManager
import com.xr.common.middleware.speech.MetaSTTCallback
import com.xr.common.middleware.speech.MetaSpeechBehavior
import com.xr.common.middleware.speech.MetaTTSCallback
import com.xr.common.middleware.speech.MetaTranslateCallback

class IFlyTekSpeechBehavior : MetaSpeechBehavior {

    override fun setSttLanguageCode(language: String) {
        IFlytekSTTManager.getInstance().setLanguageCode(language)
    }

    override fun currSttLanguageCode(): String {
        return IFlytekSTTManager.getInstance().getLanguageCode()
    }

    override fun startAudioRecorder() {
        IFlytekSTTManager.getInstance().startAudioRecorder()
    }

    override fun stopAudioRecorder() {
        IFlytekSTTManager.getInstance().stopAudioRecorder()
    }

    override fun sendAudioStream(data: ByteArray) {
        IFlytekSTTManager.getInstance().sendAudioStream(data)
    }

    override fun stopAudioStream() {
        IFlytekSTTManager.getInstance().stopAudioStream()
    }

    override fun releaseSTT() {
        IFlytekSTTManager.getInstance().releaseSTT()
    }

    override fun addSTTCallback(metaSTTCallback: MetaSTTCallback) {
        IFlytekSTTManager.getInstance().addMetaSTTCallback(metaSTTCallback)
    }

    override fun removeSTTCallback(metaSTTCallback: MetaSTTCallback) {
        IFlytekSTTManager.getInstance().removeMetaSTTCallback(metaSTTCallback)
    }

    override fun startTTS(content: String, language: String) {
        IFlytekTTSManager.getInstance().startTTS(content, language)
    }

    override fun stopTTS() {
        IFlytekTTSManager.getInstance().stopTTS()
    }

    override fun releaseTTS() {
        IFlytekTTSManager.getInstance().release()
    }

    override fun addTTSCallback(metaTTSCallback: MetaTTSCallback) {
        IFlytekTTSManager.getInstance().addMetaTTSCallback(metaTTSCallback)
    }

    override fun removeTTSCallback(metaTTSCallback: MetaTTSCallback) {
        IFlytekTTSManager.getInstance().removeMetaTTSCallback(metaTTSCallback)
    }

    override fun translate(msgId: Byte, isFinal: Boolean, content: String, language: String) {

    }


    override fun addTranslateCallback(metaTranslateCallback: MetaTranslateCallback) {

    }

    override fun removeTranslateCallback(metaTranslateCallback: MetaTranslateCallback) {

    }

}