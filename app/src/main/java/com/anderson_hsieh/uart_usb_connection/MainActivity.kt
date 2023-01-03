package com.anderson_hsieh.uart_usb_connection

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import android.content.DialogInterface




private const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"

private val usbReceiver = object : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (ACTION_USB_PERMISSION == intent.action) {
            // like a semaphore for this broadcast receiver, haha
            synchronized(this) {
                val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)

                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    device?.apply {
                        //call method to set up device communication

                    }
                } else {
                    Log.d("permission", "permission denied for device $device")
                }
            }
        }
    }
}

class MainActivity : AppCompatActivity() {

    lateinit var dataSet: ArrayList<String>
    lateinit var recyclerView: RecyclerView
    lateinit var usbManager: UsbManager
    lateinit var connectBtn: MaterialButton
    lateinit var alertDialog: AlertDialog
    var device: UsbDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dataSet = ArrayList()
        dataSet.add("1")
        dataSet.add("2")
        dataSet.add("3")
        recyclerView = findViewById(R.id.serial_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = SerialRecyclerAdapter(dataSet)

        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        registerReceiver(usbReceiver, filter)

        initAlertDialog();
        initConntBtn(PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0));
    }

    fun initAlertDialog(){
        alertDialog = AlertDialog.Builder(this@MainActivity).create()
        alertDialog.setTitle(getString(R.string.oops))
        alertDialog.setMessage(getString(R.string.no_device))
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
            DialogInterface.OnClickListener { dialog, _ -> dialog.dismiss() })
    }

    fun initConntBtn(permissionIntent:PendingIntent) {
        connectBtn = findViewById(R.id.connect_btn);
        connectBtn.setOnClickListener {
            val deviceList = usbManager.deviceList
            deviceList.forEach {
                dataSet.add(it.value.deviceName);
                recyclerView.adapter?.apply {
                    notifyDataSetChanged()
                }
            }
            device = deviceList["deviceName"]
            if (device != null) {
                usbManager.requestPermission(device, permissionIntent)
            } else {
                alertDialog.show()
            }
        }
    }
}