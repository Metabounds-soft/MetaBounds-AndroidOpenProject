package com.xr.common.middleware.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionKT {
    val LOCATION_PERMISSION = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val READ_WRITE_PERMISSION = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
    )
    val READ_WRITE_PERMISSION_13 = arrayOf(
        Manifest.permission.READ_MEDIA_AUDIO,
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
    )
    val READ_PHONE_PERMISSION = arrayOf(
        Manifest.permission.READ_PHONE_STATE
    )

    private val permissionHeight = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_ADVERTISE
    )
    private val permissionLow =
        arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN).plus(
            LOCATION_PERMISSION
        )

    val LOCATION_LOW_PERMISSION = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val LOCATION_HIGH_PERMISSION = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )
    val bluetoothPermission by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionHeight
        } else {
            permissionLow
        }
    }

    val locationPermission by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            LOCATION_LOW_PERMISSION
        } else {
            LOCATION_HIGH_PERMISSION
        }
    }
}


fun Activity.checkLocation(isRequestPermission: Boolean, result: (Boolean) -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // 检查定位权限
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (isRequestPermission) {
                // 如果没有权限，请求权限
                ActivityCompat.requestPermissions(
                    this, PermissionKT.locationPermission, 0
                )
            }
            result.invoke(false)
        } else {
            // 已经有权限，可以进行定位操作
            result.invoke(true)
        }
    } else {
        // 检查定位权限
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (isRequestPermission) {
                // 如果没有权限，请求权限
                ActivityCompat.requestPermissions(
                    this, PermissionKT.locationPermission, 0
                )
            }
            result.invoke(false)
        } else {
            // 已经有权限，可以进行定位操作
            result.invoke(true)
        }
    }
}