package dev.lazarev.calltasks.app

import android.app.Application
import dev.lazarev.calltasks.data.account.UserCredentials
import com.crashlytics.android.Crashlytics
import dev.lazarev.calltasks.service.WorkerService
import io.fabric.sdk.android.Fabric



class App : Application() {

    companion object {
        lateinit var INSTANCE: App
    }

    var userCredentials: UserCredentials? = null
    var workerService: WorkerService? = null

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        Fabric.with(this, Crashlytics())
    }
}