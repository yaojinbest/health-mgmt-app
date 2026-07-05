package com.opck.health.ui.main.health

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.opck.health.databinding.FragmentHealthBinding

/**
 * 健康数据 tab - D3 实现 (录入表单 + MPAndroidChart 趋势)
 */
class HealthFragment : Fragment() {

    private var _binding: FragmentHealthBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // D3 实现: listHealthData() + 录入表单 + LineChart
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
