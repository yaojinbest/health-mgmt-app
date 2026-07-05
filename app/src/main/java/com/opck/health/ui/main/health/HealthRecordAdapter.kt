package com.opck.health.ui.main.health

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.opck.health.R
import com.opck.health.data.model.HealthData
import com.opck.health.databinding.ItemHealthRecordBinding
import java.math.BigDecimal

/**
 * 健康数据历史记录 RecyclerView Adapter (D3)
 *
 * - ListAdapter + DiffUtil 自动增量刷新
 * - 字段空时显示 "--" 不闪退
 * - 警告级别用颜色 tag (绿/红)
 */
class HealthRecordAdapter : ListAdapter<HealthData, HealthRecordAdapter.VH>(DIFF) {

    class VH(val binding: ItemHealthRecordBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemHealthRecordBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        val ctx = holder.binding.root.context

        holder.binding.tvTime.text = item.recordTime?.replace("T", " ") ?: "--"

        // 警告 tag
        val isWarn = item.warningLevel == "WARN"
        holder.binding.tvWarning.text = if (isWarn) "警告" else "正常"
        holder.binding.tvWarning.background = ctx.getDrawable(
            if (isWarn) R.drawable.bg_warning_normal else R.drawable.bg_tag_green
        )

        holder.binding.tvBp.text = if (item.systolic != null && item.diastolic != null) {
            "血压 ${item.systolic}/${item.diastolic} mmHg"
        } else "血压 --"

        holder.binding.tvSugar.text = "血糖 ${formatDecimal(item.bloodSugar)} mmol/L"

        holder.binding.tvHeartRate.text = if (item.heartRate != null) {
            "心率 ${item.heartRate} bpm"
        } else "心率 --"

        holder.binding.tvSteps.text = if (item.steps != null) {
            "步数 ${item.steps}"
        } else "步数 --"
    }

    private fun formatDecimal(value: BigDecimal?): String {
        return value?.toPlainString() ?: "--"
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<HealthData>() {
            override fun areItemsTheSame(old: HealthData, new: HealthData) = old.id == new.id
            override fun areContentsTheSame(old: HealthData, new: HealthData) = old == new
        }
    }
}