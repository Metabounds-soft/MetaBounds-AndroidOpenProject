package com.xr.common.middleware.utils

import com.metabounds.libglass.bluetooth.protocol.basic.MetaLanguage
import com.xr.common.middleware.model.bean.InterfaceLanguage
import com.xr.common.middleware.model.bean.StandardLanguage

/**
 * Description:
 * CreateDate:     2025/10/17 13:50
 * Author:         agg
 */
object LanguageUtil {
    fun getInterfaceLanguage(language: String): String {
        return when (language) {
            StandardLanguage.CHINESE.code -> InterfaceLanguage.LANG_CHINESE.code
            StandardLanguage.ENGLISH.code -> InterfaceLanguage.LANG_ENGLISH.code
            else -> InterfaceLanguage.LANG_ENGLISH.code
        }
    }

    fun getMetaLanguage(standardLanguage: String): MetaLanguage {
        return when (standardLanguage) {
            StandardLanguage.CHINESE.code -> MetaLanguage.META_LANGUAGE_CN
            StandardLanguage.ENGLISH.code -> MetaLanguage.META_LANGUAGE_EN
            else -> MetaLanguage.META_LANGUAGE_EN
        }
    }
}