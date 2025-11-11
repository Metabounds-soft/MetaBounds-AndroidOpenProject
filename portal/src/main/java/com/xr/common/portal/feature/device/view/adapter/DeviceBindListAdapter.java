package com.xr.common.portal.feature.device.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SizeUtils;
import com.metabounds.libglass.v2.bean.MBDevice;
import com.xr.common.portal.databinding.ItemSearchDeviceBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 作用描述
 * @Author: bigfish
 * @CreateDate: 2022/3/21 14:34
 */
public class DeviceBindListAdapter extends RecyclerView.Adapter<DeviceBindListAdapter.ViewHolder> {
    private final List<MBDevice> deviceList;
    private ViewGroup.LayoutParams layoutParams;
    private OnItemClickListener mOnItemClickListener;

    public DeviceBindListAdapter() {
        deviceList = new ArrayList<>();
    }

    public synchronized void addDevice(final MBDevice mbDevice) {
        boolean isHave = false;
        for (int i = 0; i < deviceList.size(); i++) {
            if (deviceList.get(i).getDeviceIdentity().equals(mbDevice.getDeviceIdentity())) {
                isHave = true;
                break;
            }
        }
        if (!isHave) {
            deviceList.add(mbDevice);
            notifyDataSetChanged();
        }
    }

    public void clear() {
        deviceList.clear();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSearchDeviceBinding binding = ItemSearchDeviceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(72.0f));
        return new ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final MBDevice glassDevice = deviceList.get(position);
        holder.bind(glassDevice);
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private MBDevice mGlassDevice;
        private ItemSearchDeviceBinding mBinding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public final void bind(final MBDevice glassDevice) {
            mBinding = ItemSearchDeviceBinding.bind(itemView);
            mGlassDevice = glassDevice;


            mBinding.getRoot().setLayoutParams(layoutParams);
            mBinding.tvDeviceName.setText(mGlassDevice.getName());
            mBinding.tvDeviceMac.setText(mGlassDevice.getDeviceIdentity());
            mBinding.rightButton.setVisibility(View.VISIBLE);
            mBinding.rightButton.setOnClickListener(v -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(glassDevice);
                }
            });

        }
    }

    public interface OnItemClickListener {
        void onItemClick(MBDevice glassDevice);
    }
}
