package dev.lazarev.calltasks.ui

import android.Manifest.permission
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dev.lazarev.calltasks.R
import dev.lazarev.calltasks.app.App
import dev.lazarev.calltasks.data.network.CallTasksNetworkManager
import dev.lazarev.calltasks.data.network.Resource
import dev.lazarev.calltasks.data.network.UserCredentials
import dev.lazarev.calltasks.service.WorkerService
import dev.lazarev.calltasks.utils.AuthUtils
import dev.lazarev.calltasks.utils.fireAndForget
import dev.lazarev.calltasks.utils.isMyServiceRunning
import dev.lazarev.calltasks.utils.toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG: String = MainActivity::class.java.simpleName
        private const val REQUEST_PHONE_CALL = 123
    }


    override fun onResume() {
        super.onResume()
        val text = if (isMyServiceRunning(WorkerService::class.java)) "Stop Work" else "Start Work"
        button.text = text
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        App.INSTANCE.userCredentials?.let {
            supportActionBar?.title = "Logged in as ${it.login}"
        }

        button.setOnClickListener {
            if (isMyServiceRunning(WorkerService::class.java)) {
                stopWorker()
                button.text = "Start Work"
            } else {
                if (ContextCompat.checkSelfPermission(this, permission.CALL_PHONE) != PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, permission.READ_PHONE_STATE) != PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(permission.CALL_PHONE, permission.READ_PHONE_STATE), REQUEST_PHONE_CALL)
                } else {
                    startWorker()
                    button.text = "Stop Work"
                }
            }
        }
    }

    private fun stopWorker() = try {
        App.INSTANCE.workerService?.stop()
        val intent = Intent(this, WorkerService::class.java)
        stopService(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.logout -> performLogout()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun performLogout() {
        stopWorker()
        AuthUtils.doLogout(this)
        val intent = Intent(this, LoginActivity::class.java)
        App.INSTANCE.userCredentials = null
        startActivity(intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun startWorker() {
        val intent = Intent(this, WorkerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PHONE_CALL -> {
                if (grantResults.first() == PERMISSION_GRANTED) {
                    startWorker()
                } else {
                    toast("Permissions not granted!")
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}
