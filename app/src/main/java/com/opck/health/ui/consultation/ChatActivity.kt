package com.opck.health.ui.consultation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.opck.health.HealthApp
import com.opck.health.data.model.ConsultationMessage
import com.opck.health.databinding.ActivityChatBinding
import kotlinx.coroutines.launch

/**
 * 聊天界面 (D6)
 *
 * - 加载消息列表 (GET /api/consultations/{id}/messages)
 * - 发送消息 (POST /api/consultations/message/send)
 * - 按 senderId 判断左右气泡
 */
class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val app get() = application as HealthApp
    private var consultationId: Long = -1
    private lateinit var adapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        consultationId = intent.getLongExtra("consultationId", -1)
        val title = intent.getStringExtra("title") ?: "咨询 #${consultationId}"
        binding.toolbar.title = title
        binding.toolbar.setNavigationOnClickListener { finish() }

        val currentUserId = app.retrofitClient.tokenStore.getUserId()
        adapter = ChatAdapter(currentUserId)

        binding.rvMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true  // 最新消息沉底
        }
        binding.rvMessages.adapter = adapter

        binding.btnSend.setOnClickListener { sendMessage() }
        loadMessages()
    }

    private fun loadMessages() {
        if (consultationId <= 0) return
        lifecycleScope.launch {
            try {
                val resp = app.api.listMessages(consultationId)
                val list = resp.data ?: emptyList()
                adapter.submitList(list) {
                    if (list.isNotEmpty()) {
                        binding.rvMessages.scrollToPosition(list.size - 1)
                    }
                }
            } catch (e: Exception) {
                Snackbar.make(binding.root, "加载消息失败: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun sendMessage() {
        val text = binding.etMessage.text.toString().trim()
        if (text.isEmpty()) return
        if (consultationId <= 0) return

        val senderId = app.retrofitClient.tokenStore.getUserId()
        val msg = ConsultationMessage(
            consultationId = consultationId,
            senderId = senderId,
            senderRole = "USER",
            messageType = "TEXT",
            content = text
        )
        lifecycleScope.launch {
            try {
                app.api.sendMessage(msg)
                binding.etMessage.setText("")
                loadMessages()
            } catch (e: Exception) {
                Snackbar.make(binding.root, "发送失败: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadMessages()
    }
}