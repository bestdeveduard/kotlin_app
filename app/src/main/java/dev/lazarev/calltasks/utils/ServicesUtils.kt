package dev.lazarev.calltasks.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE


fun Context.isMyServiceRunning(serviceClass: Class<*>): Boolean {
    val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}