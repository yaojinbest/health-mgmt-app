package com.opck.health.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.opck.health.data.local.TokenStore
import com.opck.health.databinding.FragmentHomeBinding

/**
 * 首页 - 今日健康概览 (D1 占位, D2 实现)
 *
 * 对齐 H5: m-header (greeting + 姓名) + m-hero (今日概览 + 状态) + m-stats (4 健康卡片) + m-quick-grid (快捷服务) + m-reminder-list
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tokenStore = TokenStore(requireContext())
        binding.tvRealName.text = tokenStore.getRealName() ?: "用户"
        binding.tvRole.text = "角色: ${tokenStore.getRole() ?: "未知"}"
        // D2 接入: loadHomeOverview() API
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
