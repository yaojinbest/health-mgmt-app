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
 * 注意：后端字段是 snake_case (real_name), GSON 会用 @SerializedName 映射
 */
data class LoginVO(
    val id: Long,
    val username: String,
    val realName: String?,
    val phone: String?,
    val role: String,
    val gender: String?,
    val age: Int?,
    val token: String
)
