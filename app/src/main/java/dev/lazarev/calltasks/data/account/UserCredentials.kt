package dev.lazarev.calltasks.data.account

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserCredentials(val login: String, val password: String): Parcelable {
    companion object {
        const val LOGIN_KEY = "login_key"
        const val PASSWORD_KEY = "password_key"
    }
}