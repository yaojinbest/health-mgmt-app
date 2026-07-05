package com.opck.health.ui.widget

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView 横向间距装饰
 *
 * 用法:
 *   addItemDecoration(HorizontalSpaceDecoration(8))
 */
class HorizontalSpaceDecoration(private val spaceDp: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return
        outRect.left = if (position == 0) spaceDp else spaceDp / 2
        outRect.right = if (position == parent.adapter!!.itemCount - 1) spaceDp else spaceDp / 2
    }
}