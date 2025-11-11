package com.xr.common.portal.feature.translate.viewmodel

import com.xr.common.middleware.utils.LanguageUtil
import com.xr.common.middleware.viewmodel.BaseViewModel
import com.xr.common.portal.feature.translate.model.HttpTranslateManager

/**
 * Description:
 * CreateDate:     2025/10/17 11:38
 * Author:         agg
 */
class TranslateViewModel : BaseViewModel() {

    private val repository by lazy { HttpTranslateManager() }

    fun simultaneousTranslate(
        messageId: Byte,
        sourceLang: String,
        targetLang: String,
        text: String,
        isFinal: Boolean,
        action: (Byte, String, String, Boolean) -> Unit
    ) {
        launchBaseResponse({
            repository.googleTranslate(
                LanguageUtil.getInterfaceLanguage(sourceLang),
                LanguageUtil.getInterfaceLanguage(targetLang),
                text
            )
        }, {
            action.invoke(messageId, text, it?.data ?: "", isFinal)
        }, isShowProcess = false)
    }

}