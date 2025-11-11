package com.xr.common.portal.feature.device.view.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.xr.common.portal.R
import com.xr.common.portal.feature.device.model.bean.CmdDeviceRemoteControlBean

class CmdDeviceRemoteControlAdapter(data: MutableList<CmdDeviceRemoteControlBean>) :
    BaseQuickAdapter<CmdDeviceRemoteControlBean, BaseViewHolder>(
        R.layout.item_device_remote_control, data
    ) {
    override fun convert(holder: BaseViewHolder, item: CmdDeviceRemoteControlBean) {
        holder.setText(R.id.tv_type, item.name)
        if (item.result) {
            holder.setText(R.id.tv_result, "成功")
        } else {
            holder.setText(R.id.tv_result, "失败")
        }
        holder.setText(R.id.tv_time, "${item.time}ms")
    }
}