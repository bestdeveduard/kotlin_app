package dev.lazarev.calltasks.auth

import android.accounts.AbstractAccountAuthenticator
import android.app.Service
import android.content.Intent
import android.os.IBinder

class AuthService: Service() {
    private var mAuthenticator: AbstractAccountAuthenticator? = null

    override fun onCreate() {
        super.onCreate()
        mAuthenticator = Authenticator(applicationContext)
    }

    override fun onBind(intent: Intent): IBinder? {
        return mAuthenticator?.iBinder
    }
}