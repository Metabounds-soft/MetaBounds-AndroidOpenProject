package com.xr.common.middleware.easynet.p2p

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.Channel
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener
import android.net.wifi.p2p.WifiP2pManager.PeerListListener
import android.os.Build
import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.annotation.RequiresApi
import com.xr.common.middleware.easynet.manager.EasyNetManager

class WifiP2pConnect(val context: Context) : BroadcastReceiver() {
    companion object {
        private const val TAG = "WifiP2pConnect"
        private const val MSG_TIMEOUT = 0
        private const val TIMEOUT = 30000L
    }

    var deviceAddress = ""
    var deviceWifiIp = ""
    var deviceWifiPort = 0

    private var wifiP2pManager: WifiP2pManager =
        context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    private var channel: Channel = wifiP2pManager.initialize(context, context.mainLooper, null)
    private var isConnect = false
    private var isConnecting = false
    private var isFindDevice = false
    private val myHandler by lazy { MyHandler() }
    var isAttemptToConnect = true


    @SuppressLint("HandlerLeak")
    inner class MyHandler : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MSG_TIMEOUT -> {
                    Log.d(TAG, "GlassConnect connect timeout")
                    EasyNetManager.getInstance().wifiP2pConnectStateList.forEach {
                        it.onFailure("GlassConnect connect timeout 30000ms")
                    }
                    isConnecting = false
                }
            }
        }
    }

    init {
        val mIntentFilter = IntentFilter()
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        context.registerReceiver(this, mIntentFilter)
    }

    fun reset() {
        isConnect = false
        isConnecting = false
        isFindDevice = false
    }

    fun isTimeOut(): Boolean {
        return !myHandler.hasMessages(MSG_TIMEOUT)
    }

    @SuppressLint("MissingPermission")
    fun connect(macAddress: String, ip: String, port: Int) {
        isFindDevice = false
        isConnecting = false
        deviceAddress = macAddress
        deviceWifiIp = ip
        deviceWifiPort = port
        myHandler.removeMessages(MSG_TIMEOUT)
        myHandler.sendEmptyMessageDelayed(MSG_TIMEOUT, TIMEOUT)
        discoverPeers()
    }

    fun disConnect() {
        if (isConnecting) {
            isConnecting = false
            wifiP2pManager.cancelConnect(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.d(TAG, "wifiP2pManager disConnect onSuccess")
                }

                override fun onFailure(p0: Int) {
                    Log.d(TAG, "wifiP2pManager disConnect onFailure")
                }
            })
        } else if (isConnect) {
            isConnect = false
            removeGroup()
        }
    }

    fun removeGroup() {
        wifiP2pManager.removeGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "wifiP2pManager removeGroup onSuccess")
            }

            override fun onFailure(p0: Int) {
                Log.d(TAG, "wifiP2pManager removeGroup onFailure")
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun discoverPeers() {
        isFindDevice = false
        wifiP2pManager.requestConnectionInfo(channel, object : ConnectionInfoListener {
            override fun onConnectionInfoAvailable(wifiP2pInfo: WifiP2pInfo?) {
                Log.d(
                    TAG,
                    "connect onConnectionInfoAvailable: ${wifiP2pInfo?.groupOwnerAddress?.hostAddress} $isConnect"
                )
                if (wifiP2pInfo?.groupOwnerAddress?.hostAddress != null) {
                    myHandler.removeMessages(MSG_TIMEOUT)
                    isConnect = true
                    if (!isAttemptToConnect && deviceWifiPort != 0) {
                        EasyNetManager.getInstance().startClient(deviceWifiIp, deviceWifiPort)
                    }
                    if (isAttemptToConnect) {
                        isAttemptToConnect = false
                    }
                    EasyNetManager.getInstance().wifiP2pConnectStateList.forEach {
                        it.onSuccess()
                    }
                } else {
                    wifiP2pManager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
                        @RequiresApi(Build.VERSION_CODES.Q)
                        override fun onSuccess() {
                            Log.d(TAG, "GlassConnect discoverPeers onSuccess")
                        }

                        override fun onFailure(reason: Int) {
                            Log.d(TAG, "GlassConnect discoverPeers onFailure: reason = $reason")
                            isConnecting = false
                            myHandler.removeMessages(MSG_TIMEOUT)
                            EasyNetManager.getInstance().wifiP2pConnectStateList.forEach {
                                it.onFailure("GlassConnect discoverPeers onFailure: reason = $reason")
                            }
                        }
                    })
                }
            }
        })
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun connectDevice(address: String) {
        isConnecting = true
        val config = WifiP2pConfig()
        config.deviceAddress = address
        wifiP2pManager.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "GlassConnect connect onSuccess: ")
            }

            override fun onFailure(reason: Int) {
                isConnecting = false
                myHandler.removeMessages(MSG_TIMEOUT)
                EasyNetManager.getInstance().wifiP2pConnectStateList.forEach {
                    it.onFailure("GlassConnect connect onFailure")
                }
                Log.d(TAG, "GlassConnect connect onFailure")
            }
        })
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestPeers() {
        wifiP2pManager.requestPeers(channel, object : PeerListListener {
            override fun onPeersAvailable(wifiP2pDeviceList: WifiP2pDeviceList?) {
                if (wifiP2pDeviceList?.deviceList?.isNotEmpty() != true) {
                    Log.d(TAG, "GlassConnect onPeersAvailable: PeersAvailable is Empty")
                    return
                } else {
                    wifiP2pDeviceList.deviceList?.forEach {
                        Log.d(
                            TAG,
                            "GlassConnect onPeersAvailable deviceAddress =  ${it.deviceAddress}"
                        )
                        if (it.deviceAddress == this@WifiP2pConnect.deviceAddress) {
                            isFindDevice = true
                            if (!isConnecting && !isConnect) {
                                connectDevice(it.deviceAddress)
                            }
                            return@forEach
                        }
                    }
                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onReceive(context: Context?, intent: Intent?) {
        val state = intent?.action
        Log.d(TAG, "GlassConnect onReceive: $state")
        when (state) {
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                val networkInfo: NetworkInfo? =
                    intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO)
                Log.d(
                    TAG,
                    "WIFI_P2P_CONNECTION_CHANGED_ACTION  networkInfo.isConnected() = ${(networkInfo != null && networkInfo.isConnected())} isConnect = $isConnect isAttemptToConnect =$isAttemptToConnect"
                )
                if (networkInfo != null && networkInfo.isConnected() && deviceWifiPort != 0) {
                    if (!isConnect) {
                        isConnect = true
                        isConnecting = false
                        myHandler.removeMessages(MSG_TIMEOUT)
                        EasyNetManager.getInstance().wifiP2pConnectStateList.forEach {
                            it.onSuccess()
                        }
                        if (!isAttemptToConnect) {
                            EasyNetManager.getInstance().startClient(deviceWifiIp, deviceWifiPort)
                        }
                        isAttemptToConnect = false
                    }
                } else {
                    if (isConnect) {
                        isConnect = false
                    }
                }
            }

            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                if (!isFindDevice) {
                    requestPeers()
                }
            }
        }
    }
}