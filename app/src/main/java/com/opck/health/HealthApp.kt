package com.opck.health

import android.app.Application
import com.opck.health.data.api.HealthApi
import com.opck.health.data.api.RetrofitClient
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

    override fun onCreate() {
        super.onCreate()
        instance = this
        val tokenStore = TokenStore(this)
        api = RetrofitClient.create(tokenStore)
        authRepository = AuthRepository(tokenStore)
        repository = HealthRepository(api)
    }

    companion object {
        @Volatile
        lateinit var instance: HealthApp
            private set

        fun get(): HealthApp = instance
    }
}