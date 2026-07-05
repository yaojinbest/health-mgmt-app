package com.opck.health.data.model

import com.google.gson.annotations.SerializedName

/**
 * 登录请求
 */
data class LoginRequest(
    val username: String,
    val password: String,
    val role: String? = null
)

/**
 * 注册请求
 */
data class RegisterRequest(
    val username: String,
    val password: String,
    val realName: String,
    val phone: String? = null,
    val gender: String? = null,
    val age: Int? = null,
    val role: String? = null
)

/**
 * 用户实体 (数据库 sys_user 表)
 */
data class SysUser(
    val id: Long,
    val username: String,
    val realName: String?,
    @SerializedName("phone") val phone: String?,
    val role: String,
    val gender: String?,
    val age: Int?,
    val status: String?,
    val createTime: String?
)

/**
 * 登录响应 VO (含 token)
 *
 * 后端实际响应结构:
 * {
 *   "code": 200,
 *   "data": {
 *     "user": { "id": 4, "username": "user_wang", "realName": "...", ... },
 *     "doctor": null,
 *     "token": "eyJ..."
 *   }
 * }
 *
 * 重要: id 在 user 子对象里, token 平级。
 * 这里用扁平字段映射, 跟前端 TokenStore.saveLogin() 兼容。
 *
 * 用 Gson 反序列化时:
 * 1. 反序列化整个 data 块到 LoginVO 会失败 (结构不匹配)
 * 2. 用 LoginEnvelope 中转 (data: LoginVO) -> LoginVO (user, token)
 * 3. AuthRepository 解析后扁平存 TokenStore
 */
data class LoginVO(
    val user: SysUser? = null,
    val doctor: Any? = null,
    val token: String
) {
    /** 兼容旧 API: 让 HomeFragment 还能用 state.user.realName */
    val id: Long get() = user?.id ?: 0L
    val username: String get() = user?.username ?: ""
    val realName: String? get() = user?.realName
    val phone: String? get() = user?.phone
    val role: String get() = user?.role ?: "USER"
}