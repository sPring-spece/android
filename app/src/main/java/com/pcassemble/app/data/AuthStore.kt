package com.pcassemble.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "auth_store")

/** 用 DataStore 持久化登录 Token 与服务器地址（内存缓存供 OkHttp 拦截器同步读取） */
class AuthStore(private val context: Context) {

    private val tokenKey = stringPreferencesKey("access_token")
    private val serverUrlKey = stringPreferencesKey("server_url")

    private var cached: String? = null

    val tokenFlow: Flow<String?> = context.dataStore.data.map { prefs -> prefs[tokenKey] }

    suspend fun getToken(): String? {
        cached = tokenFlow.first()
        return cached
    }

    /** 同步读取内存缓存（拦截器用，不能挂起） */
    fun cachedToken(): String? = cached

    suspend fun saveToken(token: String) {
        cached = token
        context.dataStore.edit { it[tokenKey] = token }
    }

    suspend fun clearToken() {
        cached = null
        context.dataStore.edit { it.remove(tokenKey) }
    }

    // ---------- 服务器地址（设置页可改，IP 变化不用重装） ----------

    suspend fun getServerUrl(): String? =
        context.dataStore.data.map { it[serverUrlKey] }.first()

    suspend fun saveServerUrl(url: String) {
        context.dataStore.edit { it[serverUrlKey] = url }
    }
}
