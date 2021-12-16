package com.jellybean.stepaway.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jellybean.stepaway.Device;
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
    RecyclerView currentDevicesView;
    RecyclerView.Adapter adapter;
    ArrayList<Device> devices;
    boolean rippleStates;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String SEARCH_STATUS = "searchStatus";


    public HomeFragment() {
        // Required empty public constructor
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
        currentDevicesView = view.findViewById(R.id.currentDevices);
        devices = new ArrayList<Device>(Arrays.asList(new Device("1234","8.30", Device.Threat.LEVEL1),new Device("1234","8.30", Device.Threat.LEVEL2)));
        adapter = new DeviceAdapter(devices);
        currentDevicesView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        currentDevicesView.setAdapter(adapter);

        setRipple(rippleStates);

        return view;
    }

    public void setRipple(boolean status){
        if(!status) {
            searchRipple.stopRipple();
            rippleStates = false;
        }
        else {
            searchRipple.startRipple();
            rippleStates = true;
        }
    }
    public boolean getRippleStatus(){
        return rippleStates;
    }
}