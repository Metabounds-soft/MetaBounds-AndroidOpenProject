package com.xr.common.middleware.model.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.xr.common.middleware.model.bean.NewDevice
import com.xr.common.middleware.model.db.MetaDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DeviceDBManager : Observer<List<NewDevice>> {
    private val mData = MutableLiveData<List<NewDevice>>()

    companion object {
        private val mInstance by lazy {
            DeviceDBManager().apply {
                initDevices()
            }
        }

        fun getInstance(): DeviceDBManager {
            return mInstance
        }

    }

    private val newDeviceDao by lazy {
        MetaDatabase.getInstance().newDeviceDao
    }


    fun saveDevice(newDevice: NewDevice) {
        GlobalScope.launch(Dispatchers.IO) {
//            val allData = newDeviceDao.getDevices().sortedBy {
//                it.sequence
//            }
//            var isHave = false
//            allData.forEach {
//                if (it.mac_ble == newDevice.mac_ble) {
//                    isHave = true
//                    it.sequence = 1
//                    it.token = newDevice.token
//                } else {
//                    it.sequence = 0
//                }
//            }
//            var sequence = 2
//            allData.forEach {
//                if (it.sequence != 1) {
//                    it.sequence = sequence
//                }
//                sequence++
//            }
//            if (!isHave) {
//                val data = ArrayList<NewDevice>()
//                newDevice.sequence = 1
//                data.add(newDevice)
//                data.addAll(allData)
//                Log.e("TGA", "saveDevice size 1:${data.size}")
            val datas =
                newDeviceDao.getDevicesByUser(UserV2DBManager.getInstance().getUserId().toLong())
            if (datas.isNotEmpty()) {
                val temp = datas.filter {
                    newDevice.uid == it.uid && it.mac_bt == newDevice.mac_bt
                }
                newDeviceDao.delete(temp)
            }
            var sequence = 2
            datas.forEach {
                it.sequence = sequence
                sequence++
            }
            val data = ArrayList<NewDevice>()
            data.add(newDevice)
            data.addAll(datas)
            newDeviceDao.insertList(data)
//            } else {
//                Log.e("TGA", "saveDevice size 2:${allData.size}")
//                newDeviceDao.insertList(allData)
//            }

        }
    }

    fun updateDevice(newDevice: NewDevice) {
        GlobalScope.launch(Dispatchers.IO) {
            newDeviceDao.update(newDevice)
        }
    }

    fun deleteDevice(newDevice: NewDevice) {
        GlobalScope.launch(Dispatchers.IO) {
            newDeviceDao.delete(newDevice)
        }
    }

    fun deleteDevice(newDevices: List<NewDevice>) {
        GlobalScope.launch(Dispatchers.IO) {
            newDeviceDao.delete(newDevices)
        }
    }

    fun saveDevices(newDevices: List<NewDevice>) {
        GlobalScope.launch(Dispatchers.IO) {
            newDeviceDao.insertList(newDevices)
        }
    }


    fun getDeviceToLive(): LiveData<List<NewDevice>> {
//        val userId = UserV2DBManager.getInstance().getUserId().toLong()
//        logD("TGA", "getDeviceToLive userId:${userId}")
        return mData
    }


    private fun initDevices() {
        newDeviceDao.getDevicesToLive().observeForever(this)
    }

    suspend fun getDevices(): List<NewDevice> {
        return newDeviceDao.getDevices()
    }

    suspend fun getDevicesByUserId(userId: Long): List<NewDevice> {
        return newDeviceDao.getDevicesByUser(userId)
    }

    fun clearDevices() {
        Log.e("TGA", "-------->clearDevices---->")
        GlobalScope.launch(Dispatchers.IO) {
            newDeviceDao.deleteAll()
        }

    }

    suspend fun saveDevicesBySuspend(newDevices: List<NewDevice>) {
        newDeviceDao.insertList(newDevices)
    }

    suspend fun clearDevicesBySuspend() {
        Log.e("TGA", "-------->clearDevices---->")
        newDeviceDao.deleteAll()
    }

    suspend fun getDeviceById(uid: Long): NewDevice? {
        return newDeviceDao.getDevicesById(uid)
    }


    suspend fun getDeviceByBle(macBle: String): List<NewDevice> {
        return newDeviceDao.getDevicesByBle(macBle)
    }

    suspend fun getDeviceByBt(macBt: String): List<NewDevice> {
        return newDeviceDao.getDevicesByBle(macBt)
    }

    suspend fun getDeviceByUserId() {
        val value = getDevices()
        val list = value.filter {
            Log.i(
                "DeviceDBManager", "device userId:${it.uid}; loginUserId:${
                    UserV2DBManager.getInstance().getUserId().toLong()
                }"
            )
            it.uid == UserV2DBManager.getInstance().getUserId().toLong()
        }
        mData.postValue(list)
    }

    override fun onChanged(value: List<NewDevice>) {
        val list = value.filter {
            Log.i(
                "DeviceDBManager", "device userId:${it.uid}; loginUserId:${
                    UserV2DBManager.getInstance().getUserId().toLong()
                }"
            )
            it.uid == UserV2DBManager.getInstance().getUserId().toLong()
        }
        mData.postValue(list)
    }

}