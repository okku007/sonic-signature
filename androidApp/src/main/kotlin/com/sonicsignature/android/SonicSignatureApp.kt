package com.sonicsignature.android

import android.app.Application
import com.sonicsignature.storage.ApplicationContextHolder

class SonicSignatureApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ApplicationContextHolder.init(this)
    }
}
