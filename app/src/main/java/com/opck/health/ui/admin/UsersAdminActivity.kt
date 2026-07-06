package com.opck.health.ui.admin

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.opck.health.HealthApp
import com.opck.health.R
import com.opck.health.data.model.SysUser
import com.opck.health.databinding.ActivityUsersAdminBinding
import kotlinx.coroutines.launch

/**
 * 用户管理 (admin only)
 *
 * - 列表 + 角色筛选
 * - 新增 / 编辑 (弹窗表单)
 * - 停用 (PC Web 同款: save with status=INACTIVE)
 */
class UsersAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUsersAdminBinding
    private val app get() = application as HealthApp
    private lateinit var adapter: UserAdapter
    private var dataList: List<SysUser> = emptyList()
    private var currentRoleFilter: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.title = "用户管理"

        binding.btnFilterAll.setOnClickListener { currentRoleFilter = null; loadData() }
        binding.btnFilterUser.setOnClickListener { currentRoleFilter = "USER"; loadData() }
        binding.btnFilterDoctor.setOnClickListener { currentRoleFilter = "DOCTOR"; loadData() }
        binding.btnFilterAdmin.setOnClickListener { currentRoleFilter = "ADMIN"; loadData() }
        binding.btnCreate.setOnClickListener { openEditDialog(null) }

        adapter = UserAdapter(
            dataList = dataList,
            onEdit = { idx -> openEditDialog(dataList[idx]) },
            onDisable = { idx -> confirmDisable(idx) }
        )
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = adapter

        loadData()
    }

    private fun loadData() {
        binding.progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val res = app.retrofitClient.api().listUsers(currentRoleFilter)
                binding.progress.visibility = View.GONE
                if (res.code == 200 && res.data != null) {
                    dataList = res.data
                    adapter.update(dataList)
                    binding.tvCount.text = "共 ${dataList.size} 个用户"
                } else {
                    Toast.makeText(this@UsersAdminActivity, "加载失败: ${res.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progress.visibility = View.GONE
                Toast.makeText(this@UsersAdminActivity, "网络异常: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmDisable(idx: Int) {
        val user = dataList[idx]
        AlertDialog.Builder(this)
            .setTitle("停用用户")
            .setMessage("确认停用 ${user.username} (${user.realName})?")
            .setPositiveButton("确认停用") { _, _ -> doDisable(user) }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun doDisable(user: SysUser) {
        lifecycleScope.launch {
            try {
                val req = user.copy(status = "INACTIVE")
                val res = app.retrofitClient.api().saveUser(req)
                if (res.code == 200) {
                    Toast.makeText(this@UsersAdminActivity, "已停用", Toast.LENGTH_SHORT).show()
                    loadData()
                } else {
                    Toast.makeText(this@UsersAdminActivity, "失败: ${res.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@UsersAdminActivity, "网络异常: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openEditDialog(user: SysUser?) {
        val isEdit = user != null
        val padding = 48

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, 24, padding, 0)
        }

        val etUsername = EditText(this).apply {
            hint = "用户名 *"
            setText(user?.username ?: "")
        }
        val etPassword = EditText(this).apply {
            hint = if (isEdit) "密码 (留空不修改)" else "密码 *"
        }
        val etRealName = EditText(this).apply {
            hint = "真实姓名"
            setText(user?.realName ?: "")
        }
        val etPhone = EditText(this).apply {
            hint = "手机号"
            setText(user?.phone ?: "")
        }
        val spRole = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@UsersAdminActivity,
                android.R.layout.simple_spinner_dropdown_item,
                listOf("USER (患者)", "DOCTOR (医生)", "ADMIN (管理员)")
            )
            val currentRole = user?.role ?: "USER"
            setSelection(when (currentRole) {
                "ADMIN" -> 2; "DOCTOR" -> 1; else -> 0
            })
        }
        val spGender = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@UsersAdminActivity,
                android.R.layout.simple_spinner_dropdown_item,
                listOf("男", "女")
            )
            setSelection(if (user?.gender == "女") 1 else 0)
        }
        val etAge = EditText(this).apply {
            hint = "年龄"
            setText(user?.age?.toString() ?: "30")
        }
        val spStatus = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@UsersAdminActivity,
                android.R.layout.simple_spinner_dropdown_item,
                listOf("ACTIVE (启用)", "INACTIVE (停用)")
            )
            setSelection(if (user?.status == "INACTIVE") 1 else 0)
        }

        listOf(
            TextView(this).apply { text = "角色"; setPadding(0, 16, 0, 4) },
            spRole,
            TextView(this).apply { text = "用户名"; setPadding(0, 16, 0, 4) },
            etUsername,
            TextView(this).apply { text = "密码"; setPadding(0, 16, 0, 4) },
            etPassword,
            TextView(this).apply { text = "真实姓名"; setPadding(0, 16, 0, 4) },
            etRealName,
            TextView(this).apply { text = "手机号"; setPadding(0, 16, 0, 4) },
            etPhone,
            TextView(this).apply { text = "性别"; setPadding(0, 16, 0, 4) },
            spGender,
            TextView(this).apply { text = "年龄"; setPadding(0, 16, 0, 4) },
            etAge,
            TextView(this).apply { text = "状态"; setPadding(0, 16, 0, 4) },
            spStatus
        ).forEach { container.addView(it) }

        AlertDialog.Builder(this)
            .setTitle(if (isEdit) "编辑用户: ${user?.username ?: ""}" else "新增用户")
            .setView(container)
            .setPositiveButton(if (isEdit) "保存" else "创建") { _, _ ->
                val role = when (spRole.selectedItemPosition) {
                    2 -> "ADMIN"; 1 -> "DOCTOR"; else -> "USER"
                }
                val gender = if (spGender.selectedItemPosition == 1) "女" else "男"
                val status = if (spStatus.selectedItemPosition == 1) "INACTIVE" else "ACTIVE"

                val req = SysUser(
                    id = user?.id ?: 0L,
                    username = etUsername.text.toString().trim(),
                    password = etPassword.text.toString().takeIf { it.isNotBlank() },
                    realName = etRealName.text.toString().trim(),
                    phone = etPhone.text.toString().trim(),
                    role = role,
                    gender = gender,
                    age = etAge.text.toString().toIntOrNull() ?: 30,
                    status = status,
                    createTime = user?.createTime ?: nowIsoLocal()
                )
                if (req.username.isNullOrBlank()) {
                    toast("用户名不能为空")
                    return@setPositiveButton
                }
                if (!isEdit && req.password.isNullOrBlank()) {
                    toast("新用户密码不能为空")
                    return@setPositiveButton
                }
                saveUser(req)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun saveUser(req: SysUser) {
        lifecycleScope.launch {
            try {
                val res = app.retrofitClient.api().saveUser(req)
                if (res.code == 200) {
                    Toast.makeText(this@UsersAdminActivity, "保存成功", Toast.LENGTH_SHORT).show()
                    loadData()
                } else {
                    Toast.makeText(this@UsersAdminActivity, "失败: ${res.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@UsersAdminActivity, "网络异常: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    /** LocalDateTime 转 ISO 8601 (后端 NOT NULL create_time 需要 T 分隔) */
    private fun nowIsoLocal(): String {
        val cal = java.util.Calendar.getInstance()
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
        return sdf.format(cal.time)
    }
}

class UserAdapter(
    private var dataList: List<SysUser>,
    private val onEdit: (Int) -> Unit,
    private val onDisable: (Int) -> Unit
) : RecyclerView.Adapter<UserAdapter.VH>() {

    fun update(newData: List<SysUser>) {
        dataList = newData
        notifyDataSetChanged()
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvLine1: TextView = view.findViewById(android.R.id.text1)
        val tvLine2: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(
            android.R.layout.simple_list_item_2, parent, false
        )
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val u = dataList[position]
        val role = u.role ?: "USER"
        val roleLabel = when (role) {
            "ADMIN" -> "管理员"
            "DOCTOR" -> "医生"
            else -> "患者"
        }
        val statusLabel = if (u.status == "INACTIVE") "已停用" else "启用"
        holder.tvLine1.text = "${u.username} (${u.realName ?: "-"}) · $roleLabel · $statusLabel"
        holder.tvLine2.text = "手机: ${u.phone ?: "-"} · 性别: ${u.gender ?: "-"} · 年龄: ${u.age ?: "-"}"

        holder.itemView.setOnClickListener { onEdit(position) }
        holder.itemView.setOnLongClickListener {
            if (u.status != "INACTIVE") onDisable(position)
            true
        }
    }

    override fun getItemCount() = dataList.size
}