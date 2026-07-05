package com.opck.health.data.repository

import com.opck.health.data.api.HealthApi
import com.opck.health.data.api.RetrofitClient
import com.opck.health.data.local.TokenStore
import com.opck.health.data.model.LoginRequest
import com.opck.health.data.model.LoginVO
import com.opck.health.data.model.RegisterRequest

/**
 * 认证仓库 - 业务层与 API 层解耦
 *
 * 调用流程: ViewModel -> AuthRepository -> HealthApi -> 后端
 */
class AuthRepository(private val tokenStore: TokenStore) {

    private val api: HealthApi by lazy { RetrofitClient.create(tokenStore) }

    suspend fun login(username: String, password: String, role: String? = null): Result<LoginVO> {
        return runCatching {
            val resp = api.login(LoginRequest(username, password, role))
            if (resp.code == 200 && resp.data != null) {
                val vo = resp.data!!
                tokenStore.saveLogin(vo.token, vo.id, vo.username, vo.role, vo.realName)
                vo
            } else {
                throw RuntimeException(resp.message ?: "登录失败")
            }
        }
    }

    suspend fun register(req: RegisterRequest): Result<LoginVO> {
        return runCatching {
            val resp = api.register(req)
            if (resp.code == 200 && resp.data != null) {
                val vo = resp.data!!
                tokenStore.saveLogin(vo.token, vo.id, vo.username, vo.role, vo.realName)
                vo
            } else {
                throw RuntimeException(resp.message ?: "注册失败")
            }
        }
    }

    fun logout() {
        tokenStore.clear()
    }

    fun isLoggedIn() = tokenStore.isLoggedIn()
    fun currentUserId() = tokenStore.getUserId()
}
