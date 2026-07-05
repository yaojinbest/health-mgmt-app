package com.opck.health.data.repository

import com.opck.health.data.api.HealthApi
import com.opck.health.data.model.ApiResult
import com.opck.health.data.model.HealthArticle
import com.opck.health.data.model.HealthData

/**
 * 健康数据 + 文章仓库
 *
 * 封装 viewModelScope.launch 之外的 .onSuccess / .onFailure
 *   -> 返回 Kotlin Result, 让 ViewModel 简单处理
 */
class HealthRepository(private val api: HealthApi) {

    suspend fun latestHealth(userId: Long): Result<HealthData?> {
        return runCatching {
            val resp = api.listHealthData(userId)
            (resp.data ?: emptyList()).maxByOrNull {
                it.recordTime ?: ""
            }
        }
    }

    suspend fun listHealth(userId: Long): Result<List<HealthData>> {
        return runCatching {
            val resp = api.listHealthData(userId)
            resp.data ?: emptyList()
        }
    }

    suspend fun latestArticles(limit: Int = 3): Result<List<HealthArticle>> {
        return runCatching {
            val resp = api.listArticles(category = null, keyword = null, diseaseTag = null)
            (resp.data ?: emptyList()).take(limit)
        }
    }
}