package com.amhsolution.caisse

import android.app.Application
import com.amhsolution.caisse.data.AppContainer

class CaisseApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
