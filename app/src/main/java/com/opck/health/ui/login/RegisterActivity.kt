package com.opck.health.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.opck.health.HealthApp
import com.opck.health.databinding.ActivityRegisterBinding
import com.opck.health.ui.main.MainActivity

/**
 * 注册页 - 独立 Activity (D1.x UI 调整)
 *
 * 字段: 用户名 / 密码 / 真实姓名 / 手机 / 性别 / 年龄
 * 注册成功后自动登录跳主页
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(HealthApp.get().authRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeState()
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val realName = binding.etRealName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val gender = binding.etGender.text.toString().trim()
            val age = binding.etAge.text.toString().toIntOrNull()

            // 前端基本校验
            if (username.length < 3) {
                binding.etUsername.error = "至少 3 位"; return@setOnClickListener
            }
            if (password.length < 6) {
                binding.etPassword.error = "至少 6 位"; return@setOnClickListener
            }
            if (realName.isEmpty()) {
                binding.etRealName.error = "请填写真实姓名"; return@setOnClickListener
            }

            viewModel.register(username, password, realName, phone, gender, age)
        }

        binding.tvToLogin.setOnClickListener {
            finish()  // 直接关闭, 回到登录页 (LoginActivity 在栈底)
        }
    }

    private fun observeState() {
        viewModel.state.observe(this, Observer { state ->
            when (state) {
                is LoginViewModel.UiState.Idle -> {
                    binding.btnRegister.isEnabled = true
                }
                is LoginViewModel.UiState.Loading -> {
                    binding.btnRegister.isEnabled = false
                }
                is LoginViewModel.UiState.Success -> {
                    Toast.makeText(this, "注册成功，欢迎 ${state.user.realName}", Toast.LENGTH_SHORT).show()
                    goMain()
                }
                is LoginViewModel.UiState.Error -> {
                    binding.btnRegister.isEnabled = true
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