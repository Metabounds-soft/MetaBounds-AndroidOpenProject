package com.xr.common.middleware.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.StringUtils.getString
import com.xr.common.middleware.BuildConfig
import com.xr.common.middleware.R
import com.xr.common.middleware.network.ApiException
import com.xr.common.middleware.network.KTBaseResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

typealias RequestBlock<T> = suspend () -> T
/**
 * 处理网络请求业务体
 */
typealias Block<T> = (T?) -> Unit

/**
 * 处理异常情况
 */
typealias Error = (code: Int, message: String?) -> Unit
/**
 * 处理取消
 */
typealias Cancel = (e: Exception) -> Unit

abstract class BaseViewModel : ViewModel() {
    /**
     * requestBlock: 请求体
     * block： 函数体
     * filterBlock：过滤体
     * onError ： 错误函数体
     * isShowProcess：是否显示进度
     * cancel：取消操作
     *
     * */
    open fun <T> launch(
        requestBlock: RequestBlock<KTBaseResponse<T>?>,
        block: Block<T?>,
        filterBlock: (KTBaseResponse<T>?) -> Boolean = { true },
        onError: Error? = null,
        isShowProcess: Boolean = true,
        cancel: Cancel? = null,
        processText: String = ""
    ): Job {
        return viewModelScope.launch {
            flow {
                emit(requestBlock.invoke())
            }.filter {
                filterBlock.invoke(it)
            }.flowOn(Dispatchers.IO).onStart {
                if (isShowProcess) {
                    //显示load
//                        mStateLiveData.value = ShowLoading(processText)
                }
            }.catch {
                when (it) {
                    is CancellationException -> {
                        cancel?.invoke(it)
                    }

                    is Exception -> {
                        doError(it, onError)
                    }
                }
            }.onCompletion {
                if (isShowProcess) {
                    //关闭弹窗
//                        mStateLiveData.value = DismissLoading
                }
            }.collect {
                when (it?.code) {
                    200 -> {
                        block.invoke(it.data)
                    }

                    else -> {
                        doError(ApiException(it!!.code, it.msg), onError)
                    }
                }

            }
        }
    }

    /**
     * requestBlock: 请求体
     * block： 函数体
     * filterBlock：过滤体
     * onError ： 错误函数体
     * isShowProcess：是否显示进度
     * cancel：取消操作
     *
     * */
    open fun <T> launchBaseResponse(
        requestBlock: RequestBlock<KTBaseResponse<T>?>,
        block: Block<KTBaseResponse<T>?>,
        filterBlock: (KTBaseResponse<T>?) -> Boolean = { true },
        onError: Error? = null,
        isShowProcess: Boolean = true,
        cancel: Cancel? = null,
        processText: String = ""
    ): Job {
        return viewModelScope.launch {
            flow {
                emit(requestBlock.invoke())
            }.filter {
                filterBlock.invoke(it)
            }.flowOn(Dispatchers.IO).onStart {
                if (isShowProcess) {
                    //显示load
//                        mStateLiveData.value = ShowLoading(processText)
                }
            }.catch {
                when (it) {
                    is CancellationException -> {
                        cancel?.invoke(it)
                    }

                    is Exception -> {
                        doError(it, onError)
                    }
                }
            }.onCompletion {
                if (isShowProcess) {
                    //关闭弹窗
//                        mStateLiveData.value = DismissLoading
                }
            }.collect {
                when (it?.code) {
                    200 -> {
                        block.invoke(it)
                    }

                    else -> {
                        block.invoke(it)
                        doError(ApiException(it!!.code, it.msg), onError)
                    }
                }

            }
        }
    }


    private fun doError(e: Exception?, error: Error?) {
        if (BuildConfig.DEBUG) {
            e?.printStackTrace()
        }
        error?.invoke(-1, getString(R.string.network_error_tip))
//        mStateLiveData.postValue(ErrorState(getString(R.string.network_error_tip)))
    }
}

class NoneViewModel : BaseViewModel()