package com.opck.health.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.opck.health.HealthApp
import com.opck.health.databinding.ActivityLoginBinding
import com.opck.health.ui.main.MainActivity

/**
 * 登录页 - 入口 Activity
 *
 * 行为对齐 H5 patient portal 登录页:
 * - 用户名 + 密码登录
 * - demo 账号一键填入
 * - 底部链接跳独立 RegisterActivity (D1.x 改造)
 * - 右上角齿轮跳 ServerConfigActivity (D2.x 服务器地址配置)
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(HealthApp.get().authRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 已登录直接进 Main
        if (HealthApp.get().authRepository.isLoggedIn()) {
            goMain()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeState()
        refreshServerHint()
    }

    private fun setupListeners() {
        binding.btnPrimary.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString()
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请输入账号和密码", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.login(username, password, role = null)
        }

        // 跳独立注册页
        binding.tvToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // demo 账号一键填充
        binding.tvDemoHint.setOnClickListener {
            binding.etUsername.setText("user_wang")
            binding.etPassword.setText("root")
        }

        // 齿轮 → 服务器地址设置
        binding.btnServerSettings.setOnClickListener {
            startActivity(Intent(this, ServerConfigActivity::class.java))
        }
    }

    private fun observeState() {
        viewModel.state.observe(this, Observer { state ->
            when (state) {
                is LoginViewModel.UiState.Idle -> {
                    binding.btnPrimary.isEnabled = true
                }
                is LoginViewModel.UiState.Loading -> {
                    binding.btnPrimary.isEnabled = false
                }
                is LoginViewModel.UiState.Success -> {
                    Toast.makeText(this, "欢迎 ${state.user.realName}", Toast.LENGTH_SHORT).show()
                    goMain()
                }
                is LoginViewModel.UiState.Error -> {
                    binding.btnPrimary.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                    viewModel.resetState()
                }
                else -> {}
            }
        })
    }

    override fun onResume() {
        super.onResume()
        refreshServerHint()
    }

    /**
     * 显示当前服务器地址 (齿轮下方小字)
     * 仅当用户自定义过才显示
     */
    private fun refreshServerHint() {
        val serverConfig = HealthApp.get().serverConfig
        if (serverConfig.isCustomized()) {
            val url = serverConfig.getServerUrl()
            // 截短显示: http://192.168.1.100:8090/ → 192.168.1.100:8090
            val display = url.removePrefix("http://").removePrefix("https://").trimEnd('/')
            binding.tvCurrentServer.text = display
            binding.tvCurrentServer.visibility = View.VISIBLE
        } else {
            binding.tvCurrentServer.visibility = View.GONE
        }
    }

    private fun goMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}