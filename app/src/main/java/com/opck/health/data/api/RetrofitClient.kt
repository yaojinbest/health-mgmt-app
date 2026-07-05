package com.opck.health.data.api

import com.opck.health.BuildConfig
import com.opck.health.data.local.ServerConfig
import com.opck.health.data.local.TokenStore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit 客户端 - 单例 (支持运行时 baseUrl 切换)
 *
 * - baseUrl 默认从 ServerConfig 读取 (运行时可改)
 * - 全局拦截器: 自动加 Authorization Bearer
 * - 日志拦截器: debug 模式打印请求/响应体
 *
 * 切换服务器地址流程:
 *   1. 用户在 ServerConfigActivity 改 URL, 写入 SharedPreferences
 *   2. ServerConfigActivity 调 retrofitClient.recreate() 重建
 *   3. 新请求自动用新 baseUrl
 */
class RetrofitClient(
    private val serverConfig: ServerConfig,
    private val tokenStore: TokenStore
) {
    @Volatile
    private var apiInstance: HealthApi? = null
    private val lock = Any()

    /**
     * 获取当前 API 实例 (懒加载)
     */
    fun api(): HealthApi {
        return apiInstance ?: synchronized(lock) {
            apiInstance ?: create().also { apiInstance = it }
        }
    }

    /**
     * 重建 API 实例 (用户改 baseUrl 后调用)
     */
    fun recreate(): HealthApi {
        synchronized(lock) {
            val newApi = create()
            apiInstance = newApi
            return newApi
        }
    }

    /**
     * 当前 baseUrl (调试用 / UI 显示)
     */
    fun currentBaseUrl(): String = serverConfig.getServerUrl()

    private fun create(): HealthApi {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val okHttp = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenStore))
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS) // 文件上传需要更长
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(serverConfig.getServerUrl())
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(HealthApi::class.java)
    }
}