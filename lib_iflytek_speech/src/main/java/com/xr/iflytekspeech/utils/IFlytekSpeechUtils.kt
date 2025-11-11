package com.xr.iflytekspeech.utils

import com.xr.common.middleware.model.bean.StandardLanguage

object IFlytekSpeechUtils {
    fun getSpeechLanguageStr(standardLanguage: String): String {
        return when (standardLanguage) {
            StandardLanguage.ENGLISH.code -> IFlyTekLanguage.ENGLISH.code
            StandardLanguage.CHINESE.code -> IFlyTekLanguage.CHINESE.code
            else -> IFlyTekLanguage.ENGLISH.code
        }
    }
}