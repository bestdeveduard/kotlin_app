package dev.lazarev.calltasks.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import dev.lazarev.calltasks.R
import dev.lazarev.calltasks.app.App
import dev.lazarev.calltasks.data.network.CallTasksNetworkManager
import dev.lazarev.calltasks.data.network.Resource
import dev.lazarev.calltasks.data.network.UserCredentials
import dev.lazarev.calltasks.utils.*
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), AuthenticateListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        sign_in.setOnClickListener {
            signIn(login.text?.toString() ?: "", password.text?.toString() ?: "", true)
        }

        password.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(
                        v.windowToken,
                        0
                )
                sign_in.performClick()
            }
            true
        }
        val account = AuthUtils.getAccount(this)
        AuthUtils.authenticate(this, account, this)
    }

    private fun signIn(login: String, password: String, saveAccount: Boolean) {
        showProgress()
        val body = Resource(listOf(UserCredentials(login, password)))
        CallTasksNetworkManager.create().signIn(body).callback {
            onSuccess {
                onAuthSucceeded(dev.lazarev.calltasks.data.account.UserCredentials(login, password), saveAccount)
                hideProgress()
            }
            onFailure { _, _ ->
                hideProgress()
                toast("Invalid login or password")
            }

            onException {
                hideProgress()
                toast("Can not connect. Check your internet connection")
            }
        }
    }

    private fun onAuthSucceeded(credentials: dev.lazarev.calltasks.data.account.UserCredentials, saveAccount: Boolean) {
        if (saveAccount) AuthUtils.addAccount(this, credentials)
        App.INSTANCE.userCredentials = credentials
        switchToMainScreen()
    }

    override fun onSuccessSignIn(credentials: dev.lazarev.calltasks.data.account.UserCredentials) {
        signIn(credentials.login, credentials.password, true)
    }

    override fun onFailSignIn() {
        hideProgress()
    }

    private fun switchToMainScreen() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun hideProgress() {
        overlay.visibility = View.GONE
    }

    private fun showProgress() {
        overlay.visibility = View.VISIBLE
    }

}