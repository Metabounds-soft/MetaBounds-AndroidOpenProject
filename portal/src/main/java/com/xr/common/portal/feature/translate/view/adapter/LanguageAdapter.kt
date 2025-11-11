package com.xr.common.portal.feature.translate.view.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.xr.common.portal.R

class LanguageAdapter(mData: ArrayList<String>) :
    BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_text, mData) {
    override fun convert(holder: BaseViewHolder, item: String) {
        holder.setText(R.id.tv_text, item)
    }
}