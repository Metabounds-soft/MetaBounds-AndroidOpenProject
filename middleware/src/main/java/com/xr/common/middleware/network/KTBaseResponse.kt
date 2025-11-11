package com.xr.common.middleware.network

import com.google.gson.annotations.Expose

data class KTBaseResponse<T>(
    val data: T? = null, @Expose val code: Int, @Expose val msg: String
)