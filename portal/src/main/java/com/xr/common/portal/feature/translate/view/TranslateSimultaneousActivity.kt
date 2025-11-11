package com.xr.common.portal.feature.translate.view

import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.metabounds.libglass.v2.req.CmdV2SimulLanguageSetReq
import com.tencent.mmkv.MMKV
import com.xr.common.middleware.manager.MBDeviceManager
import com.xr.common.middleware.model.bean.StandardLanguage
import com.xr.common.middleware.utils.LanguageUtil
import com.xr.common.middleware.view.BaseActivity
import com.xr.common.middleware.view.CommonPopupWindow
import com.xr.common.middleware.viewmodel.NoneViewModel
import com.xr.common.portal.R
import com.xr.common.portal.databinding.ActivityTranslateSimultaneousBinding
import com.xr.common.portal.feature.task.SimultaneousTask.Companion.SIMULTANEOUS_SOURCE
import com.xr.common.portal.feature.task.SimultaneousTask.Companion.SIMULTANEOUS_SOURCE_DISPLAY
import com.xr.common.portal.feature.task.SimultaneousTask.Companion.SIMULTANEOUS_TARGET
import com.xr.common.portal.feature.translate.view.adapter.LanguageAdapter

class TranslateSimultaneousActivity :
    BaseActivity<ActivityTranslateSimultaneousBinding, NoneViewModel>() {

    private val TAG = this.javaClass.simpleName
    private val POP_TYPE_SOURCE = 0
    private val POP_TYPE_TARGET = 1
    private var languagePop: PopupWindow? = null
    private var languageCodeSource: String =
        MMKV.defaultMMKV().decodeString(SIMULTANEOUS_SOURCE, StandardLanguage.ENGLISH.code)
            .toString()
    private var languageCodeTarget: String =
        MMKV.defaultMMKV().decodeString(SIMULTANEOUS_TARGET, StandardLanguage.CHINESE.code)
            .toString()

    override fun initBinding(inflater: LayoutInflater): ActivityTranslateSimultaneousBinding =
        ActivityTranslateSimultaneousBinding.inflate(layoutInflater)

    override fun initData() {
        initLanguageUI(languageCodeSource, languageCodeTarget)
    }

    override fun initView() {
        binding.header.ivBack.setOnClickListener { finish() }
        binding.header.title.text =
            resources.getString(com.xr.common.middleware.R.string.translate_simultaneous)
        binding.tvSource.setOnClickListener {
            Log.i(TAG, "initView: tvSource.setOnClickListener")
            showPopSelectLanguage(
                it,
                binding.tvSource.text.toString(),
                binding.tvTarget.text.toString(),
                POP_TYPE_SOURCE
            )
        }

        binding.tvTarget.setOnClickListener {
            Log.i(TAG, "initView: tvTarget.setOnClickListener")
            showPopSelectLanguage(
                it,
                binding.tvSource.text.toString(),
                binding.tvTarget.text.toString(),
                POP_TYPE_TARGET
            )
        }

        binding.swSourceDisplay.isChecked =
            MMKV.defaultMMKV().decodeBool(SIMULTANEOUS_SOURCE_DISPLAY, true)
        binding.swSourceDisplay.setOnCheckedChangeListener { _, isChecked ->
            MMKV.defaultMMKV().encode(SIMULTANEOUS_SOURCE_DISPLAY, isChecked)
        }
    }

    override fun initViewModel() {}

    override fun onDestroy() {
        super.onDestroy()
        MBDeviceManager.getInstance().sendCmd(
            CmdV2SimulLanguageSetReq(
                LanguageUtil.getMetaLanguage(languageCodeSource).code,
                LanguageUtil.getMetaLanguage(languageCodeTarget).code
            )
        )
    }

    private fun showPopSelectLanguage(
        parentView: View, curLanguage: String, tarLanguage: String, type: Int
    ) {
        Log.i(TAG, "showPopSelectLanguage: cur=$curLanguage,tar=$tarLanguage")
        val view = LayoutInflater.from(this).inflate(R.layout.pop_select_language, null, false)
        val recyclerview = view.findViewById<RecyclerView>(R.id.recycler_view)
        val mData = ArrayList<String>()
        mData.add(getString(com.xr.common.middleware.R.string.simplified_chinese))
        mData.add(getString(com.xr.common.middleware.R.string.english))
        if (type == POP_TYPE_SOURCE) mData.remove(curLanguage) else mData.remove(tarLanguage)

        Log.e(TAG, "showPopSelectLanguage: ${mData.size},,,${mData}")
        val adapter = LanguageAdapter(mData)
        recyclerview.adapter = adapter
        recyclerview.layoutManager = LinearLayoutManager(this)
        adapter.setOnItemClickListener { _, _, position ->
            when (mData[position]) {
                getString(com.xr.common.middleware.R.string.simplified_chinese) -> {
                    if (type == POP_TYPE_TARGET) {
                        languageCodeTarget = StandardLanguage.CHINESE.code
                    } else {
                        languageCodeSource = StandardLanguage.CHINESE.code
                    }
                }

                getString(com.xr.common.middleware.R.string.english) -> {
                    if (type == POP_TYPE_TARGET) {
                        languageCodeTarget = StandardLanguage.ENGLISH.code
                    } else {
                        languageCodeSource = StandardLanguage.ENGLISH.code
                    }
                }
            }

            languagePop?.dismiss()
            MMKV.defaultMMKV().encode(SIMULTANEOUS_TARGET, languageCodeTarget)
            MMKV.defaultMMKV().encode(SIMULTANEOUS_SOURCE, languageCodeSource)
            MMKV.defaultMMKV().sync()
            Log.i(
                TAG,
                "languageCodeSource:$languageCodeSource, languageCodeTarget:$languageCodeTarget"
            )
            initLanguageUI(languageCodeSource, languageCodeTarget)

            MBDeviceManager.getInstance().sendCmd(
                CmdV2SimulLanguageSetReq(
                    LanguageUtil.getMetaLanguage(languageCodeSource).code,
                    LanguageUtil.getMetaLanguage(languageCodeTarget).code
                )
            )
        }

        val width = SizeUtils.dp2px(148f)
        languagePop = CommonPopupWindow.Builder(this).setView(view)
            .setWidthAndHeight(width, ViewGroup.LayoutParams.WRAP_CONTENT).setBackGroundLevel(1f)
            .setOutsideTouchable(true).builder()
        var xoff = parentView.width - width
        languagePop?.showAsDropDown(
            parentView, xoff, SizeUtils.dp2px(10f), Gravity.NO_GRAVITY
        )
    }

    private fun initLanguageUI(source: String, target: String) {
        when (source) {
            StandardLanguage.CHINESE.code -> {
                binding.tvSource.text =
                    getString(com.xr.common.middleware.R.string.simplified_chinese)
            }

            StandardLanguage.ENGLISH.code -> {
                binding.tvSource.text = getString(com.xr.common.middleware.R.string.english)
            }

            else -> {
                binding.tvSource.text = getString(com.xr.common.middleware.R.string.english)
            }
        }
        when (target) {
            StandardLanguage.CHINESE.code -> {
                binding.tvTarget.text =
                    getString(com.xr.common.middleware.R.string.simplified_chinese)
            }

            StandardLanguage.ENGLISH.code -> {
                binding.tvTarget.text = getString(com.xr.common.middleware.R.string.english)
            }

            else -> {
                binding.tvTarget.text =
                    getString(com.xr.common.middleware.R.string.simplified_chinese)
            }
        }

        binding.tvRecognized.text = resources.getString(
            com.xr.common.middleware.R.string.translate_recognized,
            binding.tvSource.text,
            binding.tvTarget.text
        )
    }
}