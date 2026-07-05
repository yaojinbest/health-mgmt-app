package com.opck.health.ui.widget

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.opck.health.data.model.HealthArticle
import com.opck.health.databinding.ItemHomeArticleBinding

/**
 * 首页文章 RecyclerView 适配器 (D2)
 *
 * 单行横滑风格: 标题 + 摘要 + 分类 + 时间
 */
class HomeArticleAdapter : RecyclerView.Adapter<HomeArticleAdapter.VH>() {

    private val items = mutableListOf<HealthArticle>()

    fun submit(list: List<HealthArticle>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemHomeArticleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    class VH(private val binding: ItemHomeArticleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(article: HealthArticle) {
            binding.tvTitle.text = article.title
            binding.tvSummary.text = article.summary ?: ""
            binding.tvCategory.text = article.category ?: "健康"
            binding.tvPublishTime.text = formatTime(article.publishTime)
        }

        private fun formatTime(t: String?): String {
            if (t.isNullOrBlank()) return ""
            // 后端 LocalDateTime -> ISO 8601 (e.g., "2026-07-05T12:00:00")
            return t.replace("T", " ").take(10)
        }
    }
}