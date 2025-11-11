package com.xr.common.middleware.easynet.listener

import android.util.Log
import com.metabounds.easynet.auxiliary.EasynetStatus
import com.metabounds.easynet.listener.EasynetClientLister
import com.xr.common.middleware.easynet.manager.EasyNetManager
import com.metabounds.easynet.option.EasynetOption
import com.metabounds.easynet.option.Option
import com.metabounds.easynet.session.EasynetSession
import com.metabounds.netprotocol.message.Message

class MetaEasyNetClientListener : EasynetClientLister {
    companion object {
        private const val TAG = "EasyNetClientListener"
    }

    override fun onConnectSuccess(option: EasynetOption) {
        val message = StringBuilder()
        message.append("Server connection successful").append("\n")
        message.append("local channel: ").append(option.get<String>(Option.LOCAL_IP_ADDR))
            .append(":").append(
                option.get<Int>(Option.LOCAL_IP_PORT)
            ).append("\n")
        message.append("remote: ").append(option.get<String>(Option.REMOTE_IP_ADDR)).append(":")
            .append(
                option.get<Int>(
                    Option.REMOTE_IP_PORT
                )
            )
        Log.d(TAG, " $message")
        EasyNetManager.getInstance().easyNetConnectStateList.forEach {
            it.onConnectSuccess(option)
        }
    }

    override fun onConnectFailure(status: EasynetStatus) {
        Log.d(TAG, " Server connection failed: ${status.details()}")
        EasyNetManager.getInstance().easyNetConnectStateList.forEach {
            it.onConnectFailure(status)
        }
    }

    override fun onSessionActive(session: EasynetSession) {
        EasyNetManager.getInstance().setSession(session)
        val message = StringBuilder()
        message.append("Session channel activation").append("\n")
        message.append("local channel: ").append(session.option<String>(Option.LOCAL_IP_ADDR))
            .append(":").append(
                session.option<Int>(
                    Option.LOCAL_IP_PORT
                )
            ).append("\n")
        message.append("Remote channel: ").append(session.option<String>(Option.REMOTE_IP_ADDR))
            .append(":").append(
                session.option<Int>(
                    Option.REMOTE_IP_PORT
                )
            )
        Log.d(TAG, " $message")
        EasyNetManager.getInstance().easyNetConnectStateList.forEach {
            it.onSessionActive(session)
        }
    }

    override fun onSessionInactive(status: EasynetStatus) {
        EasyNetManager.getInstance().setSession(null)
        Log.d(TAG, " Session channel failure: ${status.details()}")
        EasyNetManager.getInstance().easyNetConnectStateList.forEach {
            it.onSessionInactive(status)
        }
    }

    override fun onMessageArrived(msg: Message) {
        Log.d(TAG, " new message: $msg")
        EasyNetManager.getInstance().easyNetMessageArriveList.forEach {
            it.onMessageArrived(msg)
        }
    }

}