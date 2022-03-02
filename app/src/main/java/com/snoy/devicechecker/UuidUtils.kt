package com.snoy.devicechecker

import java.nio.ByteBuffer
import java.util.*

//ref= https://stackoverflow.com/questions/17893609/convert-uuid-to-byte-that-works-when-using-uuid-nameuuidfrombytesb

fun UUID.toByteArray(): ByteArray {
    val bb: ByteBuffer = ByteBuffer.wrap(ByteArray(16))
    bb.putLong(this.mostSignificantBits)
    bb.putLong(this.leastSignificantBits)
    return bb.array()
}

fun ByteArray.toUuid(): UUID {
    val bb = ByteBuffer.wrap(this)
    val firstLong = bb.long
    val secondLong = bb.long
    return UUID(firstLong, secondLong)
}
