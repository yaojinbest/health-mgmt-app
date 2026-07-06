package com.opck.health.ui.medical

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.opck.health.HealthApp
import com.opck.health.R
import com.opck.health.data.model.Department
import com.opck.health.data.model.Doctor
import com.opck.health.data.model.Hospital
import com.opck.health.databinding.ActivityMedicalBinding
import kotlinx.coroutines.launch

/**
 * Medical 预约页 (用户视角 - 4 步引导)
 *
 * Step 1: 选医院
 * Step 2: 选科室 (按 hospitalId 过滤)
 * Step 3: 选医生 (按 departmentId 过滤)
 * Step 4: 选 schedule (时段) + 填写症状 → 创建预约
 */
class MedicalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicalBinding
    private val app get() = application as HealthApp

    private var currentStep = 1
    private var selectedHospital: Hospital? = null
    private var selectedDepartment: Department? = null
    private var selectedDoctor: Doctor? = null
    private var selectedScheduleId: Long? = null
    private var selectedScheduleDesc: String? = null

    // 通用 list adapter (单字段文本列表)
    private lateinit var adapter: SimpleTextAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.title = "预约就诊"
        binding.btnPrev.setOnClickListener { goPrev() }
        binding.btnNext.setOnClickListener { goNext() }

        adapter = SimpleTextAdapter { idx ->
            onItemClick(idx)
        }
        binding.rvList.layoutManager = LinearLayoutManager(this)
        binding.rvList.adapter = adapter

        updateStepIndicator()
        loadHospitals()
    }

    private fun updateStepIndicator() {
        binding.tvStep.text = "第 $currentStep/4 步 · ${stepTitle(currentStep)}"
        binding.btnPrev.visibility = if (currentStep > 1) View.VISIBLE else View.GONE
        binding.btnNext.text = if (currentStep == 4) "提交预约" else "下一步 →"
    }

    private fun stepTitle(step: Int) = when (step) {
        1 -> "选择医院"
        2 -> "选择科室"
        3 -> "选择医生"
        4 -> "选择时段"
        else -> ""
    }

    private fun goPrev() {
        if (currentStep > 1) {
            currentStep--
            updateStepIndicator()
            when (currentStep) {
                1 -> loadHospitals()
                2 -> selectedHospital?.id?.let { loadDepartments(it) }
                3 -> selectedDepartment?.id?.let { loadDoctors(it) }
            }
        }
    }

    private fun goNext() {
        when (currentStep) {
            1 -> selectedHospital?.let {
                currentStep = 2; updateStepIndicator(); loadDepartments(it.id ?: return@let)
            } ?: toast("请先选择医院")

            2 -> selectedDepartment?.let {
                currentStep = 3; updateStepIndicator(); loadDoctors(it.id ?: return@let)
            } ?: toast("请先选择科室")

            3 -> selectedDoctor?.let {
                currentStep = 4; updateStepIndicator(); loadSchedules(it.id ?: return@let)
            } ?: toast("请先选择医生")

            4 -> if (selectedScheduleId != null) showSymptomDialog() else toast("请先选择时段")
        }
    }

    private fun onItemClick(idx: Int) {
        when (currentStep) {
            1 -> selectedHospital = (adapter.tagList as List<Hospital>).getOrNull(idx)
            2 -> selectedDepartment = (adapter.tagList as List<Department>).getOrNull(idx)
            3 -> selectedDoctor = (adapter.tagList as List<Doctor>).getOrNull(idx)
            4 -> {
                selectedScheduleId = (adapter.tagList as List<Long>).getOrNull(idx)
                selectedScheduleDesc = adapter.items.getOrNull(idx)
            }
        }
        adapter.selectedPos = idx
        adapter.notifyDataSetChanged()
    }

    // ====== Step 1 ======
    private fun loadHospitals() {
        binding.progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val res = app.retrofitClient.api().listHospitals()
                binding.progress.visibility = View.GONE
                if (res.code == 200 && res.data != null) {
                    val items = res.data.map { "${it.name}  ${it.level ?: ""}" }
                    adapter.setData(items, res.data)
                    if (items.isEmpty()) toast("暂无医院数据")
                } else toast("加载失败: ${res.message}")
            } catch (e: Exception) {
                binding.progress.visibility = View.GONE
                toast("网络异常: ${e.message}")
            }
        }
    }

    // ====== Step 2 ======
    private fun loadDepartments(hospitalId: Long) {
        binding.progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val res = app.retrofitClient.api().listDepartments(hospitalId)
                binding.progress.visibility = View.GONE
                if (res.code == 200 && res.data != null) {
                    val items = res.data.map { it.name + (it.description?.let { d -> "  ($d)" } ?: "") }
                    adapter.setData(items, res.data)
                    if (items.isEmpty()) toast("暂无科室数据")
                } else toast("加载失败: ${res.message}")
            } catch (e: Exception) {
                binding.progress.visibility = View.GONE
                toast("网络异常: ${e.message}")
            }
        }
    }

    // ====== Step 3 ======
    private fun loadDoctors(departmentId: Long) {
        binding.progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val res = app.retrofitClient.api().listDoctors(departmentId)
                binding.progress.visibility = View.GONE
                if (res.code == 200 && res.data != null) {
                    val items = res.data.map {
                        "${it.doctorName ?: "医生"}  ${it.title ?: ""}  ${it.specialty ?: ""}"
                    }
                    adapter.setData(items, res.data)
                    if (items.isEmpty()) toast("暂无医生数据")
                } else toast("加载失败: ${res.message}")
            } catch (e: Exception) {
                binding.progress.visibility = View.GONE
                toast("网络异常: ${e.message}")
            }
        }
    }

    // ====== Step 4 ======
    private fun loadSchedules(doctorId: Long) {
        binding.progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val res = app.retrofitClient.api().listSchedules(doctorId)
                binding.progress.visibility = View.GONE
                if (res.code == 200 && res.data != null) {
                    val ids = res.data.mapNotNull { (it["id"] as? Number)?.toLong() }
                    val items = res.data.map { entry ->
                        val date = entry["scheduleDate"]?.toString() ?: ""
                        val slot = entry["timeSlot"]?.toString() ?: ""
                        val remain = (entry["remainQuota"] as? Number)?.toInt() ?: 0
                        "$date  $slot  余号 $remain"
                    }
                    adapter.setData(items, ids)
                    if (items.isEmpty()) toast("暂无可预约时段")
                } else toast("加载失败: ${res.message}")
            } catch (e: Exception) {
                binding.progress.visibility = View.GONE
                toast("网络异常: ${e.message}")
            }
        }
    }

    private fun showSymptomDialog() {
        val input = EditText(this).apply {
            hint = "请描述症状 (选填)"
            minLines = 3
            setPadding(48, 32, 48, 32)
        }
        AlertDialog.Builder(this)
            .setTitle("确认预约")
            .setMessage("${selectedHospital?.name}\n${selectedDepartment?.name}\n${selectedDoctor?.doctorName}\n${selectedScheduleDesc}")
            .setView(input)
            .setPositiveButton("提交") { _, _ -> createAppointment(input.text.toString()) }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun createAppointment(symptom: String) {
        val userId = app.retrofitClient.tokenStore.getUserId()
        if (userId == 0L) { toast("未登录"); return }
        val scheduleId = selectedScheduleId ?: return
        lifecycleScope.launch {
            try {
                val req = mapOf(
                    "userId" to userId,
                    "hospitalId" to (selectedHospital?.id ?: 0L),
                    "departmentId" to (selectedDepartment?.id ?: 0L),
                    "doctorId" to (selectedDoctor?.id ?: 0L),
                    "scheduleId" to scheduleId,
                    "symptom" to symptom
                )
                val res = app.retrofitClient.api().createAppointment(req)
                if (res.code == 200) {
                    toast("预约成功!")
                    finish()
                } else {
                    toast("预约失败: ${res.message}")
                }
            } catch (e: Exception) {
                toast("网络异常: ${e.message}")
            }
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}

/**
 * 简单文本列表 adapter (带选中态)
 * - items: 显示文本
 * - tagList: 关联数据 (Hospital/Department/Doctor/Long id)
 */
class SimpleTextAdapter(
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<SimpleTextAdapter.VH>() {

    var items: List<String> = emptyList()
    var tagList: List<Any> = emptyList()
    var selectedPos: Int = -1

    fun setData(items: List<String>, tags: List<Any>) {
        this.items = items
        this.tagList = tags
        this.selectedPos = -1
        notifyDataSetChanged()
    }

    class VH(val text: TextView) : RecyclerView.ViewHolder(text)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val tv = LayoutInflater.from(parent.context).inflate(
            android.R.layout.simple_list_item_activated_1, parent, false
        ) as TextView
        return VH(tv)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.text.text = items.getOrNull(position) ?: ""
        holder.text.setBackgroundColor(
            if (position == selectedPos) 0xFFE3F2FD.toInt() else 0x00000000
        )
        holder.text.setOnClickListener { onClick(position) }
    }

    override fun getItemCount() = items.size
}