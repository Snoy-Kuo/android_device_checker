package com.snoy.devicechecker

fun ByteArray.toHex(separator:String): String = joinToString(separator = separator) { eachByte -> "%02x".format(eachByte) }