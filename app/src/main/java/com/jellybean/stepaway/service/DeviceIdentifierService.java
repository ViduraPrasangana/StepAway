package com.jellybean.stepaway.service;

import android.content.Context;
import android.os.Handler;

import com.jellybean.stepaway.model.Device;
import com.jellybean.stepaway.MainActivity;

import java.util.ArrayList;

public class DeviceIdentifierService {
    final int MEASURED_POWER = -69;
    final int N = 2;
    final int AVERAGE_COUNT = 3;
    final int TOGGLE_TIMEOUT = 10000;
    final int RECYCLE_DEVICE_TIMEOUT = 5000;

    public final static float DISTANCE_DANGER = 1;
    public final static float DISTANCE_POTENTIAL_RISK = 2;
    public final static float DISTANCE_WARNING = 3;

    BluetoothService bluetoothService;
    CloudService cloudService;
    MainActivity activity = null;
    Context context;

    Runnable serviceRunnable;
    Runnable serviceRunnable2;
    Handler serviceHandler = new Handler();

    boolean advertisable = true;

    private ArrayList<Device> identifiedDevices;

    Runnable garbageRunnable = new Runnable() {
        @Override
        public void run() {
            for (Device device:
                 identifiedDevices) {
                if(System.currentTimeMillis()-device.getLastIdentifiedTime()>RECYCLE_DEVICE_TIMEOUT){
                    clearDevice(device);
                }
            }
            serviceHandler.postDelayed(this,5000);

        }
    };


    public DeviceIdentifierService(Context activity) {
        if(activity instanceof MainActivity){
            this.activity = (MainActivity) activity;
        }
        else {
            this.context = activity;
        }
        bluetoothService = new BluetoothService(this);
        cloudService = CloudService.getInstance();
        bluetoothService.initBLE(activity);
    }

    public void clearDevice(Device device){
        cloudService.sendDeviceToDB(device);
        identifiedDevices.remove(device);
        if(activity!=null) activity.getHomeFragment().updateDevices();
    }

    public void startService(){
        identifiedDevices = new ArrayList<>();
        if(activity!=null) activity.getHomeFragment().setDevices(identifiedDevices);

        serviceRunnable = new Runnable() {
            @Override
            public void run() {
                bluetoothService.stopAdvertising();
                bluetoothService.startScanning();
                if(activity!=null) activity.setStatusText("Scanning...");
                serviceRunnable2 = new Runnable() {
                    @Override
                    public void run() {
                        bluetoothService.stopScanning();
                        bluetoothService.startAdvertising();
                        if(activity!=null) activity.setStatusText("Advertising...");
                        serviceHandler.postDelayed(serviceRunnable,TOGGLE_TIMEOUT);
                    }
                };
                if(advertisable) serviceHandler.postDelayed(serviceRunnable2, TOGGLE_TIMEOUT);
            }
        };

        serviceHandler.post(serviceRunnable);
        serviceHandler.postDelayed(garbageRunnable,5000);

    }
    public void stopService(){
        for (Device device :
                identifiedDevices) {
            clearDevice(device);
        }
        serviceHandler.removeCallbacks(garbageRunnable);
        identifiedDevices = new ArrayList<>();
        if(activity!=null) activity.getHomeFragment().setDevices(identifiedDevices);

        serviceHandler.removeCallbacks(serviceRunnable);
        serviceHandler.removeCallbacks(serviceRunnable2);
        bluetoothService.stopAdvertising();
        bluetoothService.stopScanning();
        if(activity!=null) activity.setStatusText("");
    }

    public void setAdvertisable(boolean advertisable) {
        this.advertisable = advertisable;
    }

    public double calculateAverageDistance(Device device){
        double sum = 0;
        int size = device.getRssis().size();
        for (int rssi:
        device.getRssis().subList(Math.max(size-AVERAGE_COUNT,0),size-1)){
            sum+=rssi;
        }
        return calculateDistance(sum/AVERAGE_COUNT);
    }
    public double calculateDistance(double rssi){
        return (double) Math.round(Math.pow(10,((double) (MEASURED_POWER-rssi) / (10*N) )) * 100) / 100;
    }

    public void addDevice(Device device,int rssi){
        boolean in = false;
        Device inDevice = null;
        for(Device device1:identifiedDevices){
            if(device1.getMacAddress().equals(device.getMacAddress())) {
                in = true;
                inDevice = device1;
            }
        }
        if(!in){
            device.addRssi(rssi);
            device.setAverageDistance(calculateAverageDistance(device));
            identifiedDevices.add(device);
            if(activity!=null) activity.getHomeFragment().updateDevices();
        }else{
            inDevice.addRssi(rssi);
            inDevice.setAverageDistance(calculateAverageDistance(inDevice));
            inDevice.setLastIdentifiedTime(System.currentTimeMillis());
            if(activity!=null) activity.getHomeFragment().updateDevices();
        }

    }
}
