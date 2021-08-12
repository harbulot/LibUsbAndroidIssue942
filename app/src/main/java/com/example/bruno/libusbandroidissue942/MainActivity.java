/*
 * Copyright 2021 Bruno Harbulot.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.example.bruno.libusbandroidissue942;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bruno.libusbandroidissue942.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    public final static String TAG = "LibUsbAndroidIssue942MainActivity";

    static {
        System.loadLibrary("native-lib");
    }

    private ActivityMainBinding binding;

    private UsbManager usbManager;
    private PendingIntent usbPermissionIntent;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private UsbDevice lastAuthorisedDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        usbPermissionIntent =
                PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(usbReceiver, filter);

        TextView tv = binding.sampleText;
        tv.setText("Test");

        binding.testButton.setOnClickListener(v -> {
            if (lastAuthorisedDevice != null) {
                UsbDeviceConnection usbDeviceConnection = null;
                try {
                    usbDeviceConnection = usbManager.openDevice(lastAuthorisedDevice);
                    int r = nativeInitDevice(usbDeviceConnection.getFileDescriptor());

                    Toast toast =
                            Toast.makeText(MainActivity.this, "nativeInitDevice returned " + r,
                                           Toast.LENGTH_LONG);
                    toast.show();
                } finally {
                    if (usbDeviceConnection != null) {
                        usbDeviceConnection.close();
                    }
                }
            } else {
                Toast toast = Toast.makeText(MainActivity.this, "No authorised device detected.",
                                             Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    private native int nativeInitDevice(int usbDeviceFileDescriptor);

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            lastAuthorisedDevice = device;
                        }
                    } else {
                        Log.d(TAG, String.format("Permission denied for device %04x:%04x %s",
                                                 device.getVendorId(), device.getProductId(),
                                                 device.getDeviceName()));
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    Log.d(TAG,
                          String.format("Attached USB Device %04x:%04x %s", device.getVendorId(),
                                        device.getProductId(), device.getDeviceName()));
                    usbManager.requestPermission(device, usbPermissionIntent);
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    Log.d(TAG,
                          String.format("Detached USB Device %04x:%04x %s", device.getVendorId(),
                                        device.getProductId(), device.getDeviceName()));
                }

                lastAuthorisedDevice = null;
            }
        }
    };
}