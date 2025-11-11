package com.xr.common.middleware.speech

interface MetaTranslateCallback {
    fun onTranslated(msgId: Byte, isFinal: Boolean, text: String, trans: String)
    fun onError()
}