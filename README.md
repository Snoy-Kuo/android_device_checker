# android_device_checker

An Android example App that shows some device and resettable ids includes:
 - UUID
 - SSAID
 - AAID
 - IMEI
 - MAC address
 - IP address
 - Public IP Address
 - Firebase installation ID (FID)
 - DRM API device unique ID
 - SafetyNet API response nonce

## Dev env

 - macOS 11.6 (Big Sur) x64
 - Android Studio Bumblebee Patch 1
 - Android SDK version 31
 - JDK: 11
 - Gradle: 7.2
 - Kotlin: 1.6.10

 ## References

 - [Best practices for unique identifiers](https://developer.android.com/training/articles/user-data-ids)
 - [Using MediaDrm for device ID tracking](https://beltran.work/blog/2018-03-27-device-unique-id-android/)
 - [SafetyNetApi](https://github.com/Rendellhb/SafetyNetApi)
 - [Stay Safe With SafetyNet Attestation API in Android](https://www.netguru.com/blog/stay-safe-with-safetynet-attestation-api-in-android)

 ## Libraries

 - [play-services-ads-identifier](https://developers.google.com/android/guides/setup)
 - [kotlinx-coroutines-android](https://developer.android.com/kotlin/coroutines)
 - [lifecycle-runtime-ktx](https://developer.android.com/jetpack/androidx/releases/lifecycle)
 - [Ipify-Android](https://github.com/chintan369/Ipify-Android)
 - [firebase-installations](https://firebase.google.com/docs/projects/manage-installations#kotlin+ktx_1)
 - [play-services-safetynet](https://developer.android.com/training/safetynet)
 - [GSON](https://github.com/google/gson)

 ## Todos

 - move logic code to VM layer.