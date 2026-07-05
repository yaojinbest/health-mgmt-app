package com.opck.health.ui.consultation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.opck.health.R
import com.opck.health.data.model.Consultation
import com.opck.health.databinding.ItemConsultationBinding

class ConsultationAdapter(
    private val onClick: (Consultation) -> Unit
) : ListAdapter<Consultation, ConsultationAdapter.VH>(DIFF) {

    class VH(val binding: ItemConsultationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemConsultationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val c = getItem(position)
        val ctx = holder.binding.root.context

        holder.binding.tvTitle.text = c.title ?: "咨询 #${c.id ?: "-"}"
        holder.binding.tvDoctor.text = "医生 ID: ${c.doctorId ?: "-"}"
        holder.binding.tvTime.text = c.createTime?.replace("T", " ") ?: "-"

        val open = c.status == "OPEN"
        holder.binding.tvStatus.text = if (open) "进行中" else "已结束"
        holder.binding.tvStatus.background = ctx.getDrawable(
            if (open) R.drawable.bg_tag_green else R.drawable.bg_warning_normal
        )
        holder.binding.root.setOnClickListener { onClick(c) }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Consultation>() {
            override fun areItemsTheSame(old: Consultation, new: Consultation) = old.id == new.id
            override fun areContentsTheSame(old: Consultation, new: Consultation) = old == new
        }
    }
}