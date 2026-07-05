package com.opck.health.ui.login

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.opck.health.HealthApp
import com.opck.health.data.local.ServerConfig
import com.opck.health.databinding.ActivityServerConfigBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * 服务器地址设置页
 *
 * 入口: 登录页右上角齿轮图标
 * 功能:
 * - 显示当前生效地址
 * - 4 个快捷预设按钮 (模拟器 / 本机 / 局域网 / 自定义)
 * - 自定义输入 + 自动补 scheme
 * - 测试连接按钮 (OkHttp HEAD 探活)
 * - 保存并应用到运行时 Retrofit
 */
class ServerConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServerConfigBinding
    private lateinit var serverConfig: ServerConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServerConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        serverConfig = HealthApp.get().serverConfig

        setupToolbar()
        refreshCurrentUrl()

        binding.btnPresetEmulator.setOnClickListener { applyPreset("http://10.0.2.2:8090/") }
        binding.btnPresetLocalhost.setOnClickListener { applyPreset("http://127.0.0.1:8090/") }
        binding.btnPresetLAN.setOnClickListener {
            binding.etUrl.setText("http://192.168.1.")
            binding.etUrl.setSelection(binding.etUrl.text?.length ?: 0)
            binding.etUrl.requestFocus()
        }

        binding.btnTest.setOnClickListener { testConnection() }
        binding.btnSave.setOnClickListener { saveAndApply() }
        binding.btnReset.setOnClickListener { resetToDefault() }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun refreshCurrentUrl() {
        val current = serverConfig.getServerUrl()
        binding.tvCurrentUrl.text = current
        binding.etUrl.setText(current)
    }

    private fun applyPreset(url: String) {
        binding.etUrl.setText(url)
        binding.etUrl.setSelection(url.length)
        binding.tvStatus.text = "已填入预设: $url"
        binding.tvStatus.setTextColor(getColor(com.opck.health.R.color.primary))
    }

    /**
     * 探活用 OkHttp HEAD 请求 (不依赖 Retrofit, 避免切换 baseUrl 的副作用)
     */
    private fun testConnection() {
        val raw = binding.etUrl.text?.toString().orEmpty()
        val url = ServerConfig.normalize(raw)
        if (url == null) {
            showError("URL 格式不合法")
            return
        }

        binding.btnTest.isEnabled = false
        binding.tvStatus.text = "🔄 正在测试连接: $url"
        binding.tvStatus.setTextColor(getColor(com.opck.health.R.color.primary))

        lifecycleScope.launch {
            val (ok, msg) = withContext(Dispatchers.IO) {
                probeHttp(url)
            }
            binding.btnTest.isEnabled = true
            if (ok) {
                binding.tvStatus.text = "✅ 连接成功: $msg"
                binding.tvStatus.setTextColor(getColor(com.opck.health.R.color.success))
            } else {
                binding.tvStatus.text = "❌ 连接失败: $msg"
                binding.tvStatus.setTextColor(getColor(com.opck.health.R.color.danger))
            }
        }
    }

    private fun probeHttp(url: String): Pair<Boolean, String> {
        val client = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()
        val req = Request.Builder()
            .url(url + "actuator/health")
            .head()
            .build()
        return try {
            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful || resp.code == 404) {
                    // Spring Boot actuator/health 200 OK; 没装 actuator 404 也算"服务器活着"
                    true to "HTTP ${resp.code}"
                } else {
                    false to "HTTP ${resp.code}"
                }
            }
        } catch (e: Exception) {
            Log.w("ServerConfig", "probe fail", e)
            false to (e.javaClass.simpleName + ": " + (e.message ?: "unknown"))
        }
    }

    private fun saveAndApply() {
        val raw = binding.etUrl.text?.toString().orEmpty()
        val normalized = ServerConfig.normalize(raw)
        if (normalized == null) {
            showError("URL 格式不合法, 必须是 http(s)://host:port/ 形式")
            return
        }
        serverConfig.setServerUrl(normalized)
        // 重建 Retrofit 实例 (下次 api() 自动用新 baseUrl)
        HealthApp.get().retrofitClient.recreate()
        Toast.makeText(this, "✅ 已保存: $normalized", Toast.LENGTH_LONG).show()
        finish()
    }

    private fun resetToDefault() {
        serverConfig.resetToDefault()
        HealthApp.get().retrofitClient.recreate()
        refreshCurrentUrl()
        Toast.makeText(this, "已重置为默认值", Toast.LENGTH_SHORT).show()
        binding.tvStatus.text = ""
    }

    private fun showError(msg: String) {
        binding.tilUrl.error = msg
        binding.tvStatus.text = "❌ $msg"
        binding.tvStatus.setTextColor(getColor(com.opck.health.R.color.danger))
    }
}