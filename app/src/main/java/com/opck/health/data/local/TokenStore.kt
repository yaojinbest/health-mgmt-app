package com.opck.health.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Token 持久化 - 用 SharedPreferences (简单够用)
 *
 * 生产建议改 EncryptedSharedPreferences (但需要 androidx.security 依赖)
 * 演示项目 SharedPreferences 足够
 */
class TokenStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * 存储登录后的 token + 用户信息
     */
    fun saveLogin(token: String, userId: Long, username: String, role: String, realName: String?) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_USERNAME, username)
            .putString(KEY_ROLE, role)
            .putString(KEY_REAL_NAME, realName)
            .apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)
    fun getUserId(): Long = prefs.getLong(KEY_USER_ID, 0L)
    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)
    fun getRole(): String? = prefs.getString(KEY_ROLE, null)
    fun getRealName(): String? = prefs.getString(KEY_REAL_NAME, null)

    fun isLoggedIn(): Boolean = !getToken().isNullOrEmpty()

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "health_auth"
        private const val KEY_TOKEN = "token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_ROLE = "role"
        private const val KEY_REAL_NAME = "real_name"
    }
}
