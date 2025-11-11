package com.xr.common.middleware.speech

interface MetaSTTCallback {
    fun onStart()
    fun onComplete()
    fun onResponse(result: String, isFinal: Boolean)
    fun onError(message: String)
}