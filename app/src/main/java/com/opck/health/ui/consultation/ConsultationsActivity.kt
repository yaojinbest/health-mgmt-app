package com.opck.health.ui.consultation

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.opck.health.HealthApp
import com.opck.health.data.model.Consultation
import com.opck.health.databinding.ActivityConsultationsBinding
import kotlinx.coroutines.launch

/**
 * 在线咨询列表 (D6)
 *
 * - 加载本人咨询列表 (GET /api/consultations)
 * - 新建咨询 (POST /api/consultations/create) — 输入医生 ID + 主题
 * - 点击进 ChatActivity 看/发消息
 */
class ConsultationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConsultationsBinding
    private val app get() = application as HealthApp
    private val adapter by lazy {
        ConsultationAdapter(onClick = { c ->
            c.id?.let { id ->
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("consultationId", id)
                intent.putExtra("title", c.title ?: "咨询 #${id}")
                startActivity(intent)
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConsultationsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.rvConsultations.layoutManager = LinearLayoutManager(this)
        binding.rvConsultations.adapter = adapter

        binding.swipe.setOnRefreshListener { load() }
        binding.fabNew.setOnClickListener { showNewDialog() }
        load()
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() {
        binding.swipe.isRefreshing = true
        lifecycleScope.launch {
            try {
                val userId = app.retrofitClient.tokenStore.getUserId()
                val resp = app.api.listConsultations(userId)
                adapter.submitList(resp.data ?: emptyList())
                binding.swipe.isRefreshing = false
            } catch (e: Exception) {
                binding.swipe.isRefreshing = false
                Snackbar.make(binding.root, "加载失败: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun showNewDialog() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
        }
        val etDoctorId = EditText(this).apply {
            hint = "医生 ID (数字)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        val etTitle = EditText(this).apply {
            hint = "咨询主题 (例: 持续头痛 3 天)"
        }
        container.addView(etDoctorId)
        container.addView(etTitle)

        AlertDialog.Builder(this)
            .setTitle("新建咨询")
            .setView(container)
            .setPositiveButton("发起") { _, _ ->
                val doctorId = etDoctorId.text.toString().toLongOrNull()
                if (doctorId == null) {
                    Snackbar.make(binding.root, "请填有效的医生 ID", Snackbar.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val userId = app.retrofitClient.tokenStore.getUserId()
                val c = Consultation(
                    userId = userId,
                    doctorId = doctorId,
                    title = etTitle.text.toString().ifBlank { "咨询医生 #$doctorId" },
                    status = "OPEN"
                )
                lifecycleScope.launch {
                    try {
                        app.api.createConsultation(c)
                        Snackbar.make(binding.root, "咨询已发起", Snackbar.LENGTH_SHORT).show()
                        load()
                    } catch (e: Exception) {
                        Snackbar.make(binding.root, "发起失败: ${e.message}", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
}