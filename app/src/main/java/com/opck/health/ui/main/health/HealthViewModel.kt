package com.opck.health.ui.main.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opck.health.data.local.TokenStore
import com.opck.health.data.model.HealthData
import com.opck.health.data.repository.HealthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

/**
 * Health tab ViewModel (D3)
 *
 * - 加载历史健康数据
 * - 保存新数据
 * - 暴露 UI 状态 (loading / error / success)
 */
class HealthViewModel(
    private val repository: HealthRepository,
    private val tokenStore: TokenStore
) : ViewModel() {

    private val _state = MutableStateFlow<UiState>(UiState.Idle)
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _history = MutableStateFlow<List<HealthData>>(emptyList())
    val history: StateFlow<List<HealthData>> = _history.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            val userId = tokenStore.getUserId()
            repository.listHealth(userId)
                .onSuccess {
                    _history.value = it
                    _state.value = UiState.Loaded
                }
                .onFailure {
                    _state.value = UiState.Error(it.message ?: "加载失败")
                }
        }
    }

    fun save(
        systolic: Int?,
        diastolic: Int?,
        bloodSugar: BigDecimal?,
        heartRate: Int?,
        steps: Int?,
        sleepHours: BigDecimal?,
        weight: BigDecimal?
    ) {
        viewModelScope.launch {
            _state.value = UiState.Saving
            val userId = tokenStore.getUserId()
            val data = HealthData(
                userId = userId,
                systolic = systolic,
                diastolic = diastolic,
                bloodSugar = bloodSugar,
                heartRate = heartRate,
                steps = steps,
                sleepHours = sleepHours,
                weight = weight
            )
            repository.saveHealth(data)
                .onSuccess {
                    _state.value = UiState.Saved
                    load()  // 重新加载历史
                }
                .onFailure {
                    _state.value = UiState.Error(it.message ?: "保存失败")
                }
        }
    }

    sealed class UiState {
        data object Idle : UiState()
        data object Loading : UiState()
        data object Loaded : UiState()
        data object Saving : UiState()
        data object Saved : UiState()
        data class Error(val message: String) : UiState()
    }
}