package kkk.com.mhwjewelry

import android.app.Application

class MHWTApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        DataManager.init(this)
    }
}
