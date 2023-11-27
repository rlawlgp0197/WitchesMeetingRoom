package com.example.witchesmeeting

import android.app.Activity
import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.prolificinteractive.materialcalendarview.BuildConfig

class GlobalApplication : Application() {
    companion object {
        lateinit var prefsManager: Preferences
    }

    override fun onCreate() {
        prefsManager = Preferences(applicationContext)
        super.onCreate()

        // 카톡로그인 키해시
        KakaoSdk.init(this, "키해시")
    }

}