package com.example.cdmusicplayer.utils

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewLastItemMarginDecoration(
    private val marginInDp: Int,
    private val context: Context
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val itemCount = parent.adapter?.itemCount ?: 0

        if (position == itemCount - 1) {
            // Apply margin to the last item
            val marginInPixels = dpToPixels(marginInDp, context)
            outRect.bottom = marginInPixels
        }
    }

    private fun dpToPixels(dp: Int, context: Context): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }
}