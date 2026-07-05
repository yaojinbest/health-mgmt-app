package com.opck.health

import android.app.Application
import com.opck.health.data.api.HealthApi
import com.opck.health.data.api.RetrofitClient
import com.opck.health.data.local.ServerConfig
import com.opck.health.data.local.TokenStore
import com.opck.health.data.repository.AuthRepository
import com.opck.health.data.repository.HealthRepository

/**
 * Application 入口 - 持全局单例 (简单手写 DI)
 *
 * 生产建议改 Hilt; 学生项目手写足够, 易理解
 */
class HealthApp : Application() {

    lateinit var authRepository: AuthRepository
        private set

    lateinit var repository: HealthRepository
        private set

    lateinit var api: HealthApi
        private set

    lateinit var retrofitClient: RetrofitClient
        private set

    lateinit var serverConfig: ServerConfig
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        val tokenStore = TokenStore(this)
        serverConfig = ServerConfig(this)
        retrofitClient = RetrofitClient(serverConfig, tokenStore)
        api = retrofitClient.api()
        // Repository 持有 apiProvider lambda, 总是拿最新 api 实例
        // (避免 recreate() 后 Repository 还拿旧 baseUrl)
        authRepository = AuthRepository(tokenStore) { retrofitClient.api() }
        repository = HealthRepository { retrofitClient.api() }
    }

    companion object {
        @Volatile
        lateinit var instance: HealthApp
            private set

        fun get(): HealthApp = instance
    }
}