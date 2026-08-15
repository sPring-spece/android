package com.pcassemble.app.data

import com.pcassemble.app.BuildConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface ApiService {

    // 认证
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Token

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Token

    @GET("auth/me")
    suspend fun me(): UserOut

    // 配件
    @GET("parts")
    suspend fun parts(
        @Query("type") type: String? = null,
        @Query("keyword") keyword: String? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 50,
    ): PartListOut

    @GET("parts/{id}")
    suspend fun part(@Path("id") id: Int): PartOut

    // 装机方案
    @GET("configs")
    suspend fun configs(
        @Query("scope") scope: String = "official",
        @Query("level") level: String? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 50,
    ): ConfigListOut

    @GET("configs/{id}")
    suspend fun configDetail(@Path("id") id: Int): ConfigOut

    @POST("configs/validate")
    suspend fun validate(@Body body: ValidateRequest): ValidateResponse

    @GET("configs/recommend")
    suspend fun recommend(@Query("budget") budget: Double): RecommendResponse

    @POST("configs")
    suspend fun shareConfig(@Body body: ConfigShareRequest): ConfigOut

    @DELETE("configs/{id}")
    suspend fun deleteConfig(@Path("id") id: Int): Unit

    @POST("configs/{id}/favorite")
    suspend fun favorite(@Path("id") id: Int): ConfigOut

    @DELETE("configs/{id}/favorite")
    suspend fun unfavorite(@Path("id") id: Int): Unit

    @POST("configs/{id}/clone")
    suspend fun cloneConfig(@Path("id") id: Int): ConfigOut

    // 收藏
    @GET("favorites")
    suspend fun favorites(@Query("page") page: Int = 1, @Query("page_size") pageSize: Int = 50): FavoriteListOut

    // 订单
    @POST("orders")
    suspend fun createOrder(@Body body: OrderCreate): OrderOut

    @GET("orders")
    suspend fun orders(@Query("page") page: Int = 1, @Query("page_size") pageSize: Int = 20): OrderListOut

    @GET("orders/{id}")
    suspend fun orderDetail(@Path("id") id: Int): OrderOut

    @POST("orders/{id}/pay")
    suspend fun payOrder(@Path("id") id: Int): OrderOut

    @POST("orders/{id}/cancel")
    suspend fun cancelOrder(@Path("id") id: Int): OrderOut

    @POST("orders/{id}/confirm")
    suspend fun confirmOrder(@Path("id") id: Int): OrderOut

    // 咨询
    @POST("consultations")
    suspend fun createConsultation(@Body body: ConsultationCreate): ConsultationOut

    @GET("consultations")
    suspend fun consultations(@Query("page") page: Int = 1): ConsultationListOut

    // 社区
    @GET("posts")
    suspend fun posts(
        @Query("keyword") keyword: String? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
    ): PostListOut

    @GET("posts/{id}")
    suspend fun postDetail(@Path("id") id: Int): PostOut

    @POST("posts")
    suspend fun createPost(@Body body: PostCreate): PostOut

    @POST("posts/{id}/comments")
    suspend fun createComment(@Path("id") id: Int, @Body body: CommentCreate): CommentOut
}

/** 网络层：构建带 Token 拦截器的 Retrofit 实例 */
object Network {

    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    /** Retrofit 要求 baseUrl 以 / 结尾，这里兜底规整，避免配置漏写斜杠导致启动崩溃 */
    fun normalizeBaseUrl(url: String): String =
        if (url.endsWith("/")) url else "$url/"

    /** 编译期默认后端地址（设置页未改过时使用） */
    fun currentBaseUrl(): String = BuildConfig.BASE_URL

    fun createApi(
        baseUrl: String = BuildConfig.BASE_URL,
        tokenProvider: () -> String?,
    ): ApiService {
        val authInterceptor = okhttp3.Interceptor { chain ->
            val token = tokenProvider()
            val request = if (token != null) {
                chain.request().newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            } else {
                chain.request()
            }
            chain.proceed(request)
        }
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl(normalizeBaseUrl(baseUrl))
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApiService::class.java)
    }

    /** 测试后端连通性：从 baseUrl 提取主机请求根路径 /health（不依赖 Retrofit） */
    fun checkHealth(baseUrl: String): Boolean {
        return try {
            val url = normalizeBaseUrl(baseUrl).toHttpUrl()
            val healthUrl = "${url.scheme}://${url.host}:${url.port}/health"
            val client = OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build()
            val resp = client.newCall(
                okhttp3.Request.Builder().url(healthUrl).build()
            ).execute()
            resp.use { it.isSuccessful }
        } catch (_: Exception) {
            false
        }
    }

    /** 从后端错误响应体解析 detail 消息 */
    fun errorMessage(e: Throwable): String = when (e) {
        is retrofit2.HttpException -> {
            try {
                val body = e.response()?.errorBody()?.string() ?: return "请求失败(${e.code()})"
                json.parseToJsonElement(body).jsonObject["detail"]?.toString()?.trim('"')
                    ?: "请求失败(${e.code()})"
            } catch (_: Exception) {
                "请求失败(${e.code()})"
            }
        }
        is java.net.ConnectException -> "无法连接服务器，请确认后端已启动"
        is java.net.SocketTimeoutException -> "请求超时"
        else -> e.message ?: "未知错误"
    }
}

/** 兼容旧写法：JsonObject 便捷读取 */
fun JsonObject.str(key: String): String? = this[key]?.let { el ->
    try {
        el.toString().trim('"')
    } catch (_: Exception) {
        null
    }
}
