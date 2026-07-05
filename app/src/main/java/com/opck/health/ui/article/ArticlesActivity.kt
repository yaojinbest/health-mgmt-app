package com.opck.health.ui.article

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.opck.health.HealthApp
import com.opck.health.databinding.ActivityArticlesBinding
import kotlinx.coroutines.launch

/**
 * 健康文章列表 (D5)
 *
 * - 加载文章列表 (GET /api/articles)
 * - 搜索关键词
 * - 点击看 ArticleDetailActivity
 */
class ArticlesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArticlesBinding
    private val app get() = application as HealthApp
    private val adapter by lazy {
        ArticleAdapter(onClick = { article ->
            val intent = Intent(this, ArticleDetailActivity::class.java)
            intent.putExtra("articleId", article.id ?: -1)
            startActivity(intent)
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticlesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.rvArticles.layoutManager = LinearLayoutManager(this)
        binding.rvArticles.adapter = adapter

        binding.btnSearch.setOnClickListener { load() }
        load()
    }

    private fun load() {
        val keyword = binding.etSearch.text.toString().ifBlank { null }
        lifecycleScope.launch {
            try {
                val resp = app.api.listArticles(category = null, keyword = keyword, diseaseTag = null)
                adapter.submitList(resp.data ?: emptyList())
                if (adapter.itemCount == 0) {
                    Snackbar.make(binding.root, "暂无文章", Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Snackbar.make(binding.root, "加载失败: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }
}