package com.snoy.devicechecker

import android.util.Base64

fun String.base64Decode(): ByteArray {
    return Base64.decode(this, Base64.DEFAULT)
}

fun ByteArray.base64Encode(): String {
    return Base64.encodeToString(this, Base64.DEFAULT)
}