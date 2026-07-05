package com.opck.health.data.api

import com.opck.health.data.local.TokenStore
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 全局 JWT 拦截器 - 自动为每个请求加 Authorization 头
 *
 * 与 H5 端 portal.js 中的 ensurePortalSession 行为对齐
 */
class AuthInterceptor(private val tokenStore: TokenStore) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()
        tokenStore.getToken()?.let { token ->
            builder.header("Authorization", "Bearer $token")
        }
        return chain.proceed(builder.build())
    }
}
