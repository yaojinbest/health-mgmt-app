package com.opck.health.data.api

import com.opck.health.BuildConfig
import com.opck.health.data.local.TokenStore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit 客户端 - 单例
 *
 * - baseUrl 默认指向 10.0.2.2:8090 (Android emulator 指向宿主机)
 * - 全局拦截器: 自动加 Authorization Bearer
 * - 日志拦截器: debug 模式打印请求/响应体
 */
object RetrofitClient {

    fun create(tokenStore: TokenStore): HealthApi {
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
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(HealthApi::class.java)
    }
}
