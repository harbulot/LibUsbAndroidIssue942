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
#include <jni.h>
#include <string>

#include <android/log.h>
#include "libusb.h"

#define TAG "TestApplicationLibUsbAndroid942"

extern "C" JNIEXPORT jint JNICALL Java_com_example_bruno_libusbandroidissue942_MainActivity_nativeInitDevice(
        JNIEnv *env,
        jobject thiz,
        jint usb_device_file_descriptor) {
    int r;
    libusb_context *ctx = NULL;

    r = libusb_set_option(NULL, LIBUSB_OPTION_NO_DEVICE_DISCOVERY);
    __android_log_print(ANDROID_LOG_DEBUG, TAG,
                        "libusb_set_option LIBUSB_OPTION_NO_DEVICE_DISCOVERY: %d", r);

    r = libusb_init(&ctx);
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "libusb_init(&ctx): %d", r);
    if (r < 0) {
        return r;
    }

    struct libusb_device_handle *dev_handle;
    r = libusb_wrap_sys_device(ctx, (intptr_t) usb_device_file_descriptor, &dev_handle);
    if (r < 0) {
        __android_log_print(ANDROID_LOG_DEBUG, TAG, "libusb_wrap_sys_device: %d", r);
        libusb_exit(ctx);
        return r;
    }

    // Do something with the dev_handle...

    libusb_exit(ctx);
    return 0;
}