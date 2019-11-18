package com.github.pvtitov.grannys.android.telephone

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

class ScrollableLayoutManager(context: Context?):
    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false) {

    var isScrollable = true

    override fun canScrollHorizontally(): Boolean {
        return super.canScrollHorizontally() && isScrollable
    }
}