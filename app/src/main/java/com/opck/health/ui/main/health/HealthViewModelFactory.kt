package com.opck.health.ui.main.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.opck.health.HealthApp

class HealthViewModelFactory(private val app: HealthApp) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HealthViewModel::class.java)) {
            return HealthViewModel(app.repository, app.retrofitClient.tokenStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}