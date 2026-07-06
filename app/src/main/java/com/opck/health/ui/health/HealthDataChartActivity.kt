package com.opck.health.ui.health

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.opck.health.HealthApp
import com.opck.health.R
import com.opck.health.data.model.HealthData
import com.opck.health.databinding.ActivityHealthDataChartBinding
import kotlinx.coroutines.launch
import java.math.BigDecimal

/**
 * 健康数据图表 (4 维度趋势)
 *
 * - 血压 (systolic + diastolic 双线)
 * - 血糖 (bloodSugar)
 * - 心率 (heartRate)
 * - 体重 (weight)
 *
 * 7/30/90 天切换 (默认 7 天)
 *
 * 后端: GET /api/health-data/chart?userId=4
 */
class HealthDataChartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHealthDataChartBinding
    private val app get() = application as HealthApp
    private var days = 7
    private var allData: List<HealthData> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHealthDataChartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.title = "健康数据图表"

        binding.toggleRange.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            days = when (checkedId) {
                R.id.btn_7d -> 7
                R.id.btn_30d -> 30
                R.id.btn_90d -> 90
                else -> 7
            }
            renderCharts()
        }
        binding.toggleRange.check(R.id.btn_7d)

        loadData()
    }

    private fun loadData() {
        binding.progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val userId = app.retrofitClient.tokenStore.getUserId()
                val res = app.retrofitClient.api().chartHealthData(userId)
                binding.progress.visibility = View.GONE
                if (res.code == 200 && res.data != null) {
                    allData = res.data
                    renderCharts()
                    binding.tvEmpty.visibility = if (allData.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    Toast.makeText(this@HealthDataChartActivity, "加载失败: ${res.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progress.visibility = View.GONE
                Toast.makeText(this@HealthDataChartActivity, "网络异常: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderCharts() {
        val filtered = filterByDays(allData, days)
        if (filtered.isEmpty()) return

        val labels = filtered.map { formatLabel(it.recordTime) }

        // 1. 血压双线
        val sysEntries = filtered.mapIndexedNotNull { i, d -> d.systolic?.let { Entry(i.toFloat(), it.toFloat()) } }
        val diaEntries = filtered.mapIndexedNotNull { i, d -> d.diastolic?.let { Entry(i.toFloat(), it.toFloat()) } }
        val bpDataSets = mutableListOf<LineDataSet>()
        if (sysEntries.isNotEmpty()) bpDataSets += LineDataSet(sysEntries, "收缩压").apply {
            color = Color.parseColor("#E53935"); valueTextSize = 9f; setCircleColor(color); circleRadius = 3f; lineWidth = 2f
        }
        if (diaEntries.isNotEmpty()) bpDataSets += LineDataSet(diaEntries, "舒张压").apply {
            color = Color.parseColor("#1E88E5"); valueTextSize = 9f; setCircleColor(color); circleRadius = 3f; lineWidth = 2f
        }
        renderLineChart(binding.chartBloodPressure, bpDataSets, labels, "血压 (mmHg)")

        // 2. 血糖
        val bsEntries = filtered.mapIndexedNotNull { i, d -> d.bloodSugar?.let { Entry(i.toFloat(), it.toFloat()) } }
        if (bsEntries.isNotEmpty()) {
            renderLineChart(binding.chartBloodSugar, listOf(
                LineDataSet(bsEntries, "血糖").apply {
                    color = Color.parseColor("#FB8C00"); valueTextSize = 9f; setCircleColor(color); circleRadius = 3f; lineWidth = 2f
                }
            ), labels, "血糖 (mmol/L)")
        } else clearChart(binding.chartBloodSugar, "暂无血糖数据")

        // 3. 心率
        val hrEntries = filtered.mapIndexedNotNull { i, d -> d.heartRate?.let { Entry(i.toFloat(), it.toFloat()) } }
        if (hrEntries.isNotEmpty()) {
            renderLineChart(binding.chartHeartRate, listOf(
                LineDataSet(hrEntries, "心率").apply {
                    color = Color.parseColor("#8E24AA"); valueTextSize = 9f; setCircleColor(color); circleRadius = 3f; lineWidth = 2f
                }
            ), labels, "心率 (bpm)")
        } else clearChart(binding.chartHeartRate, "暂无心率数据")

        // 4. 体重
        val wtEntries = filtered.mapIndexedNotNull { i, d -> d.weight?.let { Entry(i.toFloat(), it.toFloat()) } }
        if (wtEntries.isNotEmpty()) {
            renderLineChart(binding.chartWeight, listOf(
                LineDataSet(wtEntries, "体重").apply {
                    color = Color.parseColor("#43A047"); valueTextSize = 9f; setCircleColor(color); circleRadius = 3f; lineWidth = 2f
                }
            ), labels, "体重 (kg)")
        } else clearChart(binding.chartWeight, "暂无体重数据")
    }

    private fun filterByDays(list: List<HealthData>, days: Int): List<HealthData> {
        return list.takeLast(days * 3)  // 假设每天 3 条数据点
    }

    private fun formatLabel(time: String?): String {
        if (time.isNullOrBlank()) return ""
        return try {
            time.substring(5, 10)  // "MM-dd"
        } catch (e: Exception) { time }
    }

    private fun renderLineChart(chart: LineChart, dataSets: List<LineDataSet>, labels: List<String>, title: String) {
        chart.data = LineData(*dataSets.toTypedArray())
        chart.description.isEnabled = false
        chart.setNoDataText("暂无数据")
        chart.setTouchEnabled(true)
        chart.setPinchZoom(true)
        chart.legend.isEnabled = true
        chart.legend.textSize = 11f

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.labelCount = minOf(labels.size, 5)
        xAxis.textSize = 9f

        chart.axisRight.isEnabled = false
        chart.axisLeft.textSize = 9f

        chart.invalidate()
    }

    private fun clearChart(chart: LineChart, msg: String) {
        chart.clear()
        chart.setNoDataText(msg)
        chart.invalidate()
    }
}