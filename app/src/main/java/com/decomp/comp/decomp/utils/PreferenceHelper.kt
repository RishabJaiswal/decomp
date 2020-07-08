package com.decomp.comp.decomp.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.decomp.comp.decomp.BuildConfig
import com.decomp.comp.decomp.application.DeCompApplicaton

object PreferenceHelper {

    private val PREFERENCE_NAME = "${BuildConfig.APPLICATION_ID}_prefs"
    private val preferences: SharedPreferences by lazy {
        DeCompApplicaton.getInstance()
                .getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    fun putValue(key: String, value: Any?) {
        preferences.edit().run {
            when (value) {
                is Boolean -> putBoolean(key, value)
                is Int -> putInt(key, value)
                is Float -> putFloat(key, value)
                is Long -> putLong(key, value)
                is String -> putString(key, value)
                else -> Log.e("Preference error", "Attempting to save non-primitive value in preference for $key")
            }
            apply()
        }
    }

    fun getString(key: String): String = preferences.getString(key, "") ?: ""

    fun getInt(key: String): Int = preferences.getInt(key, 0)

    fun getFloat(key: String): Float = preferences.getFloat(key, 0F)

    fun getLong(key: String): Long = preferences.getLong(key, 0)

    fun getBoolean(key: String): Boolean = preferences.getBoolean(key, false)

    fun existInPreferences(key: String): Boolean {
        return preferences.contains(key)
    }

    fun delete(key: String) {
        if (preferences.contains(key)) {
            preferences.edit().remove(key).apply()
        } else {
            Log.e("Preference error", "$key does not exist in preferences")
        }
    }

    /**
     *  reified means variable needs to declare type explicitly while using this getter
     *  eg. val count: Int = getValue(key)
     */
    inline fun <reified T> getValue(key: String): T {
        return when (T::class) {
            Int::class -> getInt(key) as T
            Float::class -> getFloat(key) as T
            Long::class -> getLong(key) as T
            String::class -> getString(key) as T
            Boolean::class -> getBoolean(key) as T
            else -> throw IllegalStateException("Preference type not supported")
        }
    }

    fun clearPreferences() = preferences.edit().clear().apply()
}