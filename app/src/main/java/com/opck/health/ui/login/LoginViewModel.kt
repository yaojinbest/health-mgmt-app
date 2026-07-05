package com.opck.health.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opck.health.data.model.LoginVO
import com.opck.health.data.model.RegisterRequest
import com.opck.health.data.repository.AuthRepository
import kotlinx.coroutines.launch

/**
 * 登录页 ViewModel
 *
 * UI 状态: Idle / Loading / Success / Error
 */
class LoginViewModel(private val repo: AuthRepository) : ViewModel() {

    private val _state = MutableLiveData<UiState>(UiState.Idle)
    val state: LiveData<UiState> = _state

    fun login(username: String, password: String, role: String? = null) {
        if (username.isBlank() || password.isBlank()) {
            _state.value = UiState.Error("请输入账号和密码")
            return
        }
        _state.value = UiState.Loading
        viewModelScope.launch {
            repo.login(username.trim(), password, role)
                .onSuccess { vo: LoginVO -> _state.value = UiState.Success(vo) }
                .onFailure { e -> _state.value = UiState.Error(e.message ?: "登录失败") }
        }
    }

    fun register(username: String, password: String, realName: String,
                  phone: String?, gender: String?, age: Int?) {
        if (username.isBlank() || password.isBlank() || realName.isBlank()) {
            _state.value = UiState.Error("账号/密码/姓名不能为空")
            return
        }
        _state.value = UiState.Loading
        viewModelScope.launch {
            repo.register(RegisterRequest(
                username = username.trim(),
                password = password,
                realName = realName.trim(),
                phone = phone?.trim()?.ifBlank { null },
                gender = gender?.trim()?.ifBlank { null },
                age = age
            ))
                .onSuccess { vo: LoginVO -> _state.value = UiState.Success(vo) }
                .onFailure { e -> _state.value = UiState.Error(e.message ?: "注册失败") }
        }
    }

    fun resetState() {
        _state.value = UiState.Idle
    }

    sealed class UiState {
        data object Idle : UiState()
        data object Loading : UiState()
        data class Success(val user: LoginVO) : UiState()
        data class Error(val message: String) : UiState()
    }
}
