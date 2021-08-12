This is a demo project to try to use `libusb` on Android. It's a very simple test, so it doesn't have much error handling and doesn't really take into account app lifecycle (pause/resume).

## Build

* `libusb` needs to be build, following the instructions in https://github.com/libusb/libusb/blob/master/android/README

* Essentially, from within `libusb/android/jni`, run `$NDK/ndk-build` (assuming the NDK was already installed: it will generally be in a subdirectory of the SDK if installed from within Android Studio). (For example: `C:\Users\...\AppData\Local\Android\Sdk\ndk\23.0.7599858\ndk-build`, the path/version may need to be adapted to your environment.)

* Set `libusb.path` in `local.properties`, e.g. `libusb.path=C:/Users/..../AndroidStudioProjects/libusb`

If libusb is re-built between builds of the Android project, it's worth using "Reload from Disk" on the project in Android Studio to make sure it is aware of the libusb updates (otherwise it may use a cached version).

## App usage

This demo application does nothing besides trying to connect to the device.

1. Start the app **before** plugging the device.
2. Plug in the USB device (via an OTG cable).
3. A dialog box should appear asking to allow the app to access the device.
4. Press "Test Button".

In case of success, it should show a toast message saying "*nativeInitDevice returned 0*", that's the return value from the `nativeInitDevice` function (in `native-lib.cpp`). For non-zero return values (or crashes), please check the Android logs.



There is a workaround for the permission behaviour when know in advance the Vendor ID and Product ID of the USB device: we need to define an intent filter with metadata for the activity (not service) in `AndroidManifest.xml`:

```xml
<activity
    android:name=".MainActivity"
    android:directBootAware="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />

        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>

    <intent-filter>
        <action android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" />
    </intent-filter>

    <meta-data
        android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED"
        android:resource="@xml/usb_device_filter" />
</activity>
```

Each device needs to be hard-coded in the `xml/usb_device_filter.xml` file:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources xmlns:android="http://schemas.android.com/apk/res/android">
    <!--
        USB vendor IDs and product IDs need to be written in DECIMAL
        format or using the 0x prefix for HEXADECIMAL
    -->
    <usb-device
        vendor-id="0x1234"
        product-id="0x1234"/>
    <usb-device
        vendor-id="0x1234"
        product-id="0xabcd"/>
</resources>
```