package com.xr.common.middleware.utils

import androidx.annotation.NonNull
import java.security.MessageDigest

/**
 *
 * @author Rubinson
 *
 * 【date】 2022/5/20
 *
 * 【name】 ParameterSignUtils
 *
 * 【function】 参数签名工具类,针对v2版本的请求参数进行加密
 *
 * 必填参数：token必传，为空的话，传入空字符串即可
 *
 * 签名规则：
 * --------------------------------------------
 *  1、过滤掉参数中，参数值为空串的参数
 *
 *  2、参数名首字母转大写
 *
 *  3、参数名按字母表排序(升序)
 *
 *  4、所有参数排序后，以key=value形式，用&连接为一个字符串
 *
 *  5、最后对第三步等到的字符串做MD5即可
 *
 * --------------------------------------------
 *
 */
class ParameterSignUtils private constructor() {

    companion object {
        private const val MD5 = "MD5"
        private const val PARAMETER_TOKEN = "Token"

        /**
         * Parameter Entry With
         * 参数键值对以什么字符串连接
         */
        private const val PARAMETER_ENTRY_CONTACT_WITH = "="

        /**
         * Parameter Separate With
         * 参数以什么字符串分隔
         */
        private const val PARAMETER_SEPARATE_WITH = "&"

        fun instance() = INSTANCE.Instance
    }

    object INSTANCE {
        val Instance = ParameterSignUtils()
    }

    /**
     * Sign
     *
     * @param token token值
     * @param parameters 参数Map
     *
     * 带Token的参数签名
     * 不需要校验Token的，可以参考方法[signWithoutToken]
     *
     */
    fun sign(@NonNull token: String, @NonNull parameters: Map<String, Any?>): String {
        //组合参数
        val combinedParameters = combineParameters(token, parameters)
        println("签名值：${md5(combinedParameters)}")
        return md5(combinedParameters)
    }

    /**
     * Combine parameters
     *
     * @param token token值
     * @param parameters 参数
     * @return 返回拼接后的参数字符串
     */
    private fun combineParameters(token: String, parameters: Map<String, Any?>): String {
        val sb = StringBuilder()
        //参数排序
        parameters
            //过滤掉空值
            .filter {
                //如果value是字符串
                if (it.value is String) {
                    //过滤掉空字符串值
                    (it.value as String).isNotEmpty()
                } else {
                    true
                }
            }.keys
            //参数名首字母大写
            .map {
                object : Map.Entry<String, Any?> {
                    override val key: String = transformFirstString2UppercaseIfNeeded(it)
                    override val value: Any? = parameters[it]
                }
            }
            //按key值排序(升序)
            .sortedBy { it.key }.forEach {
                println("转换后的参数：${it.key} = ${it.value}")
                //等号连接参数名和参数值，&符号连接每个键值对
                sb.append(it.key).append(PARAMETER_ENTRY_CONTACT_WITH).append(it.value)
                    .append(PARAMETER_SEPARATE_WITH)
            }
        //最后拼接Token参数
        sb.append(PARAMETER_TOKEN).append(PARAMETER_ENTRY_CONTACT_WITH).append(token)
        println("拼接后：$sb")
        return sb.toString()
    }

    /**
     * Transform first string2uppercase if needed
     *
     * @param str
     *
     * 转换字符串首字母为大写
     *
     */
    fun transformFirstString2UppercaseIfNeeded(str: String): String {
        //空串，直接返回
        if (str.isEmpty()) return str
        //长度为1，直接转大写
        if (str.length == 1) return str.uppercase()
        //首字母已经大写，直接返回
        if (str[0].isUpperCase()) return str
        //首字母转换为大写
        return str.replaceFirst(str[0].toString(), str[0].uppercase())
    }

    /**
     * Sign without token
     *
     * @param K
     * @param V
     * @param parameters
     * @return 返回签名后的字符串
     * 不带Token的参数签名
     */
    fun signWithoutToken(parameters: Map<String, Any>): String {
        return sign("", parameters)
    }

    fun md5(content: String): String {
        val hash = MessageDigest.getInstance(MD5).digest(content.toByteArray())
        val hex = StringBuilder(hash.size * 2)
        for (b in hash) {
            var str = Integer.toHexString(b.toInt())
            if (b < 0x10) {
                str = "0$str"
            }
            hex.append(str.substring(str.length - 2))
        }
        return hex.toString()
    }

}