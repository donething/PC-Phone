package net.donething.pc_phone

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import net.donething.pc_phone.ui.preferences.CustomDataStore
import net.donething.pc_phone.ui.preferences.dataStore

class MyApp : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var ctx: Context
            private set

        lateinit var myDS: CustomDataStore
            private set
    }

    override fun onCreate() {
        super.onCreate()
        ctx = applicationContext
        myDS = CustomDataStore(ctx.dataStore)
    }
}