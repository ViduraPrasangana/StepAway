package com.jellybean.stepaway.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jellybean.stepaway.DeviceAdapter;
import com.jellybean.stepaway.R;
import com.jellybean.stepaway.model.Device;
import com.jellybean.stepaway.service.CloudService;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment {
    DeviceAdapter adapter;
    RecyclerView historyView;
    CloudService cloudService;
    ArrayList<Device> historyDevices;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        historyView = view.findViewById(R.id.historyView);

        cloudService = CloudService.getInstance();
        adapter = new DeviceAdapter(new ArrayList<>());
        historyView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        historyView.setAdapter(adapter);
        cloudService.loadRecent(adapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        cloudService.loadRecent(adapter);
    }
}