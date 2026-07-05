package com.opck.health.ui.main.mine

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.opck.health.HealthApp
import com.opck.health.data.local.TokenStore
import com.opck.health.databinding.FragmentMineBinding
import com.opck.health.ui.login.LoginActivity

/**
 * 我的 tab - 个人信息 + 用药 + 档案入口 + 退出
 *
 * D4 完整实现
 */
class MineFragment : Fragment() {

    private var _binding: FragmentMineBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tokenStore = TokenStore(requireContext())
        binding.tvRealName.text = tokenStore.getRealName() ?: "未设置"
        binding.tvUsername.text = "账号: ${tokenStore.getUsername() ?: "-"}"
        binding.tvUserId.text = "ID: ${tokenStore.getUserId()}"

        binding.btnLogout.setOnClickListener {
            HealthApp.get().authRepository.logout()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
        // D4 接入: 用药管理 + 健康档案 + 找医生入口
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
