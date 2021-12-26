package com.jellybean.stepaway;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jellybean.stepaway.model.Device;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DeviceHistoryAdapter extends RecyclerView.Adapter<DeviceHistoryAdapter.ViewHolder> {
    private ArrayList<Device> dataset;
    public DeviceHistoryAdapter(ArrayList<Device> devices) {
        dataset = devices;
    }

    public void setDataset(ArrayList<Device> dataset) {
        this.dataset = dataset;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView timeView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            timeView = itemView.findViewById(R.id.time);

        }

        public TextView getTimeView() {
            return timeView;
        }

    }
    @Override
    public int getItemViewType(int position) {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous
        return dataset.get(position).getThreatLevel().getValue();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType){
            case 1:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_item_level_1, parent, false);
                break;
            case 2:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_item_level_2, parent, false);
                break;
            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_item_level_3, parent, false);
                break;
        }


        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Device device = dataset.get(position);
        String distance = ((double) Math.round(device.getAverageDistance()*1000)/1000)+" m";
        Date date=new Date(device.getLastIdentifiedTime());
        DateFormat df = new SimpleDateFormat("yyyy:MM:dd:HH:mm");
        String username = (device.getUserName() != null ? device.getUserName():"Unknown User")+" : "+df.format(date);
        holder.getTimeView().setText(username);
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }
}
