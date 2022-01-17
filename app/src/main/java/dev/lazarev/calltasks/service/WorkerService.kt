package dev.lazarev.calltasks.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.os.postDelayed
import dev.lazarev.calltasks.R
import dev.lazarev.calltasks.app.App
import dev.lazarev.calltasks.broadcast.CallBroadcastReceiver
import dev.lazarev.calltasks.data.network.AccountStatus
import dev.lazarev.calltasks.data.network.CallStatus
import dev.lazarev.calltasks.data.network.CallTasksNetworkManager
import dev.lazarev.calltasks.data.network.Resource
import dev.lazarev.calltasks.ui.MainActivity
import dev.lazarev.calltasks.utils.*
import java.util.concurrent.Executors
import java.util.concurrent.Future


class WorkerService : Service() {

    companion object {
        const val NOTIFICATION_ID = 290
        private val TAG = WorkerService::class.java.simpleName
    }

    private var callBroadcastReceiver: CallBroadcastReceiver? = null
    private val executor = Executors.newSingleThreadExecutor()
    private var future: Future<*>? = null

    override fun onCreate() {
        super.onCreate()
        App.INSTANCE.workerService = this
        val contentIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), 0)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager.getNotificationChannel("callTasks") == null) {

            val chan2 = NotificationChannel("callTasks", "callTasks", NotificationManager.IMPORTANCE_NONE)
            chan2.lockscreenVisibility = Notification.VISIBILITY_SECRET
            notificationManager.createNotificationChannel(chan2)
        }
        val builder = NotificationCompat.Builder(this, "callTasks")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setContentTitle("Call Tasks App")
                    .setContentText("Call Tasks")
                    .setAutoCancel(false)
                    .setSmallIcon(R.drawable.ic_phone)
                    .setContentIntent(contentIntent)
        } else {
            builder.setAutoCancel(true)
                    .setContentTitle("Call Tasks App")
                    .setContentText("Call Tasks")
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setSmallIcon(R.drawable.ic_phone)
                    .setContentIntent(contentIntent)
        }

        startForeground(NOTIFICATION_ID, builder.build())
        registerCallBroadcast()
    }

    private fun registerCallBroadcast() {
        callBroadcastReceiver = object : CallBroadcastReceiver() {
            override fun postStatus(status: String, onStatusPosted: () -> Unit) {
                val body = Resource(listOf(CallStatus(App.INSTANCE.userCredentials!!.login, status)))
                CallTasksNetworkManager.create().postCallStatus(body).fireAndWait {
                    onStatusPosted()
                }
            }

            override fun obtainNextTask() {
                completeTask()
            }
        }

        val filter = IntentFilter()
        filter.addAction("android.intent.action.PHONE_STATE")
        registerReceiver(callBroadcastReceiver, filter)
    }

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        changeAccountStatus("Online") {
            completeTask()
        }
        return START_STICKY
    }

    private fun changeAccountStatus(status: String, onAnyResult: () -> Unit = {}) {
        App.INSTANCE.userCredentials?.let {
            val body = Resource(listOf(AccountStatus(it.login, status)))
            CallTasksNetworkManager.create().postAccountStatus(body).fireAndWait {
                onAnyResult()
            }
        }
    }

    private fun completeTask() {
        try {
            future = executor.submit {
                val username = App.INSTANCE.userCredentials?.login ?: return@submit
                val tasksService = CallTasksNetworkManager.create()
                tasksService.getTask("username=$username").callback {
                    onSuccess {
                        it.resource.firstOrNull()?.let { task ->
                            if (task.make_call.equals("yes", true)) {
                                makeOutgoingCall(task.number)
                            } else {
                                completeTask()
                            }
                        } ?: completeTask()
                    }
                    onFailure { response, _ ->
                        Log.d(TAG, response?.string() ?: "")
                        scheduleTaskExecuting()
                    }
                    onException {
                        Log.d(TAG, it.message ?: "")
                        scheduleTaskExecuting()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun scheduleTaskExecuting(delayMillis: Long = 5.minutesToMillis()) {
        Handler().postDelayed(delayMillis) {
            completeTask()
        }
    }

    private fun makeOutgoingCall(number: String) {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:$number")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }


    fun stop() {
        callBroadcastReceiver?.let {
            try {
                it.onDestroy()
                unregisterReceiver(it)
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
        try {
            future?.cancel(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        changeAccountStatus("Offline")
    }
}