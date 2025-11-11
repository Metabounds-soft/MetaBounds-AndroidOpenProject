package com.xr.common.middleware.network.interceptor

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xr.common.middleware.utils.ParameterSignUtils
import com.tencent.mmkv.MMKV
import okhttp3.*
import okio.Buffer

/**
 * Description:    Meta-Bounds 网络请求头拦截器
 * CreateDate:     2023/5/17 14:07
 * Author:         agg
 */
class HeaderInterceptor : Interceptor {

    companion object {
        private const val TOKEN = "USER_TOKEN"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request().newBuilder().build()
        //参数
        val parameters = mutableMapOf<String, String?>()
        parameters["Timestamp"] = (System.currentTimeMillis() / 1000).toInt().toString()

        if ("POST" == request.method || "PUT" == request.method) {
            request = when (request.body) {
                is FormBody -> {
                    //通过对象传递的数据进行二次解析
                    doFormParams(request, parameters)
                }

                is MultipartBody -> {
                    doMultipartBodyParams(request, parameters)
                }

                else -> {
                    //通过对象传递的数据进行二次解析
                    parseParams(request, parameters)
                }
            }

            return chain.proceed(request)
        } else {
            val headers = request.url.queryParameterNames
            headers.forEach {
                parameters[it] = request.url.queryParameter(it) ?: ""
            }
            request = chain.request().newBuilder().addHeader(
                "Sign",
                ParameterSignUtils.instance()
                    .sign(MMKV.defaultMMKV().decodeString(TOKEN) ?: "", parameters)
            ).addHeader("Timestamp", parameters["Timestamp"] as String).build()
        }

        return chain.proceed(request)
    }

    /**
     * GET
     *
     * @param request
     * @param parameters
     * @return
     */
    private fun parseParams(request: Request, parameters: MutableMap<String, String?>): Request {
        try {
            val body = request.newBuilder().build().body ?: return request
            val requestBuffer = Buffer()
            body.writeTo(requestBuffer)
            val charset = Charsets.UTF_8
            val contentType = body.contentType()
            contentType?.charset(charset)
            val json = requestBuffer.readString(charset)
            val type = object : TypeToken<HashMap<String, String>>() {}.type
            val fromJson = Gson().fromJson<HashMap<String, String>>(json, type)
            parameters.putAll(fromJson)
            val token = MMKV.defaultMMKV().decodeString(TOKEN) ?: ""
            parameters["Sign"] = ParameterSignUtils.instance().sign(token, parameters)
            val formBuild = FormBody.Builder()
            parameters.forEach { (t, u) ->
                formBuild.add(t, u ?: "")
            }
            val requestBuilder = request.newBuilder().addHeader("Token", token)
                .addHeader("Timestamp", parameters["Timestamp"] as String)
                .addHeader("Sign", parameters["Sign"] ?: "")
            return if ("POST" == request.method) {
                requestBuilder.post(formBuild.build()).build()
            } else {
                requestBuilder.put(formBuild.build()).build()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return request
    }

    /**
     * 解析form表单参数，PUT，POST
     *
     * @param request
     * @param parameters
     * @return
     */
    private fun doFormParams(request: Request, parameters: MutableMap<String, String?>): Request {
        val formBody = request.body as FormBody
        for (index in 0 until formBody.size) {
            parameters[formBody.name(index)] = formBody.value(index)
        }
        val token = MMKV.defaultMMKV().decodeString(TOKEN) ?: ""
        parameters["Sign"] = ParameterSignUtils.instance().sign(token, parameters)

        val requestBuilder = request.newBuilder().addHeader("Token", token)
            .addHeader("Timestamp", parameters["Timestamp"] as String)
            .addHeader("Sign", parameters["Sign"] ?: "")
        val formBuild = FormBody.Builder()
        parameters.forEach { (t, u) ->
            formBuild.add(t, u ?: "")
        }
        return if ("POST" == request.method) {
            requestBuilder.post(formBuild.build()).build()
        } else {
            requestBuilder.put(formBuild.build()).build()
        }
    }


    /**
     * 上传文件
     *
     * @param request
     * @param parameters
     * @return
     */
    private fun doMultipartBodyParams(
        request: Request, parameters: MutableMap<String, String?>
    ): Request {
        val multipartBody = request.body as MultipartBody
        for (index in 0 until multipartBody.size) {
            val part = multipartBody.part(index)
            if (null == part.body.contentType()) {
                var normalParamKey = ""
                val normalParamValue = getParamContent(part.body)
                val headers = part.headers
                headers?.names()?.forEach {
                    val headerContent = headers[it]
                    if (headerContent?.isNotEmpty() == true) {
                        val split = headerContent.split("name=\"")
                        if (split.size == 2) {
                            normalParamKey = split[1].split("\"")[0]
                        }
                    }
                }
                parameters[normalParamKey] = normalParamValue
            }
        }
        val token = MMKV.defaultMMKV().decodeString(TOKEN) ?: ""
        parameters["Sign"] = ParameterSignUtils.instance().sign(token, parameters)

        val requestBuilder = request.newBuilder().addHeader("Token", token)
            .addHeader("Timestamp", parameters["Timestamp"] as String)
            .addHeader("Sign", parameters["Sign"] ?: "")

        return requestBuilder.build()
    }

    private fun getParamContent(body: RequestBody): String {
        val buffer = Buffer()
        body.writeTo(buffer)
        return buffer.readUtf8()
    }

}