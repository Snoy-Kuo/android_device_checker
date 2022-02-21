package com.snoy.devicechecker

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    private val setUUID: HashSet<String> = HashSet()
    private val tvRandomUUID: TextView by lazy { findViewById(R.id.tvRandomUUID) }
    private val btnGenRandomUUID: Button by lazy { findViewById(R.id.btnGenRandomUUID) }
    private val btnGenRandomUUIDUntilDuplicate: Button by lazy { findViewById(R.id.btnGenRandomUUIDUntilDuplicate) }
    private val handlerGenRandomUUIDUntilDuplicate: Handler = Handler(Looper.getMainLooper())
    private var continueGenUUID = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        initUUID()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopGenRandomUUID()
    }

    private fun initUUID() {
        btnGenRandomUUID.setOnClickListener { showRandomUUID() }
        btnGenRandomUUIDUntilDuplicate.setOnClickListener {
            if (btnGenRandomUUIDUntilDuplicate.text.equals("gen random UUID until duplicate")) {
                btnGenRandomUUIDUntilDuplicate.text = "stop gen random UUID"
                runGetRandomUUID()
            } else {
                btnGenRandomUUIDUntilDuplicate.text = "gen random UUID until duplicate"
                stopGenRandomUUID()
            }
        }
        showRandomUUID()
    }

    private fun stopGenRandomUUID() {
        handlerGenRandomUUIDUntilDuplicate.removeCallbacksAndMessages(null)
        continueGenUUID = false
    }

    private fun runGetRandomUUID() {
        continueGenUUID = true
        handlerGenRandomUUIDUntilDuplicate.postDelayed({
            repeat(500000) { genRandomUUID() }
            showRandomUUID()
        }, 1)
    }

    private fun showRandomUUID() {
        val uuid = genRandomUUID()
        tvRandomUUID.text = "$uuid\ncreated id count= ${setUUID.size}"
        if (continueGenUUID) {
            runGetRandomUUID()
        }
    }

    private fun genRandomUUID(): String {
        val uuid = UUID.randomUUID().toString()
        if (!continueGenUUID) {
            Log.d("RDTest", "UUID = $uuid, created id count= ${setUUID.size}")
        }
        if (setUUID.contains(uuid)) {
            Toast.makeText(
                this,
                "duplicated UUID = $uuid, created id count= ${setUUID.size}",
                Toast.LENGTH_SHORT
            ).show()
            Log.d("RDTest", "duplicated UUID = $uuid, created id count= ${setUUID.size}")
            continueGenUUID = false
        } else {
            setUUID.add(uuid)
        }
        return uuid
    }
}