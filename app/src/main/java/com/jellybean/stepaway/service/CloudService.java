package com.jellybean.stepaway.service;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jellybean.stepaway.DeviceAdapter;
import com.jellybean.stepaway.DeviceHistoryAdapter;
import com.jellybean.stepaway.fragment.HistoryFragment;
import com.jellybean.stepaway.model.Device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class CloudService {
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    static CloudService cloudService;
    public CloudService() {
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }

    public static CloudService getInstance(){
        if(cloudService == null){
            cloudService = new CloudService();
        }
        return cloudService;
    }
    public void sendDeviceToDB(Device device){
        databaseReference.child("recent").child(Objects.requireNonNull(user.getPhoneNumber())).push().setValue(device);
    }

    public ArrayList<Device> loadRecent(DeviceHistoryAdapter deviceAdapter){
        ArrayList<Device> devices = new ArrayList<>();
        databaseReference.child("recent").child(Objects.requireNonNull(user.getPhoneNumber())).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    for (DataSnapshot dataSnapshot:
                    Objects.requireNonNull(task.getResult()).getChildren()) {
                        devices.add(dataSnapshot.getValue(Device.class));

                    }
                    deviceAdapter.setDataset(devices);
                    deviceAdapter.notifyDataSetChanged();
                }
            }
        });

        return devices;
    }

    public FirebaseUser getUser(){
        return firebaseAuth.getCurrentUser();
    }

    public void getUserName(String user, Device device, IdentifierBackgroundService identifierBackgroundService) {
        databaseReference.child("users").child(user).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    device.setUserName(Objects.requireNonNull(task.getResult()).getValue(String.class));
                    identifierBackgroundService.getServiceCallbacks().getHomeFragment().updateDevices();
                }
            }
        });
    }
}
