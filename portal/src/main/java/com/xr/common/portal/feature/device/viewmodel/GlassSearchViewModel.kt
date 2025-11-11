package com.xr.common.portal.feature.device.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.xr.common.middleware.network.KTBaseResponse
import com.xr.common.middleware.viewmodel.BaseViewModel
import com.xr.common.middleware.model.bean.DeviceTokenBean
import com.xr.common.middleware.model.bean.NewDevice
import com.xr.common.middleware.model.data.DeviceDBManager
import com.xr.common.middleware.model.data.UserV2DBManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.random.Random

class GlassSearchViewModel : BaseViewModel() {

    val deviceTokenObs = MutableLiveData<KTBaseResponse<DeviceTokenBean?>>()
    val bindDeviceObs = MutableLiveData<Boolean>()
    private val dbManager by lazy { DeviceDBManager.getInstance() }

    fun getDeviceToken() {
        viewModelScope.launch(Dispatchers.IO) {
            var mkey = System.currentTimeMillis() / 1000
            mkey = mkey and 0x00FFFFFFL
            val SE_KEY_PRIVATE = 0xE0.toByte()
            mkey = mkey or (SE_KEY_PRIVATE.toInt() shl 24).toLong()
            deviceTokenObs.postValue(KTBaseResponse(DeviceTokenBean(mkey.toString()), 200, ""))
        }
    }

    fun bindDevice(mac: String, name: String, deviceToken: String, type: String) {
        MainScope().launch {
            val it = NewDevice()
            it.mac_bt = mac
            it.mac_ble = mac
            it.model = type
            it.name = name
            it.token = deviceToken
            it.sequence = 1
            it.uid = UserV2DBManager.getInstance().getUserId().toLong()
            it.id = (Random.nextLong(100000000) + 1)
            Log.e("GlassSearchViewModel", "bindDevice ${Gson().toJson(it)}")
            dbManager.saveDevice(it)
            bindDeviceObs.postValue(true)
        }
    }

}