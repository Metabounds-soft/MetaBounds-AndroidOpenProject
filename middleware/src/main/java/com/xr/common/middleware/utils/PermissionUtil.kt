package com.xr.common.middleware.utils

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.xr.common.middleware.view.NonePermissionFragment
import java.util.concurrent.ConcurrentHashMap

class PermissionUtil private constructor() {
    private val fragmentMap = ConcurrentHashMap<String, NonePermissionFragment>()
    private var isSend = false
    private val blockMap = HashMap<String, ArrayList<PermissionBean>>()

    companion object {

        private object SignHolder {
            val INSTANCE = PermissionUtil()
        }

        fun get(): PermissionUtil {
            return SignHolder.INSTANCE
        }


    }

    fun with(
        activity: FragmentActivity, permissions: Array<String>, block: (Boolean) -> Unit
    ) {
        activity.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    fragmentMap.remove(activity::class.java.simpleName)
                    blockMap.remove(activity::class.java.simpleName)
                }
            }
        })
        registerFragment(activity, permissions, block)
    }

    private fun registerFragment(
        activity: FragmentActivity, permissions: Array<String>, block: (Boolean) -> Unit
    ) {
        if (activity.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            return
        }
        val manager = activity.supportFragmentManager
        var fragment = manager.findFragmentByTag(NonePermissionFragment.Companion.TAG)
        isSend = false
        val keyName = activity::class.java.simpleName
        if (blockMap.containsKey(keyName)) {
            val result = isContains(keyName, permissions)
            if (!result) {
                blockMap[keyName]?.add(PermissionBean(permissions, block))
            }
        } else {
            val temp = ArrayList<PermissionBean>()
            temp.add(PermissionBean(permissions, block))
            blockMap[keyName] = temp
        }
        if (fragment == null) {
            fragment = fragmentMap[activity::class.java.simpleName]
            if (fragment == null) {
                fragment = NonePermissionFragment()
                fragmentMap[activity::class.java.simpleName] = fragment
                manager.beginTransaction().add(
                    fragment, NonePermissionFragment.Companion.TAG
                ).commitAllowingStateLoss()
                fragment.liveData.observe(activity) { map ->
                    doResult(keyName, map)
                }
            }
        } else {
            if (fragmentMap.size == 0 || fragmentMap[activity::class.java.simpleName] == null) {
                if (fragment is NonePermissionFragment) {
                    fragmentMap[activity::class.java.simpleName] = fragment
                    fragment.liveData.observe(activity) { map ->
                        doResult(keyName, map)
                    }
                }
            }

        }
        observable(activity, permissions)
    }

    private fun observable(
        activity: FragmentActivity, permissions: Array<String>
    ) {
        val fragment = fragmentMap[activity::class.java.simpleName]
        if (activity.supportFragmentManager.findFragmentByTag(NonePermissionFragment.Companion.TAG) == null) {
            fragment?.lifecycle?.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (event == Lifecycle.Event.ON_CREATE) {
                        fragment.requestPermission(permissions)
                    } else if (event == Lifecycle.Event.ON_DESTROY) {
                        fragmentMap.remove(activity::class.java.simpleName)
                        blockMap.remove(activity::class.java.simpleName)
                    }
                }
            })
        } else {
            fragment?.requestPermission(permissions)
        }
    }


    private fun isContains(keyName: String, arrays: Array<String>): Boolean {

        blockMap[keyName]?.forEach {
            if (arrays.size == it.permissions.size && arrays.toList()
                    .containsAll(it.permissions.toList())
            ) {
                return true
            }
        }
        return false
    }


    private fun doResult(keyName: String, map: Map<String, Boolean>) {
        if (isSend || map.isEmpty()) {
            return
        }
        isSend = true
        blockMap[keyName]?.forEach { bl ->
            bl.permissions.forEach { ps ->
                if (bl.permissions.size == map.size && map.containsKey(ps)) {
                    var result = true
                    map.forEach { item ->
                        if (!item.value) {
                            result = false
                        }
                    }
                    bl.block(result)
                    blockMap.remove(keyName)
                    return@doResult
                }
            }
        }
    }
}

private data class PermissionBean(
    val permissions: Array<String>, val block: (Boolean) -> Unit
)