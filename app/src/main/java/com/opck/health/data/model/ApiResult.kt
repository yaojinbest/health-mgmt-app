package com.opck.health.data.model

/**
 * 后端统一 Result<T> 格式
 * { "code": 200, "message": "ok", "data": T }
 */
data class ApiResult<T>(
    val code: Int,
    val message: String?,
    val data: T?
)

/**
 * UI 层业务错误包装
 */
sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(val code: Int, val message: String) : AppResult<Nothing>()
    data object Loading : AppResult<Nothing>()
}
