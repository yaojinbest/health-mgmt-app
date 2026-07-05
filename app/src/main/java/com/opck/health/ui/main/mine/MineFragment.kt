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
import com.opck.health.ui.article.ArticlesActivity
import com.opck.health.ui.archive.ArchiveActivity
import com.opck.health.ui.consultation.ConsultationsActivity
import com.opck.health.ui.login.LoginActivity
import com.opck.health.ui.login.ServerConfigActivity
import com.opck.health.ui.medicine.MedicineActivity

/**
 * 我的 tab (D4-D6 入口)
 *
 * - 用户卡片
 * - 4 个入口: 用药管理 / 健康档案 / 在线咨询 / 健康文章
 * - 服务器地址配置
 * - 退出登录
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

        binding.cardMedicine.setOnClickListener {
            startActivity(Intent(requireContext(), MedicineActivity::class.java))
        }
        binding.cardArchive.setOnClickListener {
            startActivity(Intent(requireContext(), ArchiveActivity::class.java))
        }
        binding.cardConsultation.setOnClickListener {
            startActivity(Intent(requireContext(), ConsultationsActivity::class.java))
        }
        binding.cardArticles.setOnClickListener {
            startActivity(Intent(requireContext(), ArticlesActivity::class.java))
        }

        binding.btnServerConfig.setOnClickListener {
            startActivity(Intent(requireContext(), ServerConfigActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}