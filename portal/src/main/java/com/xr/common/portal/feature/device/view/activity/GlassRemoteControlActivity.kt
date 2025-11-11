package com.xr.common.portal.feature.device.view.activity

import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.metabounds.libglass.bluetooth.protocol.basic.BizType
import com.metabounds.libglass.v2.bean.KeySetVirtualKeyBean
import com.metabounds.libglass.v2.interfaces.MBCmdV2RspMsgListener
import com.metabounds.libglass.v2.mbEnum.MBCmdV2Type
import com.metabounds.libglass.v2.req.CmdV2ReqVirtualKey
import com.metabounds.libglass.v2.rsp.CmdV2Rsp
import com.metabounds.libglass.v2.rsp.CmdV2VirtualKeyRsp
import com.xr.common.middleware.manager.MBDeviceManager
import com.xr.common.middleware.utils.checkDeviceConnect
import com.xr.common.middleware.view.BaseActivity
import com.xr.common.middleware.viewmodel.NoneViewModel
import com.xr.common.portal.databinding.ActivityGlassRemoteControlBinding
import com.xr.common.portal.feature.device.model.bean.CmdDeviceRemoteControlBean
import com.xr.common.portal.feature.device.view.adapter.CmdDeviceRemoteControlAdapter

/**
 * Description:    远程控制眼镜
 * CreateDate:     2025/10/28 10:46
 * Author:         agg
 */
class GlassRemoteControlActivity : BaseActivity<ActivityGlassRemoteControlBinding, NoneViewModel>(),
    MBCmdV2RspMsgListener {

    private val mData = ArrayList<CmdDeviceRemoteControlBean>()
    private var mAdapter: CmdDeviceRemoteControlAdapter? = null

    override fun initBinding(inflater: LayoutInflater): ActivityGlassRemoteControlBinding =
        ActivityGlassRemoteControlBinding.inflate(layoutInflater)

    override fun initData() {
        MBDeviceManager.getInstance().addBTCmdRspListener(this)
    }

    override fun initViewModel() {}

    override fun initView() {
        initHeader()
        initRecyclerView()
        initClickListener()
    }

    override fun onMetaGlassCmdRspMsg(p0: String?, p1: CmdV2Rsp<*>?) {
        if (p1?.bizType == BizType.BIZ_KEY_SETTINGS_GROUP && p1.cmdV2Type == MBCmdV2Type.KEY_SET_VIRTUAL_KEY.toInt()) {
            val bean = (p1 as CmdV2VirtualKeyRsp).value as KeySetVirtualKeyBean
            when (bean.event) {
                0x00 -> {
                    mData.add(0, CmdDeviceRemoteControlBean().apply {
                        name = "前滑"
                        time = System.currentTimeMillis()
                        result = bean.err == 0
                    })
                }

                0x01 -> {
                    mData.add(0, CmdDeviceRemoteControlBean().apply {
                        name = "后滑"
                        time = System.currentTimeMillis()
                        result = bean.err == 0
                    })
                }

                0x02 -> {
                    mData.add(0, CmdDeviceRemoteControlBean().apply {
                        name = "触控条单击"
                        time = System.currentTimeMillis()
                        result = bean.err == 0
                    })
                }

                0x0b -> {
                    mData.add(0, CmdDeviceRemoteControlBean().apply {
                        name = "触控条双击"
                        time = System.currentTimeMillis()
                        result = bean.err == 0
                    })
                }

                0x03 -> {
                    mData.add(0, CmdDeviceRemoteControlBean().apply {
                        name = "触控条长按"
                        time = System.currentTimeMillis()
                        result = bean.err == 0
                    })
                }

                0x04 -> {
                    mData.add(0, CmdDeviceRemoteControlBean().apply {
                        name = "Power单击"
                        time = System.currentTimeMillis()
                        result = bean.err == 0
                    })
                }

                0x05 -> {
                    mData.add(0, CmdDeviceRemoteControlBean().apply {
                        name = "Power双击"
                        time = System.currentTimeMillis()
                        result = bean.err == 0
                    })
                }

                0x06 -> {
                    mData.add(0, CmdDeviceRemoteControlBean().apply {
                        name = "Power长按"
                        time = System.currentTimeMillis()
                        result = bean.err == 0
                    })
                }

                0x0c -> {
                    mData.add(0, CmdDeviceRemoteControlBean().apply {
                        name = "功能键单击"
                        time = System.currentTimeMillis()
                        result = bean.err == 0
                    })
                }

                0x0d -> {
                    mData.add(0, CmdDeviceRemoteControlBean().apply {
                        name = "功能键双击"
                        time = System.currentTimeMillis()
                        result = bean.err == 0
                    })
                }

                0x0e -> {
                    mData.add(0, CmdDeviceRemoteControlBean().apply {
                        name = "功能键长按"
                        time = System.currentTimeMillis()
                        result = bean.err == 0
                    })
                }
            }
            mAdapter?.notifyItemInserted(0)
            binding.recyclerView.smoothScrollToPosition(0)
        }
    }

    private fun initHeader() {
        binding.header.title.setText(com.xr.common.middleware.R.string.remote_control)
        binding.header.ivBack.setOnClickListener { finish() }
    }

    private fun initRecyclerView() {
        binding.recyclerView.setLayoutManager(LinearLayoutManager(this))
        mAdapter = CmdDeviceRemoteControlAdapter(mData)
        binding.recyclerView.setAdapter(mAdapter)
    }

    private fun initClickListener() {
        binding.btnKeyForword.setOnClickListener { sendCmd(0x00) }
        binding.btnKeyBackword.setOnClickListener { sendCmd(0x01) }
        binding.btnTouchBarSingleClick.setOnClickListener { sendCmd(0x02) }
        binding.btnTouchBarDoubleClick.setOnClickListener { sendCmd(0x0b) }
        binding.btnTouchBarLongPress.setOnClickListener { sendCmd(0x03) }
        binding.btnPowerSingleClick.setOnClickListener { sendCmd(0x04) }
        binding.btnPowerDoubleClick.setOnClickListener { sendCmd(0x05) }
        binding.btnPowerLongPress.setOnClickListener { sendCmd(0x06) }
        binding.btnFunSingleClick.setOnClickListener { sendCmd(0x0c) }
        binding.btnFunDoubleClick.setOnClickListener { sendCmd(0x0d) }
        binding.btnFunLongPress.setOnClickListener { sendCmd(0x0e) }
    }

    private fun sendCmd(byte: Byte) {
        checkDeviceConnect {
            MBDeviceManager.getInstance().sendCmd(CmdV2ReqVirtualKey(byte))
        }
    }

}