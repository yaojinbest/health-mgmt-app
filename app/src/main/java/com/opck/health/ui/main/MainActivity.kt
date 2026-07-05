package com.opck.health.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.opck.health.databinding.ActivityMainBinding
import com.opck.health.ui.main.health.HealthFragment
import com.opck.health.ui.main.home.HomeFragment
import com.opck.health.ui.main.mine.MineFragment
import com.opck.health.ui.main.tools.ToolsFragment

/**
 * 主页面 - 5 个 BottomNav tab
 *
 * 严格遵循 Material BottomNav 5 个限制 (d7 教训)
 *
 * tabs:
 *   1. 今日   - HomeFragment   (健康概览)
 *   2. 数据   - HealthFragment (健康数据录入 + 图表)
 *   3. 工具   - ToolsFragment  (紧急求救 + 文章入口)
 *   4. 咨询   - 暂时占位, D6 完成
 *   5. 我的   - MineFragment   (个人信息 + 用药 + 档案入口)
 *
 * 用药 / 档案 / 在线咨询 等子页 后续以二级页面或侧滑抽屉进入
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            switchTab(0)
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            switchTab(
                when (item.itemId) {
                    com.opck.health.R.id.tab_home -> 0
                    com.opck.health.R.id.tab_health -> 1
                    com.opck.health.R.id.tab_tools -> 2
                    com.opck.health.R.id.tab_consult -> 3
                    com.opck.health.R.id.tab_mine -> 4
                    else -> 0
                }
            )
            true
        }
    }

    private fun switchTab(index: Int) {
        val fragment: Fragment = when (index) {
            0 -> HomeFragment()
            1 -> HealthFragment()
            2 -> ToolsFragment()
            3 -> PlaceholderFragment.newInstance("在线咨询", "D6 完成")
            4 -> MineFragment()
            else -> HomeFragment()
        }
        supportFragmentManager.beginTransaction()
            .replace(com.opck.health.R.id.fragment_container, fragment)
            .commit()
    }
}
