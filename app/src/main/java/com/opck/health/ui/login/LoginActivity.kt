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
 * - 默认登录模式
 * - 可切换到注册 (展开注册字段)
 * - 提供 demo 账号提示
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(HealthApp.get().authRepository)
    }

    private var isRegisterMode = false

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
    }

    private fun setupListeners() {
        binding.btnPrimary.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            if (isRegisterMode) {
                val realName = binding.etRealName.text.toString()
                val phone = binding.etPhone.text.toString()
                val gender = binding.etGender.text.toString()
                val age = binding.etAge.text.toString().toIntOrNull()
                viewModel.register(username, password, realName, phone, gender, age)
            } else {
                viewModel.login(username, password, role = null)
            }
        }

        binding.btnToggleMode.setOnClickListener {
            isRegisterMode = !isRegisterMode
            applyMode()
        }

        // demo 账号一键填充
        binding.tvDemoHint.setOnClickListener {
            binding.etUsername.setText("user_wang")
            binding.etPassword.setText("root")
        }
    }

    private fun applyMode() {
        binding.registerFields.visibility = if (isRegisterMode) View.VISIBLE else View.GONE
        binding.btnPrimary.text = if (isRegisterMode) "注册并登录" else "登录"
        binding.btnToggleMode.text = if (isRegisterMode) "已有账号，去登录" else "新用户注册"
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

    private fun goMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
