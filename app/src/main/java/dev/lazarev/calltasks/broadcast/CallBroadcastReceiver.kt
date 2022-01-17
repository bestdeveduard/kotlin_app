package dev.lazarev.calltasks.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager


abstract class CallBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private val TAG = CallBroadcastReceiver::class.java.simpleName
    }

    var phoneListener: CallStateListener? = null
    var telephony: TelephonyManager? = null

    override fun onReceive(context: Context, intent: Intent) {
        phoneListener = object : CallStateListener(context) {

            override fun onOutgoingCallStarted(context: Context) {
                super.onOutgoingCallStarted(context)
                postStatus("Dialing")
            }

            override fun onOutgoingCallEnded(context: Context) {
                super.onOutgoingCallEnded(context)
                postStatus("Call End"){
                    obtainNextTask()
                }
            }
        }
        telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephony!!.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE)
    }


    fun onDestroy() = telephony?.listen(phoneListener, PhoneStateListener.LISTEN_NONE)

    abstract fun postStatus(status: String, onStatusPosted: () -> Unit = {})

    abstract fun obtainNextTask()
}