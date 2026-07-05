package com.opck.health.ui.medicine

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.opck.health.R
import com.opck.health.data.model.MedicineRecord
import com.opck.health.databinding.ItemMedicineBinding

class MedicineAdapter(
    private val onFinish: (MedicineRecord) -> Unit
) : ListAdapter<MedicineRecord, MedicineAdapter.VH>(DIFF) {

    class VH(val binding: ItemMedicineBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemMedicineBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        val ctx = holder.binding.root.context

        holder.binding.tvName.text = item.medicineName
        holder.binding.tvDosage.text = "剂量: ${item.dosage ?: "-"}"
        holder.binding.tvMethod.text = "用法: ${item.usageMethod ?: "-"}"
        holder.binding.tvReminder.text = "提醒: ${item.reminderTimes ?: "未设置"}"
        holder.binding.tvDate.text = "${item.startDate ?: "-"} ~ ${item.endDate ?: "进行中"}"

        val active = item.status == "ACTIVE"
        holder.binding.tvStatus.text = if (active) "用药中" else "已结束"
        holder.binding.tvStatus.background = ctx.getDrawable(
            if (active) R.drawable.bg_tag_green else R.drawable.bg_warning_normal
        )
        holder.binding.btnFinish.isEnabled = active
        holder.binding.btnFinish.setOnClickListener { onFinish(item) }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MedicineRecord>() {
            override fun areItemsTheSame(old: MedicineRecord, new: MedicineRecord) = old.id == new.id
            override fun areContentsTheSame(old: MedicineRecord, new: MedicineRecord) = old == new
        }
    }
}