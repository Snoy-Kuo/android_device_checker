package com.snoy.devicechecker

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        initUUID()
        initSSAID()
        initAAID()
    }

    private fun initAAID() {
        btnGenAAID.setOnClickListener { showAAID() }
        showAAID()
    }

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    private fun showAAID() {
        val aaidFlow = getAAID()
        lifecycleScope.launch {
            aaidFlow.collect(collector = {
                Log.d("RDTest", "aaidFlow.collect, it=$it")
                tvAAID.text = it
            })
        }
    }


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

//    private fun getAAID(): Flow<String> { //androidX ver
//        Log.d("RDTest", "getAAID")
//        var aaid = ""
//        if (AdvertisingIdClient.isAdvertisingIdProviderAvailable(this)) {
//            val advertisingIdInfoListenableFuture =
//                AdvertisingIdClient.getAdvertisingIdInfo(applicationContext)
//
//            addCallback(
//                advertisingIdInfoListenableFuture, object : FutureCallback<AdvertisingIdInfo> {
//                    override fun onSuccess(adInfo: AdvertisingIdInfo?) {
//                        Log.d("RDTest", "onSuccess")
//                        adInfo?.let {
//                            val id: String = adInfo.id
//                            val providerPackageName: String = adInfo.providerPackageName
//                            val isLimitAdTrackingEnabled: Boolean = adInfo.isLimitAdTrackingEnabled
//                            Log.d("RDTest", "AAID= $id")
//                            aaid = id
//                        }
//                    }
//
//                    override fun onFailure(t: Throwable) {
//                        Log.e(
//                            "RDTest",
//                            "Failed to connect to Advertising ID provider."
//                        )
//                        // Try to connect to the Advertising ID provider again, or fall
//                        // back to an ads solution that doesn't require using the
//                        // Advertising ID library.
//                    }
//                }, Executors.newSingleThreadExecutor()
//            )
//        } else {
//            // The Advertising ID client library is unavailable. Use a different
//            // library to perform any required ads use cases.
//            Log.d("RDTest", "!AdvertisingIdClient.isAdvertisingIdProviderAvailable")
//        }
//        return flowOf(aaid)
//    }

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
}