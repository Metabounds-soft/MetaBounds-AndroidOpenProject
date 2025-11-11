package com.xr.common.middleware.view

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

/**
 * Description:
 * CreateDate:     2023/5/12 16:11
 * Author:         agg
 */
abstract class BaseRVAdapter<T : ViewHolder> : RecyclerView.Adapter<T>() {

    protected lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): T {
        context = parent.context
        return createMyViewHolder(parent, viewType)
    }

    abstract fun createMyViewHolder(parent: ViewGroup, viewType: Int): T

}