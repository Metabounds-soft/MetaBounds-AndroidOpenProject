package com.xr.common.middleware.model.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.xr.common.middleware.model.bean.GalleryMediaData;
import com.xr.common.middleware.model.bean.NewDevice;
import com.xr.common.middleware.model.bean.UserV2;
import com.xr.common.middleware.model.dao.GalleryMediaDao;
import com.xr.common.middleware.model.dao.NewDeviceDao;
import com.xr.common.middleware.model.dao.UserV2Dao;

@Database(entities = {UserV2.class, NewDevice.class, GalleryMediaData.class}, version = 1, exportSchema = false)
public abstract class MetaDatabase extends RoomDatabase {
    private static volatile MetaDatabase INSTANCE;
    private static final String DM_NAME = "MetaDatabase.db";

    private static MetaDatabase create(final Context context) {
        return Room.databaseBuilder(context, MetaDatabase.class, DM_NAME).fallbackToDestructiveMigration().build();
    }

    public static synchronized MetaDatabase init(final Context context) {
        if (INSTANCE == null) INSTANCE = create(context);
        return INSTANCE;
    }

    public static synchronized MetaDatabase getInstance() {
        return INSTANCE;
    }

    public abstract UserV2Dao getUserV2Dao();

    /**
     * 设备信息
     *
     * @return
     */
    public abstract NewDeviceDao getNewDeviceDao();

    /**
     * 获取相册信息
     *
     * @return
     */
    public abstract GalleryMediaDao getGalleryMediaDao();

}
