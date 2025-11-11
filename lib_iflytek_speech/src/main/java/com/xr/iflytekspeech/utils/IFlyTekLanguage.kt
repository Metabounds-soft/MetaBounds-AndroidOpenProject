package com.xr.iflytekspeech.utils

/**
 * 在线听写支持多种小语种设置。支持语言类型如下：
 * <item>zh_cn</item> 中文
 * <item>en_us</item> 英文
 * <item>ja_jp</item> 日语
 * <item>ru-ru</item> 俄语
 * <item>es_es</item> 西班牙语
 * <item>fr_fr</item> 法语
 * <item>ko_kr</item> 韩语
 */
enum class IFlyTekLanguage(val code: String) {
    CHINESE("zh_cn"),       // 简体中文
    ENGLISH("en_us"),       // 英语
}