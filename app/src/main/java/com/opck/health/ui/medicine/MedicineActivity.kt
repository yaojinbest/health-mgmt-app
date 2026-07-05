package com.opck.health.ui.medicine

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.opck.health.HealthApp
import com.opck.health.data.model.MedicineRecord
import com.opck.health.databinding.ActivityMedicineBinding
import com.opck.health.databinding.DialogMedicineInputBinding
import kotlinx.coroutines.launch

/**
 * 用药管理 Activity (D4)
 *
 * - 加载本人用药列表 (GET /api/medicine/list)
 * - 状态过滤 (用药中/已结束/全部)
 * - "新建用药" 弹窗 → POST /api/medicine/save
 * - "结束用药" → POST /api/medicine/finish/{id}
 */
class MedicineActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicineBinding
    private val app get() = application as HealthApp
    private val adapter by lazy {
        MedicineAdapter(onFinish = { record ->
            record.id?.let { id ->
                finishMedicine(id)
            }
        })
    }

    private var statusFilter: String = "ACTIVE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.rvMedicine.layoutManager = LinearLayoutManager(this)
        binding.rvMedicine.adapter = adapter

        binding.swipe.setOnRefreshListener { loadList() }
        binding.fabAdd.setOnClickListener { showInputDialog() }

        binding.chipActive.setOnClickListener { statusFilter = "ACTIVE"; loadList() }
        binding.chipFinished.setOnClickListener { statusFilter = "FINISHED"; loadList() }
        binding.chipAll.setOnClickListener { statusFilter = ""; loadList() }

        loadList()
    }

    private fun loadList() {
        binding.swipe.isRefreshing = true
        lifecycleScope.launch {
            val userId = app.retrofitClient.tokenStore.getUserId()
            val resp = app.api.listMedicines(userId = userId)
            val all = (resp.data ?: emptyList()).filter {
                statusFilter.isEmpty() || it.status == statusFilter
            }
            adapter.submitList(all)
            binding.swipe.isRefreshing = false
            if (all.isEmpty()) {
                Snackbar.make(binding.root, "暂无用药记录", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun showInputDialog() {
        val dlg = DialogMedicineInputBinding.inflate(layoutInflater)
        AlertDialog.Builder(this)
            .setView(dlg.root)
            .setPositiveButton("保存") { _, _ ->
                val name = dlg.etMedicineName.text.toString().trim()
                if (name.isEmpty()) {
                    Snackbar.make(binding.root, "请填药品名称", Snackbar.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val userId = app.retrofitClient.tokenStore.getUserId()
                val record = MedicineRecord(
                    userId = userId,
                    medicineName = name,
                    usageMethod = dlg.etUsageMethod.text.toString().ifBlank { null },
                    dosage = dlg.etDosage.text.toString().ifBlank { null },
                    reminderTimes = dlg.etReminderTimes.text.toString().ifBlank { null },
                    startDate = dlg.etStartDate.text.toString().ifBlank { null },
                    endDate = dlg.etEndDate.text.toString().ifBlank { null },
                    status = "ACTIVE"
                )
                saveMedicine(record)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun saveMedicine(record: MedicineRecord) {
        lifecycleScope.launch {
            val resp = app.api.saveMedicine(record)
            if (resp.code == 200) {
                Snackbar.make(binding.root, "保存成功", Snackbar.LENGTH_SHORT).show()
                loadList()
            } else {
                Snackbar.make(binding.root, "保存失败: ${resp.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun finishMedicine(id: Long) {
        lifecycleScope.launch {
            try {
                app.api.finishMedicine(id)
                Snackbar.make(binding.root, "已结束用药", Snackbar.LENGTH_SHORT).show()
                loadList()
            } catch (e: Exception) {
                Snackbar.make(binding.root, "结束失败: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }
}