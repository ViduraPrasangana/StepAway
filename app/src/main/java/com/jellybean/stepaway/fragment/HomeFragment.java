package com.jellybean.stepaway.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jellybean.stepaway.MainActivity;
import com.jellybean.stepaway.model.Device;
import com.jellybean.stepaway.DeviceAdapter;
import com.jellybean.stepaway.R;
import com.rodolfonavalon.shaperipplelibrary.ShapeRipple;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    ShapeRipple searchRipple;
    ImageView imageView;
    RecyclerView currentDevicesView;
    DeviceAdapter adapter;
    ArrayList<Device> devices;
    boolean rippleStates;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String SEARCH_STATUS = "searchStatus";


    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) requireActivity();
        setRipple(mainActivity.isSearchStatus());
        if(mainActivity.getMyService() !=null){
            setDevices(mainActivity.getMyService().getDevices());
        }
    }


    public static HomeFragment newInstance(boolean param1) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putBoolean(SEARCH_STATUS, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            rippleStates = getArguments().getBoolean(SEARCH_STATUS);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        searchRipple = view.findViewById(R.id.ripple);
        imageView = view.findViewById(R.id.image);
        currentDevicesView = view.findViewById(R.id.currentDevices);
        devices = new ArrayList<Device>();
        adapter = new DeviceAdapter(devices);
        currentDevicesView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        currentDevicesView.setAdapter(adapter);

        setRipple(rippleStates);

        return view;
    }

    public void setRipple(boolean status){
        if(!status) {
            searchRipple.stopRipple();
            searchRipple.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            rippleStates = false;
        }
        else {
            searchRipple.setVisibility(View.VISIBLE);
            searchRipple.startRipple();
            imageView.setVisibility(View.GONE);
            rippleStates = true;
        }
    }
    public boolean getRippleStatus(){
        return rippleStates;
    }

    public void setDevices(ArrayList<Device> devices) {
        this.devices = devices;
        adapter.setDataset(this.devices);
        adapter.notifyDataSetChanged();
    }

    public ArrayList<Device> getDevices() {
        return devices;
    }

    public void addDevice(Device device){
        devices.add(device);
        adapter.notifyDataSetChanged();
    }
    public void updateDevices(){
        adapter.notifyDataSetChanged();
    }

}