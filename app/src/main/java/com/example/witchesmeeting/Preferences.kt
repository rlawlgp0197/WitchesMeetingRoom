package com.example.witchesmeeting

import android.content.Context

class Preferences(context: Context) {
    private val prefs = context.getSharedPreferences("pref_name", Context.MODE_PRIVATE)

    // 저장값 가져오기
    fun getString(key: String, defValue: String?): String {
        return prefs.getString(key, defValue).toString()
    }

    // 값 저장
    fun setString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()

    }
}