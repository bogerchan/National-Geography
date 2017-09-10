package me.boger.geographic.core

import android.app.Fragment
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import me.boger.geographic.util.Timber

/**
 * Created by BogerChan on 2017/7/11.
 */
open class NGFragment : Fragment() {

    private val mLocalReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent == null || context == null) {
                    Timber.w("Receive a local broadcast but context or intent is null!")
                    return
                }
                onLocalBroadcastReceive(intent.action, intent.extras)
            }

        }
    }
    private var hasRegisterLocalBroadcastReceiver = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLocalBroadcastIfNeed()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (hasRegisterLocalBroadcastReceiver) {
            NGBroadcastManager.unregisterLocalBroadcastReceiver(mLocalReceiver)
            hasRegisterLocalBroadcastReceiver = false
        }
    }

    private fun registerLocalBroadcastIfNeed() {
        val actions = getBroadcastReceiverAction()
        if (actions == null || actions.isEmpty()) {
            return
        }
        val intentFilter = IntentFilter()
        actions.forEach {
            intentFilter.addAction(it)
        }
        NGBroadcastManager.registerLocalBroadcastReceiver(mLocalReceiver, intentFilter)
        hasRegisterLocalBroadcastReceiver = true
    }

    protected fun sendLocalBroadcast(action: String, data: Bundle?) {
        val intent = Intent(action)
        if (data != null) {
            intent.putExtras(data)
        }
        NGBroadcastManager.sendLocalBroadcast(intent)
    }

    open protected fun onLocalBroadcastReceive(action: String, data: Bundle?) {

    }

    open protected fun getBroadcastReceiverAction(): Array<String>? = null

}