package com.xr.common.middleware.speech

interface MetaTTSCallback {
    fun onStart()
    fun onCompletion()
    fun onSpeakProgress(position: Int, duration: Int)
    fun onError()
    fun onTotalDuration(duration: Int)
}