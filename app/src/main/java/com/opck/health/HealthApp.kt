package com.opck.health

import android.app.Application
import com.opck.health.data.api.RetrofitClient
import com.opck.health.data.repository.AuthRepository

/**
 * Application 入口 - 持全局单例 (简单手写 DI)
 *
 * 生产建议改 Hilt; 学生项目手写足够, 易理解
 */
class HealthApp : Application() {

    lateinit var authRepository: AuthRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        val tokenStore = com.opck.health.data.local.TokenStore(this)
        authRepository = AuthRepository(tokenStore)
    }

    companion object {
        @Volatile
        lateinit var instance: HealthApp
            private set

        fun get(): HealthApp = instance
    }
}
