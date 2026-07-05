package com.opck.health.ui.consultation

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.opck.health.R
import com.opck.health.data.model.ConsultationMessage
import com.opck.health.databinding.ItemChatMessageBinding

class ChatAdapter(
    private val currentUserId: Long
) : ListAdapter<ConsultationMessage, ChatAdapter.VH>(DIFF) {

    class VH(val binding: ItemChatMessageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val m = getItem(position)
        val ctx = holder.binding.root.context
        val isMe = m.senderId == currentUserId

        holder.binding.tvSender.text = if (isMe) "我 (${m.senderRole ?: "USER"})"
        else "${m.senderRole ?: "DOCTOR"} #${m.senderId ?: "-"}"
        holder.binding.tvContent.text = m.content ?: ""
        holder.binding.tvTime.text = m.sendTime?.replace("T", " ")?.substring(11, 16) ?: ""

        if (isMe) {
            holder.binding.root.gravity = Gravity.END
            holder.binding.tvContent.background = ctx.getDrawable(R.drawable.bg_tag_green)
        } else {
            holder.binding.root.gravity = Gravity.START
            holder.binding.tvContent.setBackgroundColor(0xFFE0E0E0.toInt())
            holder.binding.tvContent.setTextColor(0xFF333333.toInt())
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ConsultationMessage>() {
            override fun areItemsTheSame(old: ConsultationMessage, new: ConsultationMessage) = old.id == new.id
            override fun areContentsTheSame(old: ConsultationMessage, new: ConsultationMessage) = old == new
        }
    }
}