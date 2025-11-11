package com.xr.common.middleware.network

class ApiException(val code: Int, message: String?) : Exception(message)