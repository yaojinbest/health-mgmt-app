package com.opck.health.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.opck.health.HealthApp
import com.opck.health.R
import com.opck.health.data.model.ApiResult
import com.opck.health.databinding.ActivityDashboardBinding
import com.opck.health.databinding.ItemStatCardBinding
import kotlinx.coroutines.launch

/**
 * Dashboard 统计页 (admin/doctor 视角)
 *
 * - admin: 显示系统大盘 (用户数/医生数/预约数/预警数 + 角色饼图 + 7 天预约趋势)
 * - doctor: 显示医生视角 (患者数/预约数/咨询数/用药记录 + 患者预警 TOP)
 * - user: 提示用户跳转到 Health tab 查看自己的健康数据
 *
 * 后端 API:
 *   GET /api/dashboard/admin         (admin 视角)
 *   GET /api/dashboard/doctor/{id}   (doctor 视角)
 */
class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val app get() = application as HealthApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        val role = app.retrofitClient.tokenStore.getRole() ?: "USER"
        when (role) {
            "ADMIN" -> loadAdminDashboard()
            "DOCTOR" -> loadDoctorDashboard()
            else -> showUserRedirect()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadAdminDashboard() {
        binding.tvDashboardTitle.text = "系统大盘"
        binding.tvDashboardSubtitle.text = "管理员视角 · 全站运营概览"
        binding.layoutUserHint.visibility = View.GONE
        binding.scrollContent.visibility = View.VISIBLE

        val api = app.retrofitClient.api()
        lifecycleScope.launch {
            try {
                val res = api.dashboardAdmin()
                if (res.code == 200 && res.data != null) {
                    renderAdminCards(res.data)
                    renderAdminCharts(res.data)
                } else {
                    binding.tvError.visibility = View.VISIBLE
                    binding.tvError.text = "加载失败: ${res.message ?: "未知错误"}"
                }
            } catch (e: Exception) {
                binding.tvError.visibility = View.VISIBLE
                binding.tvError.text = "网络异常: ${e.message}"
            }
        }
    }

    private fun renderAdminCards(data: Map<String, Any>) {
        @Suppress("UNCHECKED_CAST")
        val cards = data["cards"] as? Map<String, Any> ?: emptyMap()
        binding.layoutCards.removeAllViews()

        val cardDefs = listOf(
            Triple("👥", "用户数", cards["users"]?.toString() ?: "0"),
            Triple("🩺", "医生数", cards["doctors"]?.toString() ?: "0"),
            Triple("📅", "预约数", cards["appointments"]?.toString() ?: "0"),
            Triple("⚠️", "预警数", cards["warnings"]?.toString() ?: "0"),
        )
        cardDefs.forEach { (icon, label, value) ->
            val card = ItemStatCardBinding.inflate(LayoutInflater.from(this), binding.layoutCards, false)
            card.tvCardIcon.text = icon
            card.tvCardLabel.text = label
            card.tvCardValue.text = value
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.bottomMargin = (resources.displayMetrics.density * 8).toInt()
            card.root.layoutParams = lp
            binding.layoutCards.addView(card.root)
        }
    }

    private fun renderAdminCharts(data: Map<String, Any>) {
        // 角色饼图
        @Suppress("UNCHECKED_CAST")
        val rolePie = data["rolePie"] as? Map<String, Any> ?: emptyMap()
        renderPieChart(rolePie)

        // 7 天预约趋势
        @Suppress("UNCHECKED_CAST")
        val apptTrend = data["appointmentTrend"] as? Map<String, Any> ?: emptyMap()
        renderBarChart(apptTrend, "近 7 天预约数")
    }

    private fun renderPieChart(roleMap: Map<String, Any>) {
        val entries = roleMap.entries.map { (k, v) ->
            PieEntry((v as? Number)?.toFloat() ?: 0f, k)
        }
        if (entries.isEmpty()) {
            binding.chartPie.visibility = View.GONE
            return
        }
        val ds = PieDataSet(entries, "").apply {
            setColors(
                android.graphics.Color.parseColor("#1976D2"),
                android.graphics.Color.parseColor("#43A047"),
                android.graphics.Color.parseColor("#F57C00")
            )
            valueTextSize = 12f
        }
        binding.chartPie.data = PieData(ds)
        binding.chartPie.description = Description().apply { text = "" }
        binding.chartPie.setUsePercentValues(false)
        binding.chartPie.invalidate()
    }

    private fun renderBarChart(trendMap: Map<String, Any>, title: String) {
        val entries = trendMap.entries.sortedBy { it.key }.mapIndexed { idx, (_, v) ->
            BarEntry(idx.toFloat(), (v as? Number)?.toFloat() ?: 0f)
        }
        if (entries.isEmpty()) {
            binding.chartBar.visibility = View.GONE
            binding.tvBarTitle.visibility = View.GONE
            return
        }
        val labels = trendMap.entries.sortedBy { it.key }.map { it.key.substring(5) } // MM-dd
        val ds = BarDataSet(entries, title).apply {
            color = android.graphics.Color.parseColor("#1976D2")
            valueTextSize = 10f
        }
        binding.chartBar.data = BarData(ds)
        binding.chartBar.description = Description().apply { text = "" }
        binding.chartBar.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.chartBar.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binding.chartBar.xAxis.granularity = 1f
        binding.chartBar.invalidate()
    }

    private fun loadDoctorDashboard() {
        binding.tvDashboardTitle.text = "医生工作台"
        binding.tvDashboardSubtitle.text = "医生视角 · 我的患者概览"
        binding.layoutUserHint.visibility = View.GONE
        binding.scrollContent.visibility = View.VISIBLE

        val userId = app.retrofitClient.tokenStore.getUserId() ?: 0L
        val api = app.retrofitClient.api()
        lifecycleScope.launch {
            try {
                val res = api.dashboardDoctor(userId)
                if (res.code == 200 && res.data != null) {
                    renderDoctorCards(res.data)
                    renderDoctorCharts(res.data)
                } else {
                    binding.tvError.visibility = View.VISIBLE
                    binding.tvError.text = "加载失败: ${res.message ?: "未知错误"}"
                }
            } catch (e: Exception) {
                binding.tvError.visibility = View.VISIBLE
                binding.tvError.text = "网络异常: ${e.message}"
            }
        }
    }

    private fun renderDoctorCards(data: Map<String, Any>) {
        @Suppress("UNCHECKED_CAST")
        val cards = data["cards"] as? Map<String, Any> ?: emptyMap()
        binding.layoutCards.removeAllViews()

        val cardDefs = listOf(
            Triple("👥", "患者数", cards["patients"]?.toString() ?: "0"),
            Triple("📅", "预约数", cards["appointments"]?.toString() ?: "0"),
            Triple("💬", "未结咨询", cards["openConsultations"]?.toString() ?: "0"),
            Triple("💊", "用药记录", cards["medicineRecords"]?.toString() ?: "0"),
        )
        cardDefs.forEach { (icon, label, value) ->
            val card = ItemStatCardBinding.inflate(LayoutInflater.from(this), binding.layoutCards, false)
            card.tvCardIcon.text = icon
            card.tvCardLabel.text = label
            card.tvCardValue.text = value
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.bottomMargin = (resources.displayMetrics.density * 8).toInt()
            card.root.layoutParams = lp
            binding.layoutCards.addView(card.root)
        }
    }

    private fun renderDoctorCharts(data: Map<String, Any>) {
        @Suppress("UNCHECKED_CAST")
        val consultStatus = data["consultationStatus"] as? Map<String, Any> ?: emptyMap()
        renderPieChart(consultStatus)

        @Suppress("UNCHECKED_CAST")
        val apptTrend = data["appointmentTrend"] as? Map<String, Any> ?: emptyMap()
        renderBarChart(apptTrend, "近 7 天预约数")
    }

    private fun showUserRedirect() {
        binding.tvDashboardTitle.text = "数据统计"
        binding.tvDashboardSubtitle.text = "用户视角 · 查看自己的健康数据"
        binding.layoutUserHint.visibility = View.VISIBLE
        binding.scrollContent.visibility = View.GONE
        binding.btnGoHealth.setOnClickListener {
            finish()
        }
    }
}