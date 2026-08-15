package com.pcassemble.app

import android.app.Application
import com.pcassemble.app.data.AuthStore
import com.pcassemble.app.data.Network
import com.pcassemble.app.data.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PcAssembleApp : Application() {

    lateinit var repository: Repository
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        val authStore = AuthStore(this)
        val api = Network.createApi(baseUrl = Network.currentBaseUrl()) { authStore.cachedToken() }
        repository = Repository(api, authStore)
        // 恢复登录态：先加载持久化的服务器地址（用户改过则覆盖默认），再恢复 token
        appScope.launch {
            repository.initServerUrl()
            repository.restoreSession()
        }
    }
}
