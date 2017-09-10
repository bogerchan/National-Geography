package me.boger.geographic.core

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager

/**
 * Created by BogerChan on 2017/9/9.
 */
object NGBroadcastManager {

    private val APP_CATEGORY = "me.boger.geographic.category.DEFAULT"

    private val mLocalBroadcastManager by lazy {
        LocalBroadcastManager.getInstance(NGRumtime.application)
    }

    fun registerLocalBroadcastReceiver(receiver: BroadcastReceiver, filter: IntentFilter) {
        filter.addCategory(APP_CATEGORY)
        mLocalBroadcastManager.registerReceiver(receiver, filter)
    }

    fun unregisterLocalBroadcastReceiver(receiver: BroadcastReceiver) {
        mLocalBroadcastManager.unregisterReceiver(receiver)
    }

    fun sendLocalBroadcast(intent: Intent) {
        intent.addCategory(APP_CATEGORY)
        mLocalBroadcastManager.sendBroadcast(intent)
    }
}