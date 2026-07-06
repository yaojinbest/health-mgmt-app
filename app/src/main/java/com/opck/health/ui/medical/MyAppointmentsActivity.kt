package com.opck.health.ui.medical

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.opck.health.HealthApp
import com.opck.health.databinding.ActivityMyAppointmentsBinding
import kotlinx.coroutines.launch

/**
 * 我的预约页
 *
 * - 列表展示当前用户的所有预约
 * - CONFIRMED 状态可取消
 * - 下拉刷新 (TODO: 加 SwipeRefreshLayout)
 */
class MyAppointmentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyAppointmentsBinding
    private val app get() = application as HealthApp
    private lateinit var adapter: AppointmentAdapter
    private var dataList: List<Map<String, Any>> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyAppointmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.title = "我的预约"

        adapter = AppointmentAdapter(
            dataList = dataList,
            onCancel = { idx -> confirmCancel(idx) }
        )
        binding.rvAppointments.layoutManager = LinearLayoutManager(this)
        binding.rvAppointments.adapter = adapter

        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        binding.progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val res = app.retrofitClient.api().listAppointments(userId = null)
                binding.progress.visibility = View.GONE
                if (res.code == 200 && res.data != null) {
                    dataList = res.data
                    adapter.update(dataList)
                    binding.tvEmpty.visibility = if (dataList.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    Toast.makeText(this@MyAppointmentsActivity, "加载失败: ${res.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progress.visibility = View.GONE
                Toast.makeText(this@MyAppointmentsActivity, "网络异常: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmCancel(idx: Int) {
        val item = dataList.getOrNull(idx) ?: return
        val id = (item["id"] as? Number)?.toLong() ?: return
        AlertDialog.Builder(this)
            .setTitle("取消预约")
            .setMessage("确定取消 ${item["hospitalName"]} ${item["appointmentDate"]} 的预约吗?")
            .setPositiveButton("确认取消") { _, _ -> doCancel(id) }
            .setNegativeButton("再想想", null)
            .show()
    }

    private fun doCancel(id: Long) {
        lifecycleScope.launch {
            try {
                val res = app.retrofitClient.api().cancelAppointment(id)
                if (res.code == 200) {
                    Toast.makeText(this@MyAppointmentsActivity, "已取消", Toast.LENGTH_SHORT).show()
                    loadData()
                } else {
                    Toast.makeText(this@MyAppointmentsActivity, "取消失败: ${res.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MyAppointmentsActivity, "网络异常: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

class AppointmentAdapter(
    private var dataList: List<Map<String, Any>>,
    private val onCancel: (Int) -> Unit
) : RecyclerView.Adapter<AppointmentAdapter.VH>() {

    fun update(newData: List<Map<String, Any>>) {
        dataList = newData
        notifyDataSetChanged()
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvHospital: TextView = view.findViewById(android.R.id.text1)
        val tvSubtitle: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        // 简化: 用 simple_list_item_2 (2 行)
        val v = LayoutInflater.from(parent.context).inflate(
            android.R.layout.simple_list_item_2, parent, false
        )
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = dataList[position]
        val hospital = item["hospitalName"]?.toString() ?: "未知医院"
        val department = item["departmentName"]?.toString() ?: ""
        val doctor = item["doctorName"]?.toString() ?: ""
        val date = item["appointmentDate"]?.toString() ?: ""
        val slot = item["timeSlot"]?.toString() ?: ""
        val status = item["status"]?.toString() ?: ""
        val symptom = item["symptom"]?.toString() ?: ""

        holder.tvHospital.text = "[$status] $hospital · $department"
        holder.tvSubtitle.text = "$doctor · $date $slot\n症状: $symptom"

        holder.itemView.setOnLongClickListener {
            if (status == "CONFIRMED") onCancel(position)
            true
        }
    }

    override fun getItemCount() = dataList.size
}