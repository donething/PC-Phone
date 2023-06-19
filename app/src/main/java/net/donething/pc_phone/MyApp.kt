package net.donething.pc_phone

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class MyApp : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var ctx: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        ctx = applicationContext
    }
}