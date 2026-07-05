package com.opck.health.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Patterns
import com.opck.health.BuildConfig

/**
 * 服务器地址配置 (运行时 baseUrl 切换)
 *
 * 设计:
 * - SharedPreferences 持久化 (key = config_server_url)
 * - 默认值 = BuildConfig.API_BASE_URL (编译期注入)
 * - 运行时可改 (RetrofitClient.recreate() 重建)
 *
 * 三种使用场景:
 * - 模拟器: http://10.0.2.2:8090/
 * - 真机局域网: http://192.168.1.100:8090/
 * - 服务器部署: https://api.example.com/
 *
 * 跟 TokenStore 同包, 风格一致 (课程项目简化版)
 */
class ServerConfig(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * 读取当前 baseUrl
     * - 优先从 SharedPreferences (用户运行时设置过)
     * - 否则返回 BuildConfig 默认值
     */
    fun getServerUrl(): String {
        val saved = prefs.getString(KEY_SERVER_URL, null)
        return if (saved.isNullOrBlank()) BuildConfig.API_BASE_URL else saved
    }

    /**
     * 保存 baseUrl (校验合法性)
     * @return true=保存成功, false=URL 非法
     */
    fun setServerUrl(url: String): Boolean {
        val normalized = normalize(url)
        if (normalized == null) return false
        prefs.edit().putString(KEY_SERVER_URL, normalized).apply()
        return true
    }

    /**
     * 清除用户设置, 回到 BuildConfig 默认值
     */
    fun resetToDefault() {
        prefs.edit().remove(KEY_SERVER_URL).apply()
    }

    /**
     * 是否使用过自定义配置 (用于登录页显示 "已配置" 提示)
     */
    fun isCustomized(): Boolean {
        return !prefs.getString(KEY_SERVER_URL, null).isNullOrBlank()
    }

    companion object {
        private const val PREFS_NAME = "health_mgmt_config"
        private const val KEY_SERVER_URL = "config_server_url"

        /**
         * 快捷预设值 (在 ServerConfigActivity 用作 QuickPick 按钮)
         */
        val PRESETS = listOf(
            Preset("模拟器 (Android 默认)", "http://10.0.2.2:8090/"),
            Preset("本机回环 (127.0.0.1)", "http://127.0.0.1:8090/"),
            Preset("真机局域网", "http://192.168.1.100:8090/"),
            Preset("公网 (HTTPS)", "https://api.example.com/")
        )

        /**
         * URL 规范化 + 校验
         * @return 规范化后的 URL, 非法返回 null
         *
         * 规则:
         * 1. trim 首尾空格
         * 2. 缺 scheme 自动补 http://
         * 3. host 提取后 (去除 :port) 校验: localhost / IP / 含 . / 10.0.2.*
         * 4. path 强制以 / 结尾 (Retrofit 要求)
         */
        fun normalize(raw: String): String? {
            val trimmed = raw.trim()
            if (trimmed.isEmpty()) return null

            val withScheme = if (trimmed.startsWith("http://", ignoreCase = true) ||
                                trimmed.startsWith("https://", ignoreCase = true)) {
                trimmed
            } else {
                "http://$trimmed"
            }

            val match = Regex("^(https?)://([^/\\s]+)(/.*)?$", RegexOption.IGNORE_CASE).matchEntire(withScheme)
            if (match == null) return null

            val hostPort = match.groupValues[2]
            // 拆 host 和 port (只校验 host, 不校验 port)
            val host = if (':' in hostPort && !hostPort.startsWith('[')) {
                hostPort.substringBeforeLast(':')
            } else {
                hostPort
            }
            val validHost = Patterns.IP_ADDRESS.matcher(host).matches() ||
                           host.equals("localhost", ignoreCase = true) ||
                           host.contains('.') ||
                           host.startsWith("10.0.2.")
            if (!validHost) return null

            val pathPart = match.groupValues[3].ifEmpty { "/" }
            val withSlash = if (pathPart.endsWith("/")) pathPart else "$pathPart/"

            return "${match.groupValues[1]}://$hostPort$withSlash"
        }

        data class Preset(val label: String, val url: String)
    }
}