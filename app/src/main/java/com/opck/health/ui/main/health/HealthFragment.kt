package com.opck.health.ui.main.health

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.snackbar.Snackbar
import com.opck.health.HealthApp
import com.opck.health.R
import com.opck.health.databinding.DialogHealthInputBinding
import com.opck.health.databinding.FragmentHealthBinding
import com.opck.health.data.model.HealthData
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.math.BigDecimal

/**
 * 健康数据 tab (D3 - 实装)
 *
 * - 顶部 4 卡片 (最新血压/血糖/心率/步数)
 * - 7 天血压趋势图 (MPAndroidChart LineChart)
 * - 历史记录 RecyclerView
 * - 录入按钮 → AlertDialog 表单 → POST /api/health-data/save
 */
class HealthFragment : Fragment() {

    private var _binding: FragmentHealthBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HealthViewModel by viewModels {
        HealthViewModelFactory(requireActivity().application as HealthApp)
    }

    private val adapter = HealthRecordAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvHealthHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHealthHistory.adapter = adapter

        binding.btnRefresh.setOnClickListener { viewModel.load() }
        binding.btnAddHealth.setOnClickListener { showInputDialog() }

        setupChart()

        // 监听 ViewModel 状态
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collectLatest { state ->
                        when (state) {
                            is HealthViewModel.UiState.Loading ->
                                Snackbar.make(binding.root, "加载中...", Snackbar.LENGTH_SHORT).show()
                            is HealthViewModel.UiState.Saving ->
                                Snackbar.make(binding.root, "保存中...", Snackbar.LENGTH_SHORT).show()
                            is HealthViewModel.UiState.Saved ->
                                Snackbar.make(binding.root, "保存成功", Snackbar.LENGTH_SHORT).show()
                            is HealthViewModel.UiState.Error ->
                                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                            else -> { /* Idle/Loaded 不提示 */ }
                        }
                    }
                }
                launch {
                    viewModel.history.collectLatest { list ->
                        renderLatest(list)
                        renderChart(list)
                        adapter.submitList(list.take(20))
                    }
                }
            }
        }

        viewModel.load()
    }

    private fun setupChart() {
        binding.chartBp.apply {
            description = Description().apply { text = "" }
            setNoDataText("暂无数据")
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)
            axisRight.isEnabled = true
            axisLeft.setDrawGridLines(true)
            legend.isEnabled = true
        }
    }

    private fun renderLatest(list: List<HealthData>) {
        if (list.isEmpty()) {
            binding.metricBp.text = "--/--"
            binding.metricSugar.text = "--"
            binding.metricHeartRate.text = "--"
            binding.metricSteps.text = "--"
            return
        }
        val latest = list.maxByOrNull { it.recordTime ?: "" } ?: list.first()
        binding.metricBp.text = if (latest.systolic != null && latest.diastolic != null) {
            "${latest.systolic}/${latest.diastolic}"
        } else "--/--"

        binding.metricSugar.text = latest.bloodSugar?.toPlainString() ?: "--"
        binding.metricHeartRate.text = latest.heartRate?.toString() ?: "--"
        binding.metricSteps.text = latest.steps?.toString() ?: "--"
    }

    private fun renderChart(list: List<HealthData>) {
        // 按 recordTime 升序, 取最近 7 条
        val sorted = list.sortedBy { it.recordTime ?: "" }.takeLast(7)
        if (sorted.isEmpty()) {
            binding.chartBp.clear()
            binding.chartBp.invalidate()
            return
        }

        val labels = sorted.map { extractDate(it.recordTime) }  // "MM-DD"
        val systolicEntries = sorted.mapIndexedNotNull { i, r ->
            r.systolic?.let { Entry(i.toFloat(), it.toFloat()) }
        }
        val diastolicEntries = sorted.mapIndexedNotNull { i, r ->
            r.diastolic?.let { Entry(i.toFloat(), it.toFloat()) }
        }

        if (systolicEntries.isEmpty() && diastolicEntries.isEmpty()) {
            binding.chartBp.clear()
            return
        }

        val sysSet = LineDataSet(systolicEntries, "收缩压").apply {
            color = requireContext().getColor(R.color.danger)
            setCircleColor(color)
            circleRadius = 4f
            lineWidth = 2f
            valueTextSize = 10f
        }
        val diaSet = LineDataSet(diastolicEntries, "舒张压").apply {
            color = requireContext().getColor(R.color.warning)
            setCircleColor(color)
            circleRadius = 4f
            lineWidth = 2f
            valueTextSize = 10f
        }

        binding.chartBp.data = LineData(sysSet, diaSet)
        binding.chartBp.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binding.chartBp.invalidate()
    }

    /**
     * 从后端 ISO 8601 字符串里取 MM-DD.
     * 例: "2026-07-05T12:30:43" → "07-05".
     * 若格式不对, 截最后 5 个字符作为 fallback.
     */
    private fun extractDate(recordTime: String?): String {
        if (recordTime.isNullOrBlank() || recordTime.length < 10) {
            return ""
        }
        return recordTime.substring(5, 10)
    }

    private fun showInputDialog() {
        val dialogBinding = DialogHealthInputBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("保存", null)  // 我们手动处理, 防止自动 dismiss
            .setNegativeButton("取消") { d, _ -> d.dismiss() }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val systolic = dialogBinding.etSystolic.text.toString().toIntOrNull()
                val diastolic = dialogBinding.etDiastolic.text.toString().toIntOrNull()
                val sugar = dialogBinding.etBloodSugar.text.toString().toBigDecimalOrNull()
                val hr = dialogBinding.etHeartRate.text.toString().toIntOrNull()
                val steps = dialogBinding.etSteps.text.toString().toIntOrNull()
                val sleep = dialogBinding.etSleepHours.text.toString().toBigDecimalOrNull()
                val weight = dialogBinding.etWeight.text.toString().toBigDecimalOrNull()

                if (systolic == null && diastolic == null && sugar == null
                    && hr == null && steps == null && sleep == null && weight == null
                ) {
                    Snackbar.make(binding.root, "至少填一项指标", Snackbar.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewModel.save(systolic, diastolic, sugar, hr, steps, sleep, weight)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}