package dev.lazarev.calltasks.auth

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import dev.lazarev.calltasks.ui.LoginActivity

class Authenticator(private val context: Context) : AbstractAccountAuthenticator(context) {

    private val activityClass = LoginActivity::class.java

    override fun editProperties(response: AccountAuthenticatorResponse, accountType: String) = null

    override fun addAccount(
        response: AccountAuthenticatorResponse,
        accountType: String,
        authTokenType: String,
        requiredFeatures: Array<String>,
        options: Bundle
    ): Bundle {
        val intent = Intent(context, activityClass)
        intent.putExtra(ADDING_NEW_ACCOUNT_KEY, true)
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)
        return bundle
    }

    override fun confirmCredentials(response: AccountAuthenticatorResponse, account: Account, options: Bundle) = null

    override fun getAuthToken(
        response: AccountAuthenticatorResponse,
        account: Account,
        authTokenType: String,
        options: Bundle
    ): Bundle {

        val am = AccountManager.get(context)
        val result = Bundle()
        result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
        result.putString(AccountManager.KEY_ACCOUNT_TYPE, context.packageName)

        val intent = Intent(context, activityClass)
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        intent.putExtra(ADDING_NEW_ACCOUNT_KEY, false)
        result.putParcelable(AccountManager.KEY_INTENT, intent)
        return result
    }

    override fun getAuthTokenLabel(authTokenType: String) = null

    override fun updateCredentials(
        response: AccountAuthenticatorResponse,
        account: Account,
        authTokenType: String,
        options: Bundle
    ) = null

    override fun hasFeatures(response: AccountAuthenticatorResponse, account: Account, features: Array<String>) = null

    companion object {
        const val ADDING_NEW_ACCOUNT_KEY = "adding_new_account"
    }
}
