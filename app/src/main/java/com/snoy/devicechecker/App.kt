package com.snoy.devicechecker

import android.app.Application
import com.creative.ipfyandroid.Ipfy
import com.creative.ipfyandroid.IpfyClass

class App: Application() {
    override fun onCreate() {
        super.onCreate();

        Ipfy.init(this) // this is a context of application
        //or you can also pass IpfyClass type to get either IPv4 address only or universal address IPv4/v6 as
        Ipfy.init(this, IpfyClass.IPv4) //to get only IPv4 address
        //and
//        Ipfy.init(this,IpfyClass.UniversalIP) //to get Universal address in IPv4/v6
    }
}