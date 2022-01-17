package dev.lazarev.calltasks.broadcast

import android.content.Context
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log


open class CallStateListener(private val context: Context) : PhoneStateListener() {

    companion object {
        private val TAG = CallStateListener::class.java.simpleName
        var lastState = -2
        var isIncoming = false
    }

    override fun onCallStateChanged(state: Int, phoneNumber: String?) {
        onCallStateChanged(context, state)
    }

    private fun onCallStateChanged(context: Context, state: Int) {
        if (lastState == state) {
            //No change, debounce extras
            return
        }
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                isIncoming = true
                onIncomingCallReceived(context)
            }
            TelephonyManager.CALL_STATE_OFFHOOK ->
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false
                    onOutgoingCallStarted(context)
                } else {
                    isIncoming = true
                    onIncomingCallAnswered(context)
                }
            TelephonyManager.CALL_STATE_IDLE ->
                when {
                    lastState == TelephonyManager.CALL_STATE_RINGING -> onMissedCall(context)
                    isIncoming -> onIncomingCallEnded(context)
                    else -> onOutgoingCallEnded(context)
                }
        }
        lastState = state
    }

    protected open fun onOutgoingCallEnded(context: Context) {
        Log.d(TAG, "onOutgoingCallEnded")
    }

    protected open fun onIncomingCallEnded(context: Context) {
        Log.d(TAG, "onIncomingCallEnded")
    }

    protected open fun onMissedCall(context: Context) {
        Log.d(TAG, "onMissedCall")
    }

    protected open fun onIncomingCallAnswered(context: Context) {
        Log.d(TAG, "onIncomingCallAnswered")
    }

    protected open fun onOutgoingCallStarted(context: Context) {
        Log.d(TAG, "onOutgoingCallStarted")
    }

    protected open fun onIncomingCallReceived(context: Context) {
        Log.d(TAG, "onIncomingCallReceived")
    }
}