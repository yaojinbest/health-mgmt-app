package com.opck.health.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.opck.health.databinding.FragmentPlaceholderBinding

/**
 * 占位 Fragment - 用于 D1 阶段填充尚待开发的 tab
 *
 * 后续被具体业务 Fragment 替换
 */
class PlaceholderFragment : Fragment() {

    private var _binding: FragmentPlaceholderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaceholderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title = requireArguments().getString(ARG_TITLE) ?: ""
        val subtitle = requireArguments().getString(ARG_SUBTITLE) ?: ""
        binding.tvTitle.text = title
        binding.tvSubtitle.text = subtitle
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_SUBTITLE = "subtitle"

        fun newInstance(title: String, subtitle: String) = PlaceholderFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_SUBTITLE, subtitle)
            }
        }
    }
}
