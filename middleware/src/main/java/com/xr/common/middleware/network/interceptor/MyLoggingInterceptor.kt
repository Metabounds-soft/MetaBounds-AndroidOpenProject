package com.xr.common.middleware.network.interceptor

import android.util.Log
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.http.promisesBody
import okhttp3.internal.platform.Platform
import okio.Buffer
import okio.GzipSource
import java.net.ConnectException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.TimeUnit

class MyLoggingInterceptor @JvmOverloads constructor() : Interceptor {

    private val TAG = MyLoggingInterceptor::class.java.simpleName

    @Volatile
    private var headersToRedact = emptySet<String>()

    @set:JvmName("level")
    @Volatile
    var level = Level.BODY

    enum class Level {
        /** No logs. */
        NONE,

        /**
         * Logs request and response lines.
         *
         * Example:
         * ```
         * --> POST /greeting http/1.1 (3-byte body)
         *
         * <-- 200 OK (22ms, 6-byte body)
         * ```
         */
        BASIC,

        /**
         * Logs request and response lines and their respective headers.
         *
         * Example:
         * ```
         * --> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         * <-- END HTTP
         * ```
         */
        HEADERS,

        /**
         * Logs request and response lines and their respective headers and bodies (if present).
         *
         * Example:
         * ```
         * --> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         *
         * Hi?
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         *
         * Hello!
         * <-- END HTTP
         * ```
         */
        BODY
    }

    fun interface Logger {
        fun log(message: String)

        companion object {
            /** A [Logger] defaults output appropriate for the current platform. */
            @JvmField
            val DEFAULT: Logger = DefaultLogger()

            private class DefaultLogger : Logger {
                override fun log(message: String) {
                    Platform.get().log(message)
                }
            }
        }
    }

    fun redactHeader(name: String) {
        val newHeadersToRedact = TreeSet(String.CASE_INSENSITIVE_ORDER)
        newHeadersToRedact += headersToRedact
        newHeadersToRedact += name
        headersToRedact = newHeadersToRedact
    }


    @JvmName("-deprecated_level")
    @Deprecated(
        message = "moved to var",
        replaceWith = ReplaceWith(expression = "level"),
        level = DeprecationLevel.ERROR
    )
    fun getLevel(): Level = level

    override fun intercept(chain: Interceptor.Chain): Response {
        val level = this.level

        val request = chain.request()
        if (level == Level.NONE) {
            return chain.proceed(request)
        }

        val logBody = level == Level.BODY
        val logHeaders = logBody || level == Level.HEADERS

        val requestBody = request.body

        val connection = chain.connection()
        var requestStartMessage =
            ("--> ${request.method} ${request.url}${if (connection != null) " " + connection.protocol() else ""}")
        if (!logHeaders && requestBody != null) {
            requestStartMessage += " (${requestBody.contentLength()}-byte body)"
        }
        Log.d(TAG, requestStartMessage)

        if (logHeaders) {
            val headers = request.headers

            if (requestBody != null) {
                // Request body headers are only present when installed as a network interceptor. When not
                // already present, force them to be included (if available) so their values are known.
                requestBody.contentType()?.let {
                    if (headers["Content-Type"] == null) {
                        Log.d(TAG, "Content-Type: $it")
                    }
                }
                if (requestBody.contentLength() != -1L) {
                    if (headers["Content-Length"] == null) {
                        Log.d(TAG, "Content-Length: ${requestBody.contentLength()}")
                    }
                }
            }

            for (i in 0 until headers.size) {
                logHeader(headers, i)
            }
            if (!logBody || requestBody == null) {
                Log.d(TAG, "--> END ${request.method}")
            } else if (bodyHasUnknownEncoding(request.headers)) {
                Log.d(TAG, "--> END ${request.method} (encoded body omitted)")
            } else if (requestBody.isDuplex()) {
                Log.d(TAG, "--> END ${request.method} (duplex request body omitted)")
            } else if (requestBody.isOneShot()) {
                Log.d(TAG, "--> END ${request.method} (one-shot body omitted)")
            } else if (!requestBody.contentType().toString().contains("multipart/form-data")) {
                val buffer = Buffer()
                requestBody.writeTo(buffer)

                val contentType = requestBody.contentType()
                val charset: Charset =
                    contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8

                if (buffer.clone().readString(StandardCharsets.UTF_8).isNotBlank()) {
                    Log.d(TAG, buffer.readString(charset))
                    Log.d(
                        TAG, "--> END ${request.method} (${requestBody.contentLength()}-byte body)"
                    )
                } else {
                    Log.d(
                        TAG,
                        "--> END ${request.method} (binary ${requestBody.contentLength()}-byte body omitted)"
                    )
                }
            }
        }

        val startNs = System.nanoTime()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            Log.d(TAG, "<-- HTTP FAILED: $e")
            throw ConnectException()
        }

        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

        val responseBody = response.body!!
        val contentLength = responseBody.contentLength()
        val bodySize = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"
        Log.d(
            TAG,
            "<-- ${response.code}${if (response.message.isEmpty()) "" else ' ' + response.message} ${response.request.url} (${tookMs}ms${if (!logHeaders) ", $bodySize body" else ""})"
        )

        if (logHeaders) {
            val headers = response.headers
            for (i in 0 until headers.size) {
                logHeader(headers, i)
            }

            if (!logBody || !response.promisesBody()) {
                Log.d(TAG, "<-- END HTTP")
            } else if (bodyHasUnknownEncoding(response.headers)) {
                Log.d(TAG, "<-- END HTTP (encoded body omitted)")
            } else {
                val source = responseBody.source()
                source.request(Long.MAX_VALUE) // Buffer the entire body.
                var buffer = source.buffer

                var gzippedLength: Long? = null
                if ("gzip".equals(headers["Content-Encoding"], ignoreCase = true)) {
                    gzippedLength = buffer.size
                    GzipSource(buffer.clone()).use { gzippedResponseBody ->
                        buffer = Buffer()
                        buffer.writeAll(gzippedResponseBody)
                    }
                }

                val contentType = responseBody.contentType()
                val charset: Charset =
                    contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8

                if (buffer.clone().readString(StandardCharsets.UTF_8).isEmpty()) {
                    Log.d(TAG, "<-- END HTTP (binary ${buffer.size}-byte body omitted)")
                    return response
                }

                if (contentLength != 0L) {
                    Log.d(TAG, buffer.clone().readString(charset))
                }

                if (gzippedLength != null) {
                    Log.d(
                        TAG, "<-- END HTTP (${buffer.size}-byte, $gzippedLength-gzipped-byte body)"
                    )
                } else {
                    Log.d(TAG, "<-- END HTTP (${buffer.size}-byte body)")
                }
            }
        }

        return response
    }

    private fun logHeader(headers: Headers, i: Int) {
        val value = if (headers.name(i) in headersToRedact) "██" else headers.value(i)
        Log.d(TAG, headers.name(i) + ": " + value)
    }

    private fun bodyHasUnknownEncoding(headers: Headers): Boolean {
        val contentEncoding = headers["Content-Encoding"] ?: return false
        return !contentEncoding.equals(
            "identity", ignoreCase = true
        ) && !contentEncoding.equals("gzip", ignoreCase = true)
    }
}