package com.dostonbek.complicatedscannerapp

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.*
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.ToggleButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint

class MainActivity : AppCompatActivity() {
    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var codeDetailsTextView: TextView
    private lateinit var wifiManager: WifiManager
    private lateinit var wifiStateReceiver: BroadcastReceiver
 lateinit var updateBtn: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
updateBtn=findViewById(R.id.update)
        barcodeView = findViewById(R.id.barcode_scanner)
        codeDetailsTextView = findViewById(R.id.wifiInfoTextView)

        val toggleTorchButton = findViewById<ToggleButton>(R.id.toggleTorchButton)
        toggleTorchButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                barcodeView.setTorchOn()
            } else {
                barcodeView.setTorchOff()
            }
        }




        val formats = listOf(
            BarcodeFormat.QR_CODE,
            BarcodeFormat.CODE_39,
            BarcodeFormat.CODE_128,
            BarcodeFormat.EAN_13
        )
        barcodeView.barcodeView.decoderFactory = DefaultDecoderFactory(formats)
        barcodeView.decodeContinuous(callback)

        if (allPermissionsGranted()) {
            barcodeView.resume()
        } else {
            requestPermissions.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }



        wifiStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val wifiState = intent?.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)
                if (wifiState == WifiManager.WIFI_STATE_ENABLED) {

                    barcodeView.resume()
                }
            }
        }
        registerReceiver(wifiStateReceiver, IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION))
    }

    private val callback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult?) {
            result?.let {
                barcodeView.pause()
                handleBarcodeResult(it.text)
updateBtn.setOnClickListener {
    barcodeView.resume()
    codeDetailsTextView.text=""
    barcodeView.setTorchOff()

}

            }
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.CAMERA] == true) {
                barcodeView.resume()
            } else {
                Log.e("MainActivity", "Camera permission denied")
            }
        }

    private fun allPermissionsGranted() = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ).all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun handleBarcodeResult(text: String) {
        when {
            text.startsWith("http://") || text.startsWith("https://") -> {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(text))
                startActivity(browserIntent)
                barcodeView.resume()

            }
            text.startsWith("WIFI:") -> {


            }
            else -> {
                codeDetailsTextView.text = "Code details: $text"
                codeDetailsTextView.visibility = View.VISIBLE
            }
        }
    }

    // Connect to a Wi-Fi network based on the provided Wi-Fi QR code



}
