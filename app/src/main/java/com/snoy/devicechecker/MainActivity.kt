package com.snoy.devicechecker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity() {

    private val tvRandomUUID: TextView by lazy { findViewById(R.id.tvRandomUUID) }
    private val btnGenRandomUUID: Button by lazy { findViewById(R.id.btnGenRandomUUID) }

    private val tvSSAID: TextView by lazy { findViewById(R.id.tvSSAID) }
    private val btnGenSSAID: Button by lazy { findViewById(R.id.btnGenSSAID) }

    private val tvAAID: TextView by lazy { findViewById(R.id.tvAAID) }
    private val btnGenAAID: Button by lazy { findViewById(R.id.btnGenAAID) }

    private val tvIMEI: TextView by lazy { findViewById(R.id.tvIMEI) }
    private val btnGenIMEI: Button by lazy { findViewById(R.id.btnGenIMEI) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        initUUID()
        initSSAID()
        initAAID()
        initIMEI()
    }

    private fun initIMEI() {
        btnGenIMEI.setOnClickListener { showIMEI() }
        showIMEI()
    }

    private fun showIMEI() {
        val permissionState =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
        if (permissionState != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                REQUEST_READ_PHONE_STATE
            )
        } else {
            val imei = getIMEI()
            tvIMEI.text = imei
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_READ_PHONE_STATE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showIMEI()
                } else if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, "permission denied!", Toast.LENGTH_SHORT).show()
                } else {
                    Log.w("RDTest", "grantResults[0]= ${grantResults[0]}")
                }
            }
        }
    }

    private fun getIMEI(): String {
        val imei: String
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        imei = try {
            Log.d("RDTest", "SDK_INT= ${Build.VERSION.SDK_INT}")
            if (Build.VERSION.SDK_INT >= 26) {
                telephonyManager.imei
            } else {
                telephonyManager.deviceId
            }
        } catch (e: SecurityException) { //API 29 (Android10)+
            Log.e("RDTest", "e= $e")
            ""
        }
        return imei
    }

    private fun initAAID() {
        btnGenAAID.setOnClickListener { showAAID() }
        showAAID()
    }

    private fun showAAID() {
        val aaidFlow = getAAID()
        lifecycleScope.launch {
            aaidFlow.collect(collector = {
                Log.d("RDTest", "aaidFlow.collect, it=$it")
                tvAAID.text = it
            })
        }
    }

    private val scope = CoroutineScope(Job() + Dispatchers.IO)
    private fun getAAID(): Flow<String> { //gms ver
        var aaid: String
        val aaidStateFlow: MutableStateFlow<String> = MutableStateFlow("")
        scope.launch {
            try {
                @Suppress("BlockingMethodInNonBlockingContext")
                val advertisingIdInfo = AdvertisingIdClient.getAdvertisingIdInfo(applicationContext)
                aaid = advertisingIdInfo.id ?: ""
                aaidStateFlow.value = aaid
            } catch (e: Exception) {
                Log.d("RDTest", "getAAID fail, e=$e")
            }
        }

        return aaidStateFlow
    }

    private fun initSSAID() {
        btnGenSSAID.setOnClickListener { showSSAID() }
        showSSAID()
    }

    private fun showSSAID() {
        val ssaid = getSSAID()
        tvSSAID.text = ssaid
    }

    @SuppressLint("HardwareIds")
    private fun getSSAID(): String {
        val ssaid = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        Log.d("RDTest", "SSAID = $ssaid")
        return ssaid
    }

    private fun initUUID() {
        btnGenRandomUUID.setOnClickListener { showRandomUUID() }
        showRandomUUID()
    }

    private fun showRandomUUID() {
        val uuid = genRandomUUID()
        tvRandomUUID.text = uuid
    }

    private fun genRandomUUID(): String {
        val uuid = UUID.randomUUID().toString()
        Log.d("RDTest", "UUID = $uuid")
        return uuid
    }

    companion object {
        const val REQUEST_READ_PHONE_STATE = 1
    }
}