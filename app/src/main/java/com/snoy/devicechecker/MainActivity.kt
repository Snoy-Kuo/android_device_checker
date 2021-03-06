package com.snoy.devicechecker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaDrm
import android.media.UnsupportedSchemeException
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.creative.ipfyandroid.Ipfy
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.safetynet.SafetyNet
import com.google.firebase.installations.FirebaseInstallations
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*

class MainActivity : AppCompatActivity() {

    private val tvRandomUUID: TextView by lazy { findViewById(R.id.tvRandomUUID) }
    private val btnGenRandomUUID: Button by lazy { findViewById(R.id.btnGenRandomUUID) }

    private val tvSSAID: TextView by lazy { findViewById(R.id.tvSSAID) }
    private val btnGetSSAID: Button by lazy { findViewById(R.id.btnGetSSAID) }

    private val tvAAID: TextView by lazy { findViewById(R.id.tvAAID) }
    private val btnGetAAID: Button by lazy { findViewById(R.id.btnGetAAID) }

    private val tvIMEI: TextView by lazy { findViewById(R.id.tvIMEI) }
    private val btnGetIMEI: Button by lazy { findViewById(R.id.btnGetIMEI) }

    private val tvMAC: TextView by lazy { findViewById(R.id.tvMAC) }
    private val btnGetMAC: Button by lazy { findViewById(R.id.btnGetMAC) }

    private val tvIP: TextView by lazy { findViewById(R.id.tvIP) }
    private val btnGetIP: Button by lazy { findViewById(R.id.btnGetIP) }

    private val tvExIP: TextView by lazy { findViewById(R.id.tvExIP) }
    private val btnGetExIP: Button by lazy { findViewById(R.id.btnGetExIP) }

    private val tvFID: TextView by lazy { findViewById(R.id.tvFID) }
    private val btnGetFID: Button by lazy { findViewById(R.id.btnGetFID) }

    private val tvDRM: TextView by lazy { findViewById(R.id.tvDRM) }
    private val btnGetDRM: Button by lazy { findViewById(R.id.btnGetDRM) }

    private val tvSafetyNet: TextView by lazy { findViewById(R.id.tvSafetyNet) }
    private val btnGetSafetyNet: Button by lazy { findViewById(R.id.btnGetSafetyNet) }

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("RDTest", "SDK_INT= ${Build.VERSION.SDK_INT}")
        initUUID()
        initSSAID()
        initAAID()
        initIMEI()
        initMAC()
        initIP()
        initExIP()
        initFID()
        initDRM()
        initSafetyNet()
    }

    private fun initSafetyNet() {
        btnGetSafetyNet.setOnClickListener { showSafetyNet() }
        showSafetyNet()
    }

    private fun showSafetyNet() {
        val safetyNetFlow = getSafetyNet()
        lifecycleScope.launch {
            safetyNetFlow.collect(collector = {
                Log.d("RDTest", "SafetyNet = $it")
                tvSafetyNet.text = it
            })
        }
    }

    private fun getSafetyNet(): Flow<String> {

        var safetyNet: String
        val safetyNetFlow: MutableStateFlow<String> = MutableStateFlow("")

        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
            == ConnectionResult.SUCCESS
        ) {
            // The SafetyNet Attestation API is available.
            Log.d("RDTest", "The SafetyNet Attestation API is available")
            val apiKey = BuildConfig.API_KEY
            val reqNonceUUID = UUID.randomUUID() //TODO: reqNonce should fetch from App server.
            Log.d("RDTest", "reqNonceUUID= $reqNonceUUID")
            Log.d("RDTest", "reqNonceUUID encode= ${reqNonceUUID.toByteArray().base64Encode()}")
            SafetyNet.getClient(this).attest(reqNonceUUID.toByteArray(), apiKey)
                .addOnSuccessListener(this) {
                    // Indicates communication with the service was successful.
                    // Use response.getJwsResult() to get the result data.
                    Log.d("RDTest", "attest success.")
                    safetyNet = try {
                        val model = Gson().fromJson(
                            extractJwsData(it.jwsResult)?.let { it1 -> String(it1) },
                            SafetyNetApiModel::class.java
                        )
                        Log.d("RDTest", "model.nonce= ${model.nonce}")
                        model.nonce?.let {
                            val byteArrayNonce = model.nonce!!.base64Decode()
                            val uuidNonce = byteArrayNonce.toUuid()
                            Log.d("RDTest", "model.nonce decode= $uuidNonce")
                        }
                        model.nonce ?: ""
                    } catch (e: Exception) {
                        Log.d("RDTest", "e= $e")
                        ""
                    }
                    //TODO: response jwsResult should send to App server for validation.

                    safetyNetFlow.value = safetyNet
                }
                .addOnFailureListener(this) { e ->
                    // An error occurred while communicating with the service.
                    if (e is ApiException) {
                        // An error with the Google Play services API contains some
                        // additional details.
                        Log.d("RDTest", "apiException: $e")
                        // You can retrieve the status code using the
                        // apiException.statusCode property.
                    } else {
                        // A different, unknown type of error occurred.
                        Log.d("RDTest", "Error: " + e.message)
                    }
                    safetyNet = ""
                    safetyNetFlow.value = safetyNet
                }

        } else {
            // Prompt user to update Google Play services.
            Log.d("RDTest", "The SafetyNet Attestation API is NOT available")
            safetyNet = ""
            safetyNetFlow.value = safetyNet
        }

        return safetyNetFlow
    }

    private fun extractJwsData(jws: String?): ByteArray? {
        val parts = jws?.split("[.]".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()
        if (parts?.size != 3) {
            Log.d(
                "RDTest",
                "Failure: Illegal JWS signature format. The JWS consists of "
                        + parts?.size + " parts instead of 3."
            )
            return null
        }
        return Base64.decode(parts[1], Base64.DEFAULT)
    }

    private fun initDRM() {
        btnGetDRM.setOnClickListener { showDRM() }
        showDRM()
    }

    private fun showDRM() {
        val drm = getDRM(DrmProviderUuid.WIDEVINE_UUID)
        tvDRM.text = drm
    }

    private fun getDRM(providerUUID: UUID): String {
        val byDrmId: ByteArray = try {
            MediaDrm(providerUUID).getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)
        } catch (e: UnsupportedSchemeException) {
            Log.d("RDTest", "getDRM exception e= $e")
            byteArrayOf()
        }
        val strDrmId = byDrmId.toHex()
        Log.d("RDTest", "DRM = $strDrmId, byte len=${byDrmId.size}")
        return strDrmId
    }

    private fun initFID() {
        btnGetFID.setOnClickListener { showFID() }
        showFID()
    }

    private fun showFID() {
        val fidFlow = getFID()
        lifecycleScope.launch {
            fidFlow.collect(collector = {
                Log.d("RDTest", "FID = $it")
                tvFID.text = it
            })
        }
    }

    private fun getFID(): Flow<String> {
        var fid: String
        val fidStateFlow: MutableStateFlow<String> = MutableStateFlow("")

        FirebaseInstallations.getInstance().id.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                fid = task.result?.toString() ?: ""
                fidStateFlow.value = fid
            } else {
                Log.e("RDTest", "Unable to get Installation ID")
            }
        }

        return fidStateFlow
    }

    private fun initExIP() {
        btnGetExIP.setOnClickListener { showExIP() }
        showExIP()
    }

    private fun showExIP() {
        val exipFlow = getExIP()
        lifecycleScope.launch {
            exipFlow.collect(collector = {
                Log.d("RDTest", "ExIP = $it")
                tvExIP.text = it
            })
        }
    }

    private fun getExIP(): Flow<String> {
        var exip: String
        val exipStateFlow: MutableStateFlow<String> = MutableStateFlow("")

        Ipfy.getInstance().getPublicIpObserver().observe(this) { ipData ->
            exip = ipData.currentIpAddress
                ?: "" // this is a value which is your current public IP address, null if no/lost internet connection
            exipStateFlow.value = exip
            //            ipData.lastStoredIpAddress // this is a previous IP address while network lost/reconnected and current IP address assigned to null/new one
        }

        return exipStateFlow
    }

    private fun initIP() {
        btnGetIP.setOnClickListener { showIP() }
        showIP()
    }

    private fun showIP() {
        val ip = getIP()
        tvIP.text = ip
    }

    private fun getIP(): String {
        try {
            val all = NetworkInterface.getNetworkInterfaces() ?: return ""
            val allList = Collections.list(all)
            Log.d("RDTest", "nif count= ${allList.size}")
            var ipStr = ""
            var lastNotEmpty = ""
            var lastNotEmptyNif = ""
            for (nif in allList) {
                if (NetworkInterface.getByName(nif.name) == null) {
                    continue
                }
                val addrs: List<InetAddress> = Collections.list(nif.inetAddresses)
                var tmpIPStr = ""
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        tmpIPStr = addr.hostAddress ?: ""
                        if (tmpIPStr.isNotEmpty()) {
                            lastNotEmpty = tmpIPStr
                            lastNotEmptyNif = nif.name
                        }
                    }
                }
                Log.d("RDTest", "[${nif.name}] => $tmpIPStr")
                if (nif.name.contains("eth0", ignoreCase = true)) {
                    ipStr = "[${nif.name}] => $tmpIPStr"
                } else if (nif.name.contains("wlan0", ignoreCase = true)) {
                    ipStr = "[${nif.name}] => $tmpIPStr"
                }
            }
            if (ipStr.isEmpty()) {
                ipStr = "[$lastNotEmptyNif] => $lastNotEmpty"
            }
            Log.d("RDTest", "IP = $ipStr")
            return ipStr
        } catch (e: Exception) {
            Log.e("RDTest", "e= $e")
            return ""
        }
    }

    private fun initMAC() {
        btnGetMAC.setOnClickListener { showMAC() }
        showMAC()
    }

    private fun showMAC() {
        val mac = getMAC()
        tvMAC.text = mac
    }

    private fun getMAC(): String {
        if (Build.VERSION.SDK_INT >= 30) {
            return ""
        } else {
            try {
                val all = NetworkInterface.getNetworkInterfaces() ?: return ""
                val allList = Collections.list(all)
                Log.d("RDTest", "nif count= ${allList.size}")
                var macStr = ""
                var lastNotEmpty = ""
                var lastNotEmptyNif = ""
                for (nif in allList) {
                    if (NetworkInterface.getByName(nif.name) == null) {
                        continue
                    }
                    val macBytes: ByteArray? = nif.hardwareAddress
                    val tmpMacStr = macBytes?.toHex(":") ?: "" //30
                    if (tmpMacStr.isNotEmpty()) {
                        lastNotEmpty = tmpMacStr
                        lastNotEmptyNif = nif.name
                    }
                    Log.d("RDTest", "[${nif.name}] => $tmpMacStr")
                    if (nif.name.contains("eth0", ignoreCase = true)) {
                        macStr = "[${nif.name}] => $tmpMacStr"
                    } else if (nif.name.contains("wlan0", ignoreCase = true)) {
                        macStr = "[${nif.name}] => $tmpMacStr"
                    }
                }
                if (macStr.isEmpty()) {
                    macStr = "[$lastNotEmptyNif] => $lastNotEmpty"
                }
                Log.d("RDTest", "MAC = $macStr")
                return macStr
            } catch (e: Exception) {
                Log.e("RDTest", "e= $e")
                return ""
            }
        }
    }

    private fun initIMEI() {
        btnGetIMEI.setOnClickListener { showIMEI() }
        showIMEI()
    }

    private fun showIMEI() {
        val imei = getIMEI()
        tvIMEI.text = imei
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

    @SuppressLint("MissingPermission", "HardwareIds")
    private fun getIMEI(): String {
        if (Build.VERSION.SDK_INT >= 29) {
            return ""
        } else {
            val permissionState =
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
            if (permissionState != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_PHONE_STATE),
                    REQUEST_READ_PHONE_STATE
                )
                return ""
            } else {
                val imei: String
                val telephonyManager =
                    getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                imei = try {
                    if (Build.VERSION.SDK_INT >= 26) {
                        telephonyManager.imei
                    } else {
                        @Suppress("DEPRECATION")
                        telephonyManager.deviceId
                    }
                } catch (e: SecurityException) { //API 29 (Android10)+
                    Log.e("RDTest", "e= $e")
                    ""
                }
                Log.d("RDTest", "IMEI = $imei")
                return imei
            }
        }
    }

    private fun initAAID() {
        btnGetAAID.setOnClickListener { showAAID() }
        showAAID()
    }

    private fun showAAID() {
        val aaidFlow = getAAID()
        lifecycleScope.launch {
            aaidFlow.collect(collector = {
                Log.d("RDTest", "AAID = $it")
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

    private fun initSSAID() {
        btnGetSSAID.setOnClickListener { showSSAID() }
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