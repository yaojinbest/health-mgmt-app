package com.opck.health.ui.main.tools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.opck.health.databinding.FragmentToolsBinding

/**
 * 工具 tab - 紧急求救 + 健康文章 + 找医生 等入口网格
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
        // D5 实现: 找医生 / 文章 / 紧急求救入口
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
