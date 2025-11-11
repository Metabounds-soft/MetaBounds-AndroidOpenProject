package com.xr.common

import android.app.Application
import com.jeremyliao.liveeventbus.LiveEventBus
import com.tencent.mmkv.MMKV
import com.xr.common.middleware.base.BaseLibData
import com.xr.common.middleware.easynet.manager.EasyNetManager
import com.xr.common.middleware.manager.MBDeviceManager
import com.xr.common.middleware.model.db.MetaDatabase

/**
 * Description:
 * CreateDate:     2023/5/11 11:22
 * Author:         agg
 */
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        BaseLibData.application = this
        // 出于性能考虑，此处配置LiveEventBus相关参数
        LiveEventBus.config().apply {
            autoClear(true) // 配置在没有Observer关联的时候是否自动清除LiveEvent以释放内存（默认值false）
            enableLogger(false) // 配置是否打印日志（默认打印日志）
            // 配置LifecycleObserver（如Activity）接收消息的模式（默认值true）
            // true：整个生命周期（从onCreate到onDestroy）都可以实时收到消息;
            // false：激活状态（Started）可以实时收到消息，非激活状态（Stopped）无法实时收到消息，需等到Activity重新变成激活状态，方可收到消息
            lifecycleObserverAlwaysActive(false)
        }
        MMKV.initialize(this)
        MetaDatabase.init(this)
        MBDeviceManager.Companion.getInstance().setContext(this)
        EasyNetManager.getInstance().initApplication(this)
    }

}