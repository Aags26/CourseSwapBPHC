package com.bphc.courseswap.prefs

import android.content.Context
import android.content.SharedPreferences

object SharedPrefs {

    private val PREFS_NAME = "Shared_prefs"
    private var _sharedPreferences: SharedPreferences? = null

    private fun instance(context: Context): SharedPreferences? {

        if (_sharedPreferences == null) {
            _sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
        return _sharedPreferences
    }

    fun getStringParams(context: Context?, paramKey: String?, defaultValue: String?): String? {
        return context?.let { instance(it)?.getString(paramKey, defaultValue) }
    }

    fun setStringParams(context: Context?, paramKey: String?, paramValue: String?) {
        val editor: SharedPreferences.Editor? = context?.let { instance(it)?.edit() }
        editor?.putString(paramKey, paramValue)
        editor?.apply()
    }

}