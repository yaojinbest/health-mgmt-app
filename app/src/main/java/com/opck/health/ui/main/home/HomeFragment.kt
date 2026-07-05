package com.opck.health.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.opck.health.HealthApp
import com.opck.health.R
import com.opck.health.data.local.TokenStore
import com.opck.health.databinding.FragmentHomeBinding
import com.opck.health.ui.main.MainActivity
import com.opck.health.ui.widget.HorizontalSpaceDecoration
import com.opck.health.ui.widget.HomeArticleAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 首页 - 今日健康概览 (D2 实现)
 *
 * 内容:
 * - 顶部品牌区 (绿底, 用户问候 + 角色)
 * - 最近一次健康数据卡 (2x2 指标 + 警告徽章)
 * - 8 个快捷入口网格 (横向点击切 Fragment)
 * - 最新健康文章 (RecyclerView, 3 条)
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(HealthApp.get().repository)
    }

    private val tokenStore by lazy { TokenStore(requireContext()) }

    private val articleAdapter = HomeArticleAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHeader()
        setupQuickEntries()
        setupArticles()
        observeState()
    }

    override fun onResume() {
        super.onResume()
        // 每次回首页都刷新一次 (录入新数据后能看到)
        loadData()
    }

    private fun setupHeader() {
        val realName = tokenStore.getRealName() ?: "用户"
        binding.tvRealName.text = realName
        val role = tokenStore.getRole() ?: "USER"
        binding.tvRole.text = "角色: ${roleChinese(role)}"
        binding.tvDate.text = SimpleDateFormat("yyyy年M月d日 EEEE", Locale.CHINESE).format(Date())
    }

    private fun roleChinese(role: String): String = when (role) {
        "USER" -> "患者"
        "DOCTOR" -> "医生"
        "ADMIN" -> "管理员"
        else -> role
    }

    private fun setupQuickEntries() {
        binding.quickRecord.setOnClickListener { (activity as? MainActivity)?.navigateToTab(R.id.tab_health) }
        binding.quickMedicine.setOnClickListener { showToast("用药管理 D4 实现") }
        binding.quickArchive.setOnClickListener { showToast("健康档案 D4 实现") }
        binding.quickChart.setOnClickListener { (activity as? MainActivity)?.navigateToTab(R.id.tab_health) }
        binding.quickEmergency.setOnClickListener { showToast("紧急求救 D5 实现") }
        binding.quickArticle.setOnClickListener { showToast("健康文章 D5 实现") }
        binding.quickConsult.setOnClickListener { showToast("在线咨询 D6 实现") }
        binding.quickMine.setOnClickListener { (activity as? MainActivity)?.navigateToTab(R.id.tab_mine) }
    }

    private fun setupArticles() {
        binding.rvArticles.layoutManager = LinearLayoutManager(requireContext())
        binding.rvArticles.adapter = articleAdapter
        binding.rvArticles.addItemDecoration(HorizontalSpaceDecoration(8))
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is HomeViewModel.UiState.Loading -> {
                    // 静默加载, 不显示 progress
                }
                is HomeViewModel.UiState.Loaded -> {
                    renderLatestHealth(state.latest)
                    articleAdapter.submit(state.articles)
                }
                is HomeViewModel.UiState.Error -> {
                    // 显示 "--" 占位
                    showEmptyMetrics()
                    showToast(state.message)
                }
            }
        }
    }

    private fun renderLatestHealth(item: com.opck.health.data.model.HealthData?) {
        if (item == null) {
            binding.tvLastRecordTime.text = "暂未录入"
            showEmptyMetrics()
            binding.layoutWarning.visibility = View.GONE
            return
        }

        binding.tvLastRecordTime.text = "录入时间: ${formatDateTime(item.recordTime)}"

        binding.tvBp.text = if (item.systolic != null && item.diastolic != null)
            "${item.systolic}/${item.diastolic}" else "--"
        binding.tvHeart.text = item.heartRate?.toString() ?: "--"
        binding.tvSugar.text = item.bloodSugar?.let { "%.1f".format(it) } ?: "--"
        binding.tvSteps.text = item.steps?.toString() ?: "--"

        // 警告徽章
        when (item.warningLevel) {
            "WARN" -> {
                binding.layoutWarning.visibility = View.VISIBLE
                binding.layoutWarning.setBackgroundResource(R.drawable.bg_warning_normal)
                binding.tvWarningIcon.text = "⚠️"
                binding.tvWarningText.text = item.warningMessage ?: "数据存在风险, 请关注"
                (binding.layoutWarning.background as? android.graphics.drawable.GradientDrawable)?.setColor(0xFFFFF3E0.toInt())
            }
            "DANGER" -> {
                binding.layoutWarning.visibility = View.VISIBLE
                binding.tvWarningIcon.text = "🚨"
                binding.tvWarningText.text = item.warningMessage ?: "数据异常, 建议就医"
            }
            else -> {
                binding.layoutWarning.visibility = View.VISIBLE
                binding.layoutWarning.setBackgroundResource(R.drawable.bg_warning_normal)
                binding.tvWarningIcon.text = "✅"
                binding.tvWarningText.text = "健康状态良好"
            }
        }
    }

    private fun showEmptyMetrics() {
        binding.tvBp.text = "--"
        binding.tvHeart.text = "--"
        binding.tvSugar.text = "--"
        binding.tvSteps.text = "--"
    }

    private fun formatDateTime(time: String?): String {
        if (time.isNullOrBlank()) return "未知"
        // 后端 LocalDateTime -> ISO 8601 (e.g., "2026-07-05T12:00:00")
        return time.replace("T", " ").take(16)
    }

    private fun loadData() {
        val userId = tokenStore.getUserId()
        if (userId == null) {
            showToast("请先登录")
            return
        }
        viewModel.load(userId)
    }

    private fun showToast(msg: String) {
        android.widget.Toast.makeText(requireContext(), msg, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}