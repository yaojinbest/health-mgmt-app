package com.opck.health.ui.archive

import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.opck.health.HealthApp
import com.opck.health.data.model.HealthArchive
import com.opck.health.databinding.ActivityArchiveBinding
import kotlinx.coroutines.launch

/**
 * 健康档案 Activity (D4)
 *
 * - 加载本人档案 (GET /api/archive/{userId})
 * - 展示基本信息 + 病史 + 过敏史
 * - 编辑病史/过敏 → POST /api/archive/save
 */
class ArchiveActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArchiveBinding
    private val app get() = application as HealthApp
    private var current: HealthArchive? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArchiveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnEdit.setOnClickListener { showEditDialog() }

        load()
    }

    private fun load() {
        lifecycleScope.launch {
            val userId = app.retrofitClient.tokenStore.getUserId()
            val resp = app.api.getArchive(userId)
            val a = resp.data
            current = a
            if (a != null) {
                binding.tvRealName.text = a.name ?: "-"
                binding.tvGender.text = a.gender ?: "-"
                binding.tvAge.text = a.age?.toString() ?: "-"
                binding.tvPhone.text = "-"  // 后端 HealthArchive 无 phone 字段
                binding.tvHistory.text = a.diseaseHistory ?: "暂无"
                binding.tvAllergy.text = a.allergyHistory ?: "暂无"
            } else {
                Snackbar.make(binding.root, "尚未建档，可点击下方按钮补充", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun showEditDialog() {
        val ctx = this
        val container = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
        }
        val etHistory = EditText(ctx).apply {
            hint = "病史 (例: 高血压 5 年)"
            setText(current?.diseaseHistory ?: "")
        }
        val etAllergy = EditText(ctx).apply {
            hint = "过敏史 (例: 青霉素)"
            setText(current?.allergyHistory ?: "")
        }
        container.addView(etHistory)
        container.addView(etAllergy)

        AlertDialog.Builder(ctx)
            .setTitle("编辑病史/过敏")
            .setView(container)
            .setPositiveButton("保存") { _, _ ->
                val userId = app.retrofitClient.tokenStore.getUserId()
                val updated = (current ?: HealthArchive()).copy(
                    userId = userId,
                    diseaseHistory = etHistory.text.toString().ifBlank { null },
                    allergyHistory = etAllergy.text.toString().ifBlank { null }
                )
                lifecycleScope.launch {
                    try {
                        val resp = app.api.saveArchive(updated)
                        if (resp.code == 200) {
                            Snackbar.make(binding.root, "保存成功", Snackbar.LENGTH_SHORT).show()
                            load()
                        }
                    } catch (e: Exception) {
                        Snackbar.make(binding.root, "保存失败: ${e.message}", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
}