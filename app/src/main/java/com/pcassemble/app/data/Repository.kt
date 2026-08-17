package com.pcassemble.app.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrl

/** 全局数据仓库：持有登录态，封装业务调用；支持动态切换后端地址 */
class Repository(
    initialApi: ApiService,
    private val authStore: AuthStore,
) {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var api: ApiService = initialApi

    @Volatile
    private var currentBaseUrl: String = Network.currentBaseUrl()

    private val _serverUrl = MutableStateFlow<String?>(null)
    val serverUrl: StateFlow<String?> = _serverUrl.asStateFlow()

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    private val _currentUser = MutableStateFlow<UserOut?>(null)
    val currentUser: StateFlow<UserOut?> = _currentUser.asStateFlow()

    /** 启动时登录态恢复是否完成（MainScreen 据此决定是否跳登录页，避免误跳） */
    private val _sessionReady = MutableStateFlow(false)
    val sessionReady: StateFlow<Boolean> = _sessionReady.asStateFlow()

    /** App 启动时从本地恢复登录态 */
    suspend fun restoreSession() {
        try {
            val t = authStore.getToken()
            if (t != null) {
                _token.value = t
                try {
                    _currentUser.value = api.me()
                } catch (_: Exception) {
                    // token 失效则清除
                    _token.value = null
                    authStore.clearToken()
                }
            }
        } finally {
            _sessionReady.value = true
        }
    }

    // ---------- 服务器地址动态配置 ----------

    /** 启动时读取持久化的服务器地址（若用户改过则覆盖默认值） */
    suspend fun initServerUrl() {
        val saved = authStore.getServerUrl()
        if (saved != null && saved.isNotBlank()) {
            currentBaseUrl = saved
            _serverUrl.value = saved
            api = Network.createApi(baseUrl = saved) { authStore.cachedToken() }
        } else {
            _serverUrl.value = Network.currentBaseUrl()
        }
    }

    /** 设置页：切换后端地址，持久化并立即重建 Retrofit（IP 变化无需重装） */
    fun reconfigureServer(url: String) {
        val normalized = Network.normalizeBaseUrl(url)
        currentBaseUrl = normalized
        api = Network.createApi(baseUrl = normalized) { authStore.cachedToken() }
        _serverUrl.value = normalized
        appScope.launch { authStore.saveServerUrl(normalized) }
    }

    /** 相对图片路径 → 完整 URL（按当前后端地址拼，跟随设置页改动） */
    fun imageUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        if (path.startsWith("http")) return path
        return try {
            val base = Network.normalizeBaseUrl(currentBaseUrl).toHttpUrl()
            "${base.scheme}://${base.host}:${base.port}$path"
        } catch (_: Exception) {
            null
        }
    }

    suspend fun register(username: String, password: String, nickname: String?): UserOut {
        val token = api.register(RegisterRequest(username, password, nickname))
        _token.value = token.access_token
        _currentUser.value = token.user
        authStore.saveToken(token.access_token)
        return token.user
    }

    suspend fun login(username: String, password: String): UserOut {
        val token = api.login(LoginRequest(username, password))
        _token.value = token.access_token
        _currentUser.value = token.user
        authStore.saveToken(token.access_token)
        return token.user
    }

    suspend fun logout() {
        _token.value = null
        _currentUser.value = null
        authStore.clearToken()
    }

    val isLoggedIn: Boolean get() = _token.value != null

    // ---------- 业务透传 ----------
    suspend fun parts(type: String? = null, keyword: String? = null): List<PartOut> =
        api.parts(type = type, keyword = keyword).items

    /** 分页查询配件（含 total），DIY 页面按类型拉全量用 */
    suspend fun partsPage(type: String?, page: Int, pageSize: Int): PartListOut =
        api.parts(type = type, keyword = null, page = page, pageSize = pageSize)

    suspend fun configs(scope: String = "official", level: String? = null): List<ConfigOut> =
        api.configs(scope = scope, level = level).items

    suspend fun configDetail(id: Int): ConfigOut = api.configDetail(id)

    suspend fun validate(parts: List<ValidatePartIn>): ValidateResponse =
        api.validate(ValidateRequest(parts))

    suspend fun recommend(budget: Double): RecommendResponse = api.recommend(budget)

    suspend fun shareConfig(name: String, level: String, description: String?, parts: List<ValidatePartIn>): ConfigOut =
        api.shareConfig(ConfigShareRequest(name, level, description, parts))

    suspend fun deleteConfig(id: Int) = api.deleteConfig(id)

    suspend fun favorite(id: Int) = api.favorite(id)

    suspend fun unfavorite(id: Int) = api.unfavorite(id)

    suspend fun cloneConfig(id: Int): ConfigOut = api.cloneConfig(id)

    suspend fun favorites(): List<FavoriteOut> = api.favorites().items

    suspend fun createOrder(parts: List<OrderPartIn>, receiver: ReceiverIn, remark: String?): OrderOut =
        api.createOrder(OrderCreate(parts, receiver, remark))

    suspend fun orders(): List<OrderOut> = api.orders().items

    suspend fun orderDetail(id: Int): OrderOut = api.orderDetail(id)

    suspend fun payOrder(id: Int): OrderOut = api.payOrder(id)

    suspend fun cancelOrder(id: Int): OrderOut = api.cancelOrder(id)

    suspend fun confirmOrder(id: Int): OrderOut = api.confirmOrder(id)

    suspend fun posts(keyword: String? = null): List<PostOut> = api.posts(keyword = keyword).items

    suspend fun postDetail(id: Int): PostOut = api.postDetail(id)

    suspend fun createPost(title: String, content: String): PostOut =
        api.createPost(PostCreate(title, content))

    suspend fun createComment(postId: Int, content: String): CommentOut =
        api.createComment(postId, CommentCreate(content))

    suspend fun consultations(): List<ConsultationOut> = api.consultations().items

    suspend fun createConsultation(content: String, contact: String?, appointmentDate: String?): ConsultationOut =
        api.createConsultation(ConsultationCreate(content, contact, appointmentDate))
}
