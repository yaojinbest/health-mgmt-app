package com.opck.health.ui.main.tools

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.opck.health.databinding.FragmentToolsBinding
import com.opck.health.ui.article.ArticlesActivity
import com.opck.health.ui.consultation.ConsultationsActivity
import com.opck.health.ui.emergency.ContactsActivity
import com.opck.health.ui.emergency.SosActivity
import com.opck.health.ui.medicine.MedicineActivity

/**
 * 工具 tab (D5 + D6 入口)
 *
 * - SOS 紧急求救 (红色大卡片)
 * - 在线咨询 / 健康文章 / 紧急联系人 / 用药管理 (2x2 网格)
 */
class ToolsFragment : Fragment() {

    private var _binding: FragmentToolsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentToolsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardSos.setOnClickListener {
            startActivity(Intent(requireContext(), SosActivity::class.java))
        }
        binding.cardConsultation.setOnClickListener {
            startActivity(Intent(requireContext(), ConsultationsActivity::class.java))
        }
        binding.cardArticles.setOnClickListener {
            startActivity(Intent(requireContext(), ArticlesActivity::class.java))
        }
        binding.cardContacts.setOnClickListener {
            startActivity(Intent(requireContext(), ContactsActivity::class.java))
        }
        binding.cardMedicine.setOnClickListener {
            startActivity(Intent(requireContext(), MedicineActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}