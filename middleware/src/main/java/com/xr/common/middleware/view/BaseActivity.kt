package com.xr.common.middleware.view

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.xr.common.middleware.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.reflect.ParameterizedType

/**
 * Description:
 * CreateDate:     2023/5/11 11:48
 * Author:         agg
 */
abstract class BaseActivity<VB : ViewBinding, VM : BaseViewModel> : AppCompatActivity(), IView {

    protected lateinit var binding: VB

    protected val viewModel: VM by lazy {
        ViewModelProvider(this)[(this.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[1] as Class<VM>]
    }

    /**
     * 加载中
     */
    private var loadingDialog: LoadingDialog? = null
    private var loadingJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = initBinding(layoutInflater)
        setContentView(binding.root)

        initData()
        initViewModel()
        initView()
    }

    abstract fun initBinding(inflater: LayoutInflater): VB

    fun showProgress(text: String?) {
        loadingJob = lifecycleScope.launch(Dispatchers.Main) {
            delay(200)
            if (loadingDialog == null) {
                loadingDialog = LoadingDialog(this@BaseActivity)
            }

            loadingDialog?.show()
            loadingDialog?.setMsg(text)
        }

    }

    fun showProgress(text: String?, isCancel: Boolean) {
        loadingJob = lifecycleScope.launch(Dispatchers.Main) {
            delay(200)
            if (loadingDialog == null) {
                loadingDialog = LoadingDialog(this@BaseActivity)
            }

            loadingDialog?.show()
            loadingDialog?.setMsg(text)
            if (isCancel) {
                loadingDialog?.setOnKeyListener { dialog, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        dismissProgress()
                        finish()
                        return@setOnKeyListener true
                    }
                    return@setOnKeyListener false
                }
            }
        }

    }

    fun dismissProgress() {
        loadingJob?.cancel()
        if (loadingDialog == null) {
            lifecycleScope.launch(Dispatchers.Main) {
                delay(200)
                loadingDialog?.dismiss()
                loadingDialog = null
            }
        } else {
            loadingDialog?.dismiss()
            loadingDialog = null
        }

    }

}