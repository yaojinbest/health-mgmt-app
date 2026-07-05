package com.opck.health.ui.emergency

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.opck.health.data.model.EmergencyContact
import com.opck.health.databinding.ItemContactBinding

class ContactAdapter(
    private val onDelete: (EmergencyContact) -> Unit
) : ListAdapter<EmergencyContact, ContactAdapter.VH>(DIFF) {

    class VH(val binding: ItemContactBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemContactBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val c = getItem(position)
        holder.binding.tvName.text = c.name
        holder.binding.tvRelation.text = "关系: ${c.relation ?: "-"}"
        holder.binding.tvPhone.text = "电话: ${c.phone}"
        holder.binding.btnDelete.setOnClickListener { onDelete(c) }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<EmergencyContact>() {
            override fun areItemsTheSame(old: EmergencyContact, new: EmergencyContact) = old.id == new.id
            override fun areContentsTheSame(old: EmergencyContact, new: EmergencyContact) = old == new
        }
    }
}