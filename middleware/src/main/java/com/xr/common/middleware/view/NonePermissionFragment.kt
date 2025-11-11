package com.xr.common.middleware.view

import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData

class NonePermissionFragment : Fragment() {
    private var launcher: ActivityResultLauncher<Array<String>>? = null
    private var mArray: Array<String>? = null
    val liveData: MutableLiveData<Map<String, Boolean>> = MutableLiveData()

    companion object {
        const val TAG = "NonePermissionFragment"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launcher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            var result = false
            if (it.isEmpty()) {
                return@registerForActivityResult
            }
            mArray?.forEach { ma ->
                it.forEach { (t, u) ->
                    if (t == ma) {
                        result = true
                    }
                }
            }
            if (result) {
                liveData.postValue(it)
            }
        }
    }


    fun requestPermission(array: Array<String>) {
        this.mArray = array
        launcher?.launch(array)
    }
}