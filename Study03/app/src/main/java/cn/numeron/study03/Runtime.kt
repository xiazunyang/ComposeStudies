package cn.numeron.study03

import android.content.Context
import android.content.ContextWrapper

object Runtime : ContextWrapper(null) {

    public override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

}