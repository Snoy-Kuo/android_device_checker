package com.snoy.devicechecker

import com.google.gson.annotations.SerializedName

@Suppress("unused")
class SafetyNetApiModel {
    @SerializedName("nonce")
    var nonce: String? = null

    @SerializedName("timestampMs")
    var timestampMs: Long? = null

    @SerializedName("apkPackageName")
    var apkPackageName: String? = null

    @SerializedName("apkCertificateDigestSha256")
    var apkCertificateDigestSha256: List<String>? = null

    @SerializedName("apkDigestSha256")
    var apkDigestSha256: String? = null

    @SerializedName("ctsProfileMatch")
    var ctsProfileMatch: Boolean? = null

    @SerializedName("basicIntegrity")
    var basicIntegrity: Boolean? = null
}