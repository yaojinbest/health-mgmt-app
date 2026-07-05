package com.opck.health.ui.emergency

import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.opck.health.HealthApp
import com.opck.health.data.model.EmergencyContact
import com.opck.health.databinding.ActivityContactsBinding
import kotlinx.coroutines.launch

/**
 * 紧急联系人管理 (D5)
 *
 * - 列表 (GET /api/emergency/contacts)
 * - 新建 (POST /api/emergency/contact/save)
 * - 删除 (POST /api/emergency/contact/delete/{id})
 */
class ContactsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactsBinding
    private val app get() = application as HealthApp
    private val adapter by lazy { ContactAdapter(onDelete = ::deleteContact) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.rvContacts.layoutManager = LinearLayoutManager(this)
        binding.rvContacts.adapter = adapter

        binding.fabAdd.setOnClickListener { showInputDialog() }
        load()
    }

    private fun load() {
        lifecycleScope.launch {
            try {
                val resp = app.api.listContacts()
                adapter.submitList(resp.data ?: emptyList())
            } catch (e: Exception) {
                Snackbar.make(binding.root, "加载失败: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun showInputDialog() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
        }
        val etName = EditText(this).apply { hint = "姓名" }
        val etRelation = EditText(this).apply { hint = "关系 (例: 配偶/父母/朋友)" }
        val etPhone = EditText(this).apply { hint = "电话" }
        container.addView(etName)
        container.addView(etRelation)
        container.addView(etPhone)

        AlertDialog.Builder(this)
            .setTitle("新建联系人")
            .setView(container)
            .setPositiveButton("保存") { _, _ ->
                if (etName.text.isBlank() || etPhone.text.isBlank()) {
                    Snackbar.make(binding.root, "姓名和电话必填", Snackbar.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val userId = app.retrofitClient.tokenStore.getUserId()
                val c = EmergencyContact(
                    userId = userId,
                    name = etName.text.toString(),
                    relation = etRelation.text.toString().ifBlank { null },
                    phone = etPhone.text.toString()
                )
                lifecycleScope.launch {
                    try {
                        app.api.saveContact(c)
                        load()
                    } catch (e: Exception) {
                        Snackbar.make(binding.root, "保存失败: ${e.message}", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun deleteContact(c: EmergencyContact) {
        AlertDialog.Builder(this)
            .setTitle("确认删除")
            .setMessage("删除联系人 ${c.name}?")
            .setPositiveButton("删除") { _, _ ->
                val id = c.id ?: return@setPositiveButton
                lifecycleScope.launch {
                    try {
                        app.api.deleteContact(id)
                        Snackbar.make(binding.root, "已删除", Snackbar.LENGTH_SHORT).show()
                        load()
                    } catch (e: Exception) {
                        Snackbar.make(binding.root, "删除失败: ${e.message}", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
}