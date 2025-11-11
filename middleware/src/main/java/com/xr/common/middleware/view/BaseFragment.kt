package com.xr.common.middleware.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.xr.common.middleware.viewmodel.BaseViewModel
import java.lang.reflect.ParameterizedType

/**
 * Description:
 * CreateDate:     2023/5/12 14:41
 * Author:         agg
 */
abstract class BaseFragment<VB : ViewBinding, VM : BaseViewModel> : Fragment(), IView {

    protected lateinit var binding: VB

    protected val viewModel: VM by lazy {
        ViewModelProvider(this)[(this.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[1] as Class<VM>]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = initBinding(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initData()
        initViewModel()
        initView()
    }

    abstract fun initBinding(inflater: LayoutInflater): VB

}