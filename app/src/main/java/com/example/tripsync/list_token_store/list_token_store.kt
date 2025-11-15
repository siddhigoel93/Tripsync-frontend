package com.example.tripsync.list_token_store

import android.content.Context

object TokenStore {
    private const val PREFS = "auth"
    private const val KEY_ACCESS = "access_token"
    private const val KEY_REFRESH = "refresh_token"

    fun getAccessToken(context: Context): String? {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_ACCESS, null)
    }

    fun getRefreshToken(context: Context): String? {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_REFRESH, null)
    }

    fun saveTokens(context: Context, access: String?, refresh: String?) {
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        if (access != null) sp.putString(KEY_ACCESS, access) else sp.remove(KEY_ACCESS)
        if (refresh != null) sp.putString(KEY_REFRESH, refresh) else sp.remove(KEY_REFRESH)
        sp.apply()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
