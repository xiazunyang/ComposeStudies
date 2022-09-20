package cn.numeron.study03

import android.app.Application
import cn.numeron.uistate.UIState

class ComposeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Runtime.attachBaseContext(this)
        UIState.init(this)
    }

}