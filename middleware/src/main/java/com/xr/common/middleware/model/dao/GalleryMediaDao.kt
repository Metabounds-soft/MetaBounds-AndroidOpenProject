package com.xr.common.middleware.model.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.xr.common.middleware.model.bean.GalleryMediaData

@Dao
interface GalleryMediaDao {
    @Insert
    fun insert(metadata: GalleryMediaData)

    @Query("DELETE  FROM GalleryMediaData WHERE appSavePath IN (:paths)")
    fun deleteByAppSavePath(paths: List<String>)

    @Query("SELECT *  FROM GalleryMediaData WHERE appSavePath IN (:paths)")
    fun queryByAppSavePath(paths: List<String>): List<GalleryMediaData>

    @Query("SELECT *  FROM GalleryMediaData WHERE userId = :userId ORDER BY timestamp DESC")
    fun queryAllWithUserId(userId: Long): List<GalleryMediaData>
}