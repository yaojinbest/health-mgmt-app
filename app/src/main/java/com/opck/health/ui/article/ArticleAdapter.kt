package com.opck.health.ui.article

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.opck.health.data.model.HealthArticle
import com.opck.health.databinding.ItemArticleBinding

class ArticleAdapter(
    private val onClick: (HealthArticle) -> Unit
) : ListAdapter<HealthArticle, ArticleAdapter.VH>(DIFF) {

    class VH(val binding: ItemArticleBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemArticleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val a = getItem(position)
        holder.binding.tvTitle.text = a.title ?: "未命名"
        holder.binding.tvSummary.text = a.summary ?: ""
        holder.binding.tvCategory.text = a.category ?: "健康"
        holder.binding.root.setOnClickListener { onClick(a) }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<HealthArticle>() {
            override fun areItemsTheSame(old: HealthArticle, new: HealthArticle) = old.id == new.id
            override fun areContentsTheSame(old: HealthArticle, new: HealthArticle) = old == new
        }
    }
}