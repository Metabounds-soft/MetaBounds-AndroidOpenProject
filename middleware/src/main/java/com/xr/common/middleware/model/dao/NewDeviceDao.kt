package com.xr.common.middleware.model.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.xr.common.middleware.model.bean.NewDevice

@Dao
interface NewDeviceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)

    suspend fun insert(device: NewDevice)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(devices: List<NewDevice>)

    @Update
    suspend fun update(device: NewDevice)


    @Delete
    suspend fun delete(device: NewDevice)

    @Delete
    suspend fun delete(device: List<NewDevice>)

    @Query("SELECT * FROM device_info WHERE uid=:userid")
    fun getDevicesToLive(userid: Long): LiveData<List<NewDevice>>

    @Query("SELECT * FROM device_info")
    fun getDevicesToLive(): LiveData<List<NewDevice>>

    @Query("SELECT * FROM device_info")
    suspend fun getDevices(): List<NewDevice>

    @Query("SELECT * FROM device_info WHERE uid=:userid")
    suspend fun getDevicesByUser(userid: Long): List<NewDevice>

    @Query("SELECT * FROM device_info WHERE sync=2")
    suspend fun getDevicesBySync(): List<NewDevice>


    @Query("SELECT * FROM device_info WHERE sync=4")
    suspend fun getDevicesBySyncName(): List<NewDevice>

    @Query("SELECT * FROM device_info WHERE id=:id")
    fun getDevicesByIdToLive(id: Long): LiveData<NewDevice>


    @Query("SELECT * FROM device_info WHERE id=:id")
    suspend fun getDevicesById(id: Long): NewDevice?

    @Query("SELECT * FROM device_info WHERE mac_ble=:macBle")
    suspend fun getDevicesByBle(macBle: String): List<NewDevice>

    @Query("SELECT * FROM device_info WHERE mac_bt=:macBt")
    suspend fun getDevicesByBt(macBt: String): List<NewDevice>

    @Query("DELETE FROM device_info")
    suspend fun deleteAll()
}