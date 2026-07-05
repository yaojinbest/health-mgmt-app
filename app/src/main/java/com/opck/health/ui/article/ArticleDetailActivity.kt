package com.opck.health.ui.article

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.opck.health.HealthApp
import com.opck.health.databinding.ActivityArticleDetailBinding
import kotlinx.coroutines.launch

/**
 * 文章详情 (D5)
 *
 * - 加载单篇文章 (GET /api/articles/{id})
 */
class ArticleDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArticleDetailBinding
    private val app get() = application as HealthApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticleDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val id = intent.getLongExtra("articleId", -1)
        if (id <= 0) {
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                val resp = app.api.getArticle(id)
                val a = resp.data
                if (a != null) {
                    binding.toolbar.title = a.title ?: "文章"
                    binding.tvTitle.text = a.title ?: ""
                    binding.tvCategory.text = a.category ?: "健康"
                    binding.tvSummary.text = a.summary ?: ""
                    binding.tvContent.text = a.content ?: ""
                }
            } catch (e: Exception) {
                binding.tvContent.text = "加载失败: ${e.message}"
            }
        }
    }
}