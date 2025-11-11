package com.xr.common.portal.feature.device.view.activity

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withResumed
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ToastUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.metabounds.libglass.bluetooth.MetaGlassBtConnectListener
import com.metabounds.libglass.bluetooth.MetaGlassState
import com.metabounds.libglass.bluetooth.protocol.basic.BizType
import com.metabounds.libglass.bluetooth.protocol.bean.ConnectParam
import com.metabounds.libglass.bluetooth.protocol.bean.MetaSEToken
import com.metabounds.libglass.v2.bean.MBDevice
import com.metabounds.libglass.v2.ble.MBBLEScanner
import com.metabounds.libglass.v2.interfaces.MBCmdV2RspMsgListener
import com.metabounds.libglass.v2.interfaces.MBScannerListener
import com.metabounds.libglass.v2.mbEnum.MBCmdV2Type
import com.metabounds.libglass.v2.mbEnum.MBConnectivity
import com.metabounds.libglass.v2.req.CmdV2BatteryReq
import com.metabounds.libglass.v2.req.CmdV2Bind
import com.metabounds.libglass.v2.rsp.CmdV2BindRsp
import com.metabounds.libglass.v2.rsp.CmdV2Rsp
import com.xr.common.middleware.base.DeviceType
import com.xr.common.middleware.base.LiveEventBusKey
import com.xr.common.middleware.base.getDeviceType
import com.xr.common.middleware.utils.IMEIUtil
import com.xr.common.middleware.utils.PermissionKT
import com.xr.common.middleware.utils.PermissionUtil
import com.xr.common.middleware.utils.SystemUtils
import com.xr.common.middleware.utils.checkOpenBluetooth
import com.xr.common.middleware.utils.startSettingOpenBluetooth
import com.xr.common.middleware.view.BaseActivity
import com.xr.common.portal.databinding.ActivityGlassDeviceBindBinding
import com.xr.common.middleware.manager.MBDeviceManager
import com.xr.common.middleware.model.bean.UserV2
import com.xr.common.middleware.model.data.UserV2DBManager
import com.xr.common.portal.feature.device.view.adapter.DeviceBindListAdapter
import com.xr.common.portal.feature.device.view.adapter.decoration.MyDecoration
import com.xr.common.portal.feature.device.view.dialog.ConfirmDialog
import com.xr.common.portal.feature.device.view.dialog.NewGlassBindDialog
import com.xr.common.portal.feature.device.view.dialog.NewGlassBindDialogListener
import com.xr.common.portal.feature.device.view.dialog.SearchHelpDialog
import com.xr.common.portal.feature.device.viewmodel.GlassSearchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class GlassDeviceBindActivity :
    BaseActivity<ActivityGlassDeviceBindBinding, GlassSearchViewModel>(), MBScannerListener,
    MBCmdV2RspMsgListener {

    companion object {
        private val TAG = GlassDeviceBindActivity::class.java.simpleName
        private const val TIME_OUT = 30 * 1000L
        private const val USER_ID = 12315
    }

    private var bindDialog: NewGlassBindDialog? = null
    private var isOpenGPS = true
    private var isSearching = false
    private var isConnecting = false
    private var isDestroy = false
    private var metaGlassState: MetaGlassState? = null
    private var mbBleScanner: MBBLEScanner? = null
    private var mbDevice: MBDevice? = null
    private var cmdJob: Job? = null
    private var cloudToken: String = ""
    private val mAdapter by lazy { DeviceBindListAdapter() }

    override fun initBinding(inflater: LayoutInflater): ActivityGlassDeviceBindBinding =
        ActivityGlassDeviceBindBinding.inflate(layoutInflater)

    override fun initData() {
        MBDeviceManager.getInstance().addBTConnectListener(btConnectListener)
    }

    override fun initViewModel() {
        viewModel.deviceTokenObs.observe(this) {
            if (it.data != null && it.code == 200) {
                val temp = it.data?.token?.toLong() ?: 0L
                cloudToken = temp.toString()
                if (temp != 0L) {
                    mbDevice?.let { device ->
                        dismissProgress()
                        startConnect(temp.toInt())
                    }
                }
            } else {
                ToastUtils.showShort(it.msg)
                isConnecting = false
                dismissProgress()
            }
        }
    }

    override fun initView() {
        binding.ivBack.setOnClickListener { finish() }
        binding.btnSearchAgain.setOnClickListener { compactStartSearch() }
        binding.tvSearchAgain.setOnClickListener { compactStartSearch() }
        lifecycleScope.launch { lifecycle.withResumed { compactStartSearch() } }
        binding.ivAbout.setOnClickListener { showHelpDialog() }
        binding.searchQuestion.setOnClickListener { showHelpDialog() }

        binding.listDeviceSearch.run {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(this@GlassDeviceBindActivity)
            addItemDecoration(
                MyDecoration(
                    this@GlassDeviceBindActivity, LinearLayoutManager.VERTICAL, 24, Color.WHITE
                )
            )
            itemAnimator = DefaultItemAnimator()
        }
        mAdapter.setOnItemClickListener {
            mbBleScanner?.stopScan()
            buildDevice(it)
        }
    }

    override fun onResume() {
        super.onResume()
        if (SystemUtils.isOpenLocService(this) && !isOpenGPS) compactStartSearch()
        if (metaGlassState?.isConnecting == true) bindDialog?.setConnecting()
        if (metaGlassState?.isConnected == true) bindDialog?.setConnectSuccess()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isDestroy) MBDeviceManager.getInstance().destroy()

        MBDeviceManager.getInstance().removeBTConnectListener(btConnectListener)
        MBDeviceManager.getInstance().removeBTCmdRspListener(this)
        mbBleScanner?.stopScan()
        bindDialog?.dismiss()
        mHandler.removeCallbacksAndMessages(null)
    }

    override fun onScannerStart() {
        lifecycleScope.launch {
            isSearching = true
            binding.imgSearch.start()
        }
    }

    override fun onScannerResult(p0: MBDevice) {
        if (p0.mbProductType != DeviceType.G12S0.type && p0.mbProductType != DeviceType.G12X1.type && p0.mbProductType != DeviceType.G07S3.type) {
            return
        }
        lifecycleScope.launch {
            mAdapter.addDevice(p0)
        }
    }

    override fun onScannerFinish() {
        lifecycleScope.launch {
            isSearching = false
            binding.imgSearch.stop()
            if (mAdapter.itemCount > 0) {
                binding.searchTitle.text =
                    getString(com.xr.common.middleware.R.string.glass_search_finish)
                binding.searchTips.text =
                    getString(com.xr.common.middleware.R.string.glass_search_no_device_tips)
                binding.tvSearchAgain.visibility = View.VISIBLE
            } else {
                binding.searchTitle.text =
                    getString(com.xr.common.middleware.R.string.glass_search_no_device)
                binding.searchTips.text =
                    getString(com.xr.common.middleware.R.string.glass_search_no_device_tips)
                binding.tvSearchAgain.visibility = View.VISIBLE
            }
        }
    }

    override fun onMetaGlassCmdRspMsg(p0: String?, p1: CmdV2Rsp<*>) {
        if (p1.bizType == BizType.CMD_CONNECT) {
            when (p1.cmdV2Type) {
                MBCmdV2Type.CMD_CONNECTION_BIND.toInt() -> {
                    mHandler.removeMessages(1)
                    val result = (p1 as CmdV2BindRsp).value
                    Log.e(TAG, "CMD_CONNECTION_BIND ：${result}")
                    if (result) {
                        bindDialog?.setConnectSuccess()
                        complete()
                    } else {
                        bindDialog?.setConnectFail()
                        MBDeviceManager.getInstance().destroy()
                    }
                }

                else -> {}
            }
        }
    }

    private fun showBindDialog() {
        if (bindDialog != null && bindDialog!!.isShowing) {
            return
        }
        isDestroy = true
        bindDialog = NewGlassBindDialog(this, mbDevice!!)
        bindDialog?.show()
        bindDialog?.setOnNewGlassBindDialogListener(object : NewGlassBindDialogListener {
            override fun onCancel() {
                if (isConnecting) {
                    MBDeviceManager.getInstance().destroy()
                }
                isConnecting = false
                isDestroy = true
                compactStartSearch()
            }

            override fun onConfirm() {
                isDestroy = false
                bindDialog?.dismiss()
                finish()
            }

            override fun onRefresh() {
                isDestroy = true
                mbDevice?.let {
                    isConnecting = false
                    buildDevice(it)
                }
            }

        })
        bindDialog?.setOnDismissListener { bindDialog = null }
    }

    private fun compactStartSearch() {
        if (mbBleScanner == null) {
            mbBleScanner = MBBLEScanner(this).apply {
                setMbScannerListener(this@GlassDeviceBindActivity)
            }
        }
        isOpenGPS = true

        // 判断蓝牙权限是否开启
        PermissionUtil.get().with(this, PermissionKT.bluetoothPermission) {
            if (it) {
                // 判断定位服务是否开启
                if (SystemUtils.isOpenLocService(this)) {
                    checkBluetoothIsOpen()
                } else {
                    showLocationServiceDialog()
                }
            } else {
                isOpenGPS = false
                showPermissionDialog()
            }
        }
    }

    private fun checkBluetoothIsOpen() {
        val result = checkOpenBluetooth()
        if (result) {
            startSearch()
        } else {
            showOpenBlueDialog()
        }
    }

    private fun showLocationServiceDialog() {
        isOpenGPS = false
        val dialog = ConfirmDialog(this)
        dialog.show()
        dialog.setTitle(getString(com.xr.common.middleware.R.string.location_service_not_turned_on))
            .setContent(getString(com.xr.common.middleware.R.string.click_to_confirm_to_start_the_location_service))
        dialog.setOnConfirm {
            SystemUtils.openLocService(this)
            dialog.dismiss()
        }
    }

    private fun showPermissionDialog() {
        val dialog = ConfirmDialog(this)
        dialog.show()
        dialog.setTitle(getString(com.xr.common.middleware.R.string.permission_not_enabled))
            .setContent(getString(com.xr.common.middleware.R.string.open_bluetooth_and_location_tip))
        dialog.setOnConfirm {
            startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:${packageName}")
                )
            )
            dialog.dismiss()
        }
    }

    private fun showOpenBlueDialog() {
        isOpenGPS = false
        val dialog = ConfirmDialog(this)
        dialog.show()
        dialog.setTitle(getString(com.xr.common.middleware.R.string.bluetooth_not_turned_on))
            .setContent(getString(com.xr.common.middleware.R.string.click_ok_to_turn_on_bluetooth))
        dialog.setOnConfirm {
            startSettingOpenBluetooth()
            dialog.dismiss()
        }
        dialog.setOnCancel {
            super.onBackPressed()
        }
    }

    private fun startSearch() {
        mAdapter.clear()
        binding.listDeviceSearch.visibility = View.VISIBLE
        binding.btnSearchAgain.visibility = View.GONE
        binding.tvSearchAgain.visibility = View.GONE
        binding.searchQuestion.visibility = View.GONE
        binding.searchTitle.visibility = View.VISIBLE
        binding.searchTitle.text =
            getString(com.xr.common.middleware.R.string.pair_glass_search_title)
        binding.searchTips.text =
            getString(com.xr.common.middleware.R.string.pair_glass_search_tips)
        mbBleScanner?.startScan()
    }

    private fun showHelpDialog() {
        val dialog = SearchHelpDialog(this)
        dialog.show()
        dialog.setOnAgainClickListener {
            if (!isSearching) {
                compactStartSearch()
            }
        }
    }

    private fun buildDevice(mbDevice: MBDevice) {
        Log.i(
            TAG,
            "buildDevice: isConnecting=$isConnecting,ble=${mbDevice.bleAddress},type=${mbDevice.mbConnectivity},name=${mbDevice.name}"
        )
        if (isConnecting) return

        this.mbDevice = mbDevice
        isConnecting = true
        Log.i(TAG, "buildDevice: loading...")
        showProgress(null)
        viewModel.getDeviceToken()
    }

    private fun startConnect(deviceToken: Int) {
        if (mbDevice == null) {
            return
        }
        cmdJob?.cancel()
        MBDeviceManager.getInstance().buildDevice(mbDevice!!)
        MBDeviceManager.getInstance().setParams(ConnectParam(
            MetaSEToken(
                deviceToken, USER_ID, IMEIUtil.getIMEI1(this)
            ), false, TIME_OUT
        ).apply {
            isDirectConnect = true
        })
        if (mbDevice?.mbConnectivity == MBConnectivity.BLE) {
            MBDeviceManager.getInstance().connectBle()
        } else {
            MBDeviceManager.getInstance().connectBT()
            MBDeviceManager.getInstance().addBTCmdRspListener(this)
        }
        showBindDialog()
        mHandler.sendEmptyMessageDelayed(1, TIME_OUT)
    }

    private fun complete() {
        mbDevice?.let {
            if (it.mbConnectivity == MBConnectivity.BT) {
                val seToken = MBDeviceManager.getInstance().getDeviceToken()
                viewModel.bindDevice(
                    it.bleAddress, it.name ?: "", seToken.toString(), it.sourceData
                )
                MBDeviceManager.getInstance().updateKey(seToken)
            }
            Log.i(TAG, "bind complete: ${it.sourceData.getDeviceType()}")

            LiveEventBus.get<Boolean>(LiveEventBusKey.BIND_SUCCESS).post(true)
            MBDeviceManager.getInstance().sendCmd(CmdV2BatteryReq())
            MainScope().launch(Dispatchers.IO) {
                UserV2DBManager.getInstance().saveUser(UserV2())
            }
        }
    }

    private val btConnectListener = MetaGlassBtConnectListener { p1, state ->
        if (mbDevice == null || mbDevice?.mbConnectivity == MBConnectivity.BLE) {
            Log.e(TAG, "MetaGlassBtConnectListener: ${mbDevice?.mbConnectivity}")
            return@MetaGlassBtConnectListener
        }
        Log.i(TAG, "MetaGlassBtConnectListener: $p1,$state")
        metaGlassState = state
        bindDialog?.run {
            when (state.state) {
                MetaGlassState.State.DISCONNECT, MetaGlassState.State.CONNECT_FAILED -> {
                    isConnecting = false
                    setConnectFail()
                    MBDeviceManager.getInstance().destroy()
                    mHandler.removeMessages(1)
                }

                MetaGlassState.State.CONNECTING -> setConnecting()

                MetaGlassState.State.CONNECTED -> {
                    MBDeviceManager.getInstance()
                        .sendCmd(CmdV2Bind().apply { timeout_period = TIME_OUT })
                }

                else -> {}
            }
        }
    }

    private val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 1) {
                MBDeviceManager.getInstance().destroy()
            }
        }
    }

}