package dev.lazarev.calltasks.utils

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.AuthenticatorException
import android.accounts.OperationCanceledException
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import dev.lazarev.calltasks.data.account.UserCredentials
import java.io.IOException
import kotlin.concurrent.thread

class AuthUtils {

    companion object {
        fun addAccount(context: Context, credentials: UserCredentials) {
            val account = Account(credentials.login, getAccountType(context))

            val userDataBundle = Bundle()
            userDataBundle.putString(UserCredentials.LOGIN_KEY, credentials.login)
            userDataBundle.putString(UserCredentials.PASSWORD_KEY, credentials.password)

            //add account
            val accountManager = AccountManager.get(context)

            val password = CryptUtils.encrypt(context, CryptUtils.AUTH_ALIAS, credentials.password)
            if (password != null) {
                val result = accountManager.addAccountExplicitly(account, password, userDataBundle)
                if (!result) {
                    val existAccount = getAccount(context)
                    if (existAccount != null) {
                        accountManager.setPassword(existAccount, password)
                        accountManager.setUserData(existAccount, UserCredentials.LOGIN_KEY, credentials.login)
                    }
                }

                val responseBundle = Bundle()
                responseBundle.putString(AccountManager.KEY_PASSWORD, password)
                responseBundle.putString(AccountManager.KEY_ACCOUNT_NAME, credentials.login)
                responseBundle.putString(AccountManager.KEY_ACCOUNT_TYPE, getAccountType(context))
            }
        }


        fun authenticate(context: Context, account: Account?, listener: AuthenticateListener?) {
            val pListener: AuthenticateListener
            if (listener == null) {
                pListener = AuthenticateListener.Impl()
            } else {
                pListener = listener
            }

            val accountManager = AccountManager.get(context)

            if (account == null) {
                pListener.onFailSignIn()
                return
            }


            thread {
                try {
                    val result = accountManager.getAuthToken(
                        account, getAccountType(context),
                        Bundle(), false, null, null
                    ).result
                    val errorMsg = result.getString(AccountManager.KEY_ERROR_MESSAGE)
                    if (errorMsg == null) {
                        val login = accountManager.getUserData(account, UserCredentials.LOGIN_KEY)
                        val password = accountManager.getUserData(account, UserCredentials.PASSWORD_KEY)
                        val credentials = UserCredentials(login, password)
                        Handler(Looper.getMainLooper()).post { pListener.onSuccessSignIn(credentials) }
                    } else {
                        throw AuthenticatorException("Auth result KEY ERROR MESSAGE: $errorMsg")
                    }
                } catch (e: OperationCanceledException) {
                    e.printStackTrace()
                    Handler(Looper.getMainLooper()).post { pListener.onFailSignIn() }
                } catch (e: IOException) {
                    e.printStackTrace()
                    Handler(Looper.getMainLooper()).post { pListener.onFailSignIn() }
                } catch (e: AuthenticatorException) {
                    e.printStackTrace()
                    Handler(Looper.getMainLooper()).post { pListener.onFailSignIn() }
                }
            }
        }

        fun doLogout(context: Context) {
            val account = getAccount(context)
            if (account != null) {
                AccountManager.get(context).removeAccount(account, null, null)
            }
        }

        private fun getAccountType(context: Context) = context.packageName


        fun getAccount(context: Context): Account? {
            val accountManager = AccountManager.get(context)
            val accounts = accountManager.getAccountsByType(getAccountType(context))
            return accounts.firstOrNull()
        }
    }

}


interface AuthenticateListener {
    fun onSuccessSignIn(credentials: UserCredentials)

    fun onFailSignIn()

    class Impl : AuthenticateListener {

        override fun onSuccessSignIn(credentials: UserCredentials) {}

        override fun onFailSignIn() {}
    }
}