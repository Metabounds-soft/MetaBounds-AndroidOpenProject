package com.xr.common.middleware.model.bean

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "user_v2", primaryKeys = ["id"], indices = [Index(value = ["id"], unique = true)]
)
@Parcelize
class UserV2 : Parcelable {
    @NonNull
    @ColumnInfo(name = "id")
    var id: Long = 12315L
    var username: String = ""
    var mobile: String? = ""
    var email: String? = ""
    var countryCode: String? = ""
    var headerImg: String? = ""
    var nickname: String? = ""
    var gender: Int = 0
    var height: Int = 0
    var weight: Int = 0
    var birthday: String? = ""
    var region: String? = ""
    var language: String? = ""
    var preferences: String? = ""
    var token: String? = ""
}