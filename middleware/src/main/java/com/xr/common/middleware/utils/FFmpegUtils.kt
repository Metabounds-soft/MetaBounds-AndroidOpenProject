package com.xr.common.middleware.utils

import android.util.Log
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.ExecuteCallback
import com.arthenica.mobileffmpeg.FFmpeg

class FFmpegUtils {
    companion object {
        private val TAG = this::class.java.simpleName
        private val mInstance by lazy {
            FFmpegUtils()
        }

        fun getInstance(): FFmpegUtils {
            return mInstance
        }
    }

    init {
        Config.enableStatisticsCallback { stats ->
            Log.i(TAG, "enableStatisticsCallback stats:$stats")
        }
    }

    fun pcm2aac(pcmPath: String, aacPath: String): Int {
        val command = "-f s16le -ar 16000 -ac 1 -i $pcmPath -c:a aac -b:a 128k $aacPath"
        val executeCode = FFmpeg.execute(command)

        return executeCode
    }

    fun h264Aac2mp4(h264Path: String, aacPath: String, mp4Path: String): Int {
        val command = "-i $h264Path -i $aacPath -c:v copy -c:a copy $mp4Path"
        val executeCode = FFmpeg.execute(command)

        return executeCode
    }

    fun h264ToMp4(inputH264: String, outputMp4: String): Int {
        val command = String.format("-i %s -c:v copy -an %s", inputH264, outputMp4)
        val executeCode = FFmpeg.execute(command)

        return executeCode
    }

    fun rotate90MP4(inputMP4: String, outputMp4: String, executeCallback: ExecuteCallback) {
        val command = String.format(
            "-i %s -vf transpose=1 -c:v libx264 -preset ultrafast -b:v 3000k -threads 8 %s",
            inputMP4,
            outputMp4
        )
        FFmpeg.executeAsync(command, executeCallback)
    }
}