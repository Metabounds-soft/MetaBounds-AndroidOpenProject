package com.xr.common.middleware.model.data

import android.os.Process
import android.util.Log
import com.xr.common.middleware.model.bean.UserV2
import com.xr.common.middleware.model.dao.UserV2Dao
import com.xr.common.middleware.model.db.MetaDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class UserV2DBManager {

    private val mutex = Mutex()

    companion object {
        @Volatile
        private var user: UserV2? = null

        private val mInstance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            UserV2DBManager()
        }

        fun getInstance(): UserV2DBManager {
            return mInstance
        }

    }

    private fun getUserDao(): UserV2Dao {
        return MetaDatabase.getInstance().userV2Dao
    }


    suspend fun saveUser(userV2: UserV2) {
        mutex.withLock {
            Log.d(
                "TGA",
                "saveUser-------->${userV2.id}---->${this.hashCode()}-------->${Thread.currentThread().name}-------->${Process.myPid()}"
            )
            user = userV2
            if (getUserDao().getUserById(userV2.id) == null) {
                getUserDao().insert(userV2)
            } else {
                getUserDao().update(userV2)
            }
        }
    }

    suspend fun clearUser() {
        mutex.withLock {
            getUserDao().clear()
            user = null
        }
    }

    suspend fun getUser(): UserV2? {
        mutex.withLock {
            val temp = getUserDao().getUsers()
            if (temp.isNotEmpty()) {
                user = temp[0]
                return user
            }
            return null
        }
    }

    suspend fun updateUserInfo(
        userId: Long, username: String, nickname: String, headImg: String
    ) {
        user?.let {
            it.id = userId
            it.username = username
            it.nickname = nickname
            it.headerImg = headImg
            getUserDao().update(it)
        }
    }

    suspend fun updateNickname(nickname: String) {
        user?.let {
            it.nickname = nickname
            getUserDao().update(it)
        }
    }

    suspend fun updateSex(gender: Int) {
        user?.let {
            it.gender = gender
            getUserDao().update(it)
        }
    }

    suspend fun updateWeight(weight: Int) {
        user?.let {
            it.weight = weight
            getUserDao().update(it)
        }
    }

    suspend fun updateHeight(height: Int) {
        user?.let {
            it.height = height
            getUserDao().update(it)
        }
    }

    suspend fun updateBirthday(birthday: String) {
        user?.let {
            it.birthday = birthday
            getUserDao().update(it)
        }
    }


    /**
     * 当用户为空的时候，先从mmvk获取，如果未获取到，再从数据库里获取
     * 一般用于启动页
     * @return
     */
    suspend fun getUserInfoBySuspend(): UserV2? {
        mutex.withLock {
            if (user == null || user?.id == 0L) {
                val temp = getUserDao().getUsers()
                if (temp?.isEmpty() == true) {
                    return null
                }
                user = temp?.get(0)
            }
            return user
        }
    }

    fun getUserId(): Int {
        synchronized(UserV2DBManager::class) {
            val time = System.currentTimeMillis()
            var isGet = false
            while (System.currentTimeMillis() - time < 1000 && ((user == null || user?.id == 0L))) {
                if (!isGet) {
                    isGet = true
                    MainScope().launch(Dispatchers.IO) {
                        getUserInfoBySuspend()
                    }
                }
            }
            val intId = user?.id?.toInt()
            val absId = intId?.let { Math.abs(it) } ?: 0
            Log.i(
                "TGA",
                "getUserId----->${user?.id}----->${intId}---->${absId}" + " | ${this.hashCode()}--->${Thread.currentThread().name}-------->${Process.myPid()}"
            )
            return absId
        }
    }

    fun getUserInfo2(): UserV2? {
        synchronized(UserV2DBManager::class) {
            val time = System.currentTimeMillis()
            var isGet = false
            while (System.currentTimeMillis() - time < 1000 && ((user == null || user?.id == 0L))) {
                if (!isGet) {
                    isGet = true
                    MainScope().launch(Dispatchers.IO) {
                        getUserInfoBySuspend()
                    }
                }
            }
            Log.d(
                "TGA",
                "getUserId----------------->${user?.id}-->${this.hashCode()}--->${Thread.currentThread().name}-------->${Process.myPid()}"
            )
            return user
        }
    }

    fun getUserInfo(): UserV2? {
        return user
    }
}