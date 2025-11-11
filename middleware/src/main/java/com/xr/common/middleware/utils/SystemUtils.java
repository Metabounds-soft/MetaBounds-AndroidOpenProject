package com.xr.common.middleware.utils;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;

public class SystemUtils {

    public static boolean isOpenLocService(final Context context) {
        boolean isGps = false; //推断GPS定位是否启动
        if (context != null) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                isGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            }
            if (isGps) {
                return true;
            }
        }
        return false;
    }

    public static void openLocService(final Context context) {
        final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}
