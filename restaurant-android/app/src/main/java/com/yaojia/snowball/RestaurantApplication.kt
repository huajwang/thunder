package com.yaojia.snowball

import android.app.Application
import com.yaojia.snowball.data.network.NetworkModule

class RestaurantApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NetworkModule.init(this)
    }
}
