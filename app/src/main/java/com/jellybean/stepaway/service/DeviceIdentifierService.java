package com.jellybean.stepaway.service;

import android.os.Handler;

import com.jellybean.stepaway.model.Device;
import com.jellybean.stepaway.MainActivity;

public class DeviceIdentifierService {
    final int MEASURED_POWER = -69;
    final int N = 2;
    final int AVERAGE_COUNT = 3;
    final int TOGGLE_TIMEOUT = 10000;

    BluetoothService bluetoothService;
    MainActivity activity ;

    Runnable serviceRunnable;
    Runnable serviceRunnable2;
    Handler serviceHandler = new Handler();

    boolean advertisable = true;

    public DeviceIdentifierService(MainActivity activity) {
        bluetoothService = new BluetoothService(this);
        bluetoothService.initBLE(activity);

        this.activity = activity;
    }

    public void startService(){

        serviceRunnable = new Runnable() {
            @Override
            public void run() {
                bluetoothService.stopAdvertising();
                bluetoothService.startScanning();
                activity.setStatusText("Scanning...");
                serviceRunnable2 = new Runnable() {
                    @Override
                    public void run() {
                        bluetoothService.stopScanning();
                        bluetoothService.startAdvertising();
                        activity.setStatusText("Advertising...");
                        serviceHandler.postDelayed(serviceRunnable,TOGGLE_TIMEOUT);
                    }
                };
                if(advertisable) serviceHandler.postDelayed(serviceRunnable2, TOGGLE_TIMEOUT);
            }
        };

        serviceHandler.post(serviceRunnable);

    }
    public void stopService(){
        serviceHandler.removeCallbacks(serviceRunnable);
        serviceHandler.removeCallbacks(serviceRunnable2);
        bluetoothService.stopAdvertising();
        bluetoothService.stopScanning();
        activity.setStatusText("");
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
        for(Device device1:activity.getHomeFragment().getDevices()){
            if(device1.getMacAddress().equals(device.getMacAddress())) {
                in = true;
                inDevice = device1;
            }
        }
        if(!in){
            device.addRssi(rssi);
            device.setAverageDistance(calculateAverageDistance(device));
            activity.getHomeFragment().addDevice(device);
        }else{
            inDevice.addRssi(rssi);
            inDevice.setAverageDistance(calculateAverageDistance(inDevice));
            activity.getHomeFragment().updateDevices();
        }

    }
}
