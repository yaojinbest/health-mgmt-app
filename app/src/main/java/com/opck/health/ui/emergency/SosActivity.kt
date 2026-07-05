package com.opck.health.ui.emergency

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.opck.health.HealthApp
import com.opck.health.data.model.EmergencyRecord
import com.opck.health.databinding.ActivitySosBinding
import kotlinx.coroutines.launch

/**
 * 紧急求救 (D5)
 *
 * - 大 SOS 按钮 → POST /api/emergency/help → 显示已发起
 * - 紧急联系人列表 (复用 ContactAdapter)
 * - 点击联系人 → 拨号 (Intent.ACTION_DIAL)
 * - 历史求救记录
 */
class SosActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySosBinding
    private val app get() = application as HealthApp
    private val contactAdapter by lazy {
        ContactAdapter(onDelete = { /* 在主页面只读, 不删 */ })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.rvContacts.layoutManager = LinearLayoutManager(this)
        binding.rvContacts.adapter = contactAdapter

        binding.btnSos.setOnClickListener { triggerSos() }
        binding.btnManageContacts.setOnClickListener {
            startActivity(Intent(this, ContactsActivity::class.java))
        }

        loadContacts()
    }

    override fun onResume() {
        super.onResume()
        loadContacts()
    }

    private fun loadContacts() {
        lifecycleScope.launch {
            try {
                val resp = app.api.listContacts()
                val list = resp.data ?: emptyList()
                contactAdapter.submitList(list)
                if (list.isEmpty()) {
                    Snackbar.make(binding.root, "请先添加紧急联系人", Snackbar.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Snackbar.make(binding.root, "加载联系人失败: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun triggerSos() {
        AlertDialog.Builder(this)
            .setTitle("确认发起紧急求救")
            .setMessage("将通知所有紧急联系人, 请确认是否真的需要帮助?")
            .setPositiveButton("确认求救") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val payload = mapOf(
                            "userId" to app.retrofitClient.tokenStore.getUserId(),
                            "location" to "",
                            "description" to "通过 APP SOS 按钮发起"
                        ).let { m ->
                            EmergencyRecord(
                                userId = app.retrofitClient.tokenStore.getUserId(),
                                locationText = "通过 APP SOS 按钮发起"
                            )
                        }
                        app.api.sendSos(payload)
                        Snackbar.make(binding.root, "求救信号已发出, 请保持手机畅通", Snackbar.LENGTH_LONG).show()
                        // 自动跳到拨号面板, 让用户拨打第一个联系人
                        val firstContact = contactAdapter.currentList.firstOrNull()
                        if (firstContact != null) {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${firstContact.phone}"))
                                startActivity(intent)
                            } catch (_: Exception) { /* 无拨号器时不报错 */ }
                        }
                    } catch (e: Exception) {
                        Snackbar.make(binding.root, "求救发起失败: ${e.message}", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
}