package com.opck.health.ui.consultation

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.opck.health.HealthApp
import com.opck.health.data.model.Consultation
import com.opck.health.databinding.FragmentConsultBinding
import kotlinx.coroutines.launch

/**
 * Consult tab (替换 PlaceholderFragment "D6 完成")
 *
 * - 加载本人咨询列表 (GET /api/consultations)
 * - 新建咨询 (POST /api/consultations/create) — 弹窗输入医生 ID + 主题
 * - 点击进 ChatActivity 看/发消息
 */
class ConsultFragment : Fragment() {

    private var _binding: FragmentConsultBinding? = null
    private val binding get() = _binding!!
    private val app get() = requireActivity().application as HealthApp

    private val adapter by lazy {
        ConsultationAdapter(onClick = { c ->
            c.id?.let { id ->
                val intent = Intent(requireContext(), ChatActivity::class.java)
                intent.putExtra("consultationId", id)
                intent.putExtra("title", c.title ?: "咨询 #${id}")
                startActivity(intent)
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConsultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvConsultations.layoutManager = LinearLayoutManager(requireContext())
        binding.rvConsultations.adapter = adapter
        binding.swipe.setOnRefreshListener { load() }
        binding.fabNew.setOnClickListener { showNewDialog() }
        load()
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun load() {
        binding.swipe.isRefreshing = true
        lifecycleScope.launch {
            try {
                val userId = app.retrofitClient.tokenStore.getUserId()
                val resp = app.retrofitClient.api().listConsultations(userId)
                val list = resp.data ?: emptyList()
                adapter.submitList(list)
                binding.swipe.isRefreshing = false
                binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                binding.swipe.isRefreshing = false
                Snackbar.make(binding.root, "加载失败: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun showNewDialog() {
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
        }
        val etDoctorId = EditText(requireContext()).apply {
            hint = "医生 ID (数字)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        val etTitle = EditText(requireContext()).apply {
            hint = "咨询主题 (例: 持续头痛 3 天)"
        }
        container.addView(etDoctorId)
        container.addView(etTitle)

        AlertDialog.Builder(requireContext())
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
                        app.retrofitClient.api().createConsultation(c)
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