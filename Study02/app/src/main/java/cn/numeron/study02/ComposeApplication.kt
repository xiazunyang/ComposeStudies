package cn.numeron.study02

import android.app.Application
import android.content.Context
import android.content.ContextWrapper

class ComposeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        runtime = Runtime(applicationContext)
    }

    companion object {

        private lateinit var runtime: Runtime

        operator fun invoke(): Context {
            return runtime
        }

    }

    class Runtime(context: Context) : ContextWrapper(context)

}