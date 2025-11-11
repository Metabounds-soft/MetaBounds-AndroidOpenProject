package com.xr.common.middleware.model.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.xr.common.middleware.model.bean.UserV2

@Dao
interface UserV2Dao {

    @Query("SELECT * FROM user_v2")
    suspend fun getUsers(): List<UserV2>

    @Insert
    suspend fun insert(user: UserV2)

    @Delete
    suspend fun delete(user: UserV2)


    @Query("SELECT * FROM user_v2 where id=:id")
    suspend fun getUserById(id: Long): UserV2?


    @Query("DELETE FROM user_v2")
    suspend fun clear()

    @Update
    suspend fun update(user: UserV2)


    @Query("SELECT * FROM user_v2")
    fun getUsersToLive(): LiveData<List<UserV2>>

}