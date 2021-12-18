package com.jellybean.stepaway.service;

import com.jellybean.stepaway.model.Device;
import com.jellybean.stepaway.MainActivity;

public class DeviceIdentifierService {
    final int MEASURED_POWER = -69;
    final int N = 2;
    final int AVERAGE_COUNT = 3;

    BluetoothService bluetoothService;
    MainActivity activity ;



    public DeviceIdentifierService(MainActivity activity) {
        bluetoothService = new BluetoothService(this);
        bluetoothService.initBLE(activity);

        this.activity = activity;
    }

    public void startService(){
        bluetoothService.startScanning();
    }
    public void stopService(){
        bluetoothService.stopScanning();
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
