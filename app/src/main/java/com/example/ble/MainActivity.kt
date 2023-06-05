package com.example.ble

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ble.databinding.ActivityMainBinding

private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
private const val RUNTIME_PERMISSION_REQUEST_CODE = 2


class MainActivity : AppCompatActivity() {
    val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager =
            getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }


    val scanFilter = ScanFilter.Builder()
        .setDeviceAddress("00:1A:7D:DA:71:13")
        .build()

    val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            with(result.device) {
                Log.d(
                    "ScanCallback",
                    "Device name: ${name ?: "Unnamed"}, address: $address, rssi: ${result.rssi}"
                )
            }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully connected to $deviceAddress")
                    // TODO: Store a reference to BluetoothGatt
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w(
                        "BluetoothGattCallback",
                        "Successfully disconnected from $deviceAddress"
                    )
                    gatt.close()
                }
            } else {
                Log.w(
                    "BluetoothGattCallback",
                    "Error $status encountered for $deviceAddress! Disconnecting..."
                )
                gatt.close()
            }
        }
    }

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            startBleScan()
        }


    }

    @SuppressLint("MissingPermission")
    private fun promptEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ENABLE_BLUETOOTH_REQUEST_CODE -> {
                if (resultCode != Activity.RESULT_OK) {
                    promptEnableBluetooth()
                }
            }
        }

    }

    @SuppressLint("MissingPermission")
    private fun startBleScan() {
        if (!hasRequiredRuntimePermissions()) {
            requestRelevantRuntimePermissions()
        } else {
            bleScanner.startScan(null, scanSettings, scanCallback)
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopBleScan() {
        bleScanner.stopScan(scanCallback)
    }

    fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }

    fun Context.hasRequiredRuntimePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermission(Manifest.permission.BLUETOOTH_SCAN) &&
                    hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        }

    }

    private fun Activity.requestRelevantRuntimePermissions() {
        if (hasRequiredRuntimePermissions()) {
            return
        }
        when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S -> {
                requestLocationPermission()
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                requestBluetoothPermissions()
            }
        }
    }

    private fun requestLocationPermission() {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Location permission required")
                .setMessage(
                    "Starting from Android M (6.0), the system requires apps to be granted " +
                            "location access in order to scan for BLE devices."
                )
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok) { dialog, which ->
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        RUNTIME_PERMISSION_REQUEST_CODE
                    )
                }
                .show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestBluetoothPermissions() {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Bluetooth permissions required")
                .setMessage(
                    "Starting from Android 12, the system requires apps to be granted " +
                            "Bluetooth access in order to scan for and connect to BLE devices."
                )
                .setCancelable(false)

                .setPositiveButton(android.R.string.ok) { dialog, which ->
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ),
                        RUNTIME_PERMISSION_REQUEST_CODE
                    )

                }
                .show()

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RUNTIME_PERMISSION_REQUEST_CODE -> {
                val containsPermanentDenial = permissions.zip(grantResults.toTypedArray()).any {
                    it.second == PackageManager.PERMISSION_DENIED &&
                            !ActivityCompat.shouldShowRequestPermissionRationale(this, it.first)
                }
                val containsDenial = grantResults.any { it == PackageManager.PERMISSION_DENIED }
                val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                when {
                    containsPermanentDenial -> {
                        AlertDialog.Builder(this)
                            .setTitle("Permission Required")
                            .setMessage(
                                "You have permanently denied the required location permission. " +
                                        "Please manually grant the permission from the App Settings to continue."
                            )
                            .setPositiveButton("Go to Settings") { dialog, which ->
                                // Kullanıcıyı uygulama ayarlarına yönlendir
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri = Uri.fromParts("package", packageName, null)
                                intent.data = uri
                                startActivity(intent)
                            }
                            .setNegativeButton(android.R.string.cancel) { dialog, which ->
                                Toast.makeText(this, "İzin Gerekli !", Toast.LENGTH_SHORT).show()
                            }
                            .show()
                    }

                    containsDenial -> {
                        requestRelevantRuntimePermissions()
                    }

                    allGranted && hasRequiredRuntimePermissions() -> {
                        startBleScan()
                    }

                    else -> {
                        recreate()
                    }
                }
            }
        }
    }
    companion object {
       private const val publicKeyString = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnYJxbrXUFLCPbZkqJvKt\n" +
                "DmOrrB5Xq9SUKtNfQFyBJ8b55hzTMkacKTbOX0mvQmID2MnChcDdcdOrxnu4O7SM\n" +
                "5lUWj+imrdNcZgkm+4v+eFvSXwKtGByRo49j50gBhjVKi67cuxZAKFmfsatdSWWw\n" +
                "5r/2xt9v/AqvgCkUnyT1wjNSAFIkYwslsl++ss5Fbf6zkCblO6JGhy2YohKjBQU3\n" +
                "R48lnqLGNiVNRVfAl9N5Q3dDUR+OL9uW6AHv8KgUGFv/6VR/PH/kfUP1gy7zCz3p\n" +
                "BweM4jyWcMbPQb32NyXAGIW1MXogZU7Y3+gVNBgl5J+73Gh42MdSsSWekf2ZzTDe\n" +
                "5wIDAQAB\n"

     private const    val privateKey = "MIICXAIBAAKBgQCDPjWIMx48xa/715VpvjZiVCuiF/oUj6qCYVE1KFIMKgsfClQf\n" +
                "6k74rC8vqNMB+jZPanQcRuDl55mN8qdMNX68pmATGpJHvzyFnuOtGwM98WSO6L7t\n" +
                "jCYA3h4HVSPefPC9jpyd+Srv3AKSAZ8PpxdJMjakEIpC+BYFkxbbMsgnSwIDAQAB\n" +
                "AoGAZrAwFodYq1hKYBTIVVp9Fuag1U1JYPkgAq++aIdJ2zaySPE97VLZw3yF1xaT\n" +
                "M0LhZ7X1b4KNyZUy8nvgJqLcq/qFxtTJ15IOHlpp2S33Cd1UfcAkkh1WwPMlnC2L\n" +
                "TVN4eMFJPB6hLB87WcjYR+LsA/y+Ndexi5EQh57DCjG/7EECQQDssnmk2W4MYrQ1\n" +
                "OuRqX8vy7ZR/m+1fIFzxs5kjcDAlnDIQ4MOdudqeB/SMSTWksIYl/oICxlvDDYcN\n" +
                "ZryuRR0xAkEAjfIrELFPYT8pXbdsdiBiw+6hZzK0mfjCLYsxpAAXG4/0CHj1NPve\n" +
                "J20LsH6yGqo5flWdfHg7bQHmnf9kxtH9OwJBAM8fDhsWuJnV9WNu+VmsIkedZgiU\n" +
                "ZY6MP0ixpBvCnB8NIzJpvENU0tzekTwBBBPs9DZjE1liQgHY4Ij1kb3ddMECQHXC\n" +
                "+o1/vM5+GzB/80DRP38z4739KC4xXc9xEn7wADvCov/AchZB+x2Ub0U+5z4OCWLR\n" +
                "XrWb/hlCoXRlJNN59W8CQCV7O37zl4O+ahVc+xU+aoRnL/h7aQSUtkG2ZsBLi43/\n" +
                "7fV8viVYDGkk7ZIbzuNncEvMLEvUKmOnDhMt3iegkPg=\n"
    }
}


