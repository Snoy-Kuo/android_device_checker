package com.snoy.devicechecker

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    private val tvRandomUUID: TextView by lazy { findViewById(R.id.tvRandomUUID) }
    private val btnGenRandomUUID: Button by lazy { findViewById(R.id.btnGenRandomUUID) }

    private val tvSSAID: TextView by lazy { findViewById(R.id.tvSSAID) }
    private val btnGenSSAID: Button by lazy { findViewById(R.id.btnGenSSAID) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        initUUID()
        initSSAID()
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
}