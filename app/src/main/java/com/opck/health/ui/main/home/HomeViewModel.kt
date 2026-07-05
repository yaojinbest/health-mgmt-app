package com.opck.health.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opck.health.data.model.HealthArticle
import com.opck.health.data.model.HealthData
import com.opck.health.data.repository.HealthRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * Home ViewModel - 并行拉取最新健康数据 + 最新文章
 */
class HomeViewModel(private val repo: HealthRepository) : ViewModel() {

    private val _state = MutableLiveData<UiState>(UiState.Loading)
    val state: LiveData<UiState> = _state

    fun load(userId: Long) {
        _state.value = UiState.Loading
        viewModelScope.launch {
            val healthDeferred = async { repo.latestHealth(userId) }
            val articlesDeferred = async { repo.latestArticles(limit = 3) }

            val healthResult = healthDeferred.await()
            val articlesResult = articlesDeferred.await()

            val latest = healthResult.getOrNull()
            val articles = articlesResult.getOrDefault(emptyList())

            if (healthResult.isFailure && articlesResult.isFailure) {
                _state.value = UiState.Error(
                    healthResult.exceptionOrNull()?.message ?: "加载失败"
                )
            } else {
                _state.value = UiState.Loaded(latest, articles)
            }
        }
    }

    sealed class UiState {
        data object Loading : UiState()
        data class Loaded(val latest: HealthData?, val articles: List<HealthArticle>) : UiState()
        data class Error(val message: String) : UiState()
    }
}