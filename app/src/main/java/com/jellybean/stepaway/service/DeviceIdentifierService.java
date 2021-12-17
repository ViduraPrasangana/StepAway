package com.jellybean.stepaway.service;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;

import com.jellybean.stepaway.model.Device;
import com.jellybean.stepaway.MainActivity;

import java.text.SimpleDateFormat;

public class DeviceIdentifierService {
    final int MEASURED_POWER = -69;
    final int N = 2;

    BluetoothService bluetoothService;
    MainActivity activity ;


    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            System.out.println("############ found " +result.getDevice().getAddress()+" "+result.getDevice().getName());
            addDevice(new Device(
                    result.getDevice().getAddress(),
                    System.currentTimeMillis(),
                    Device.Threat.LEVEL3
            ),result.getRssi());
        }
    };

    public DeviceIdentifierService(MainActivity activity) {
        bluetoothService = new BluetoothService();
        bluetoothService.initBLE(activity);

        this.activity = activity;
    }

    public void startScan(){
        bluetoothService.startScanning(scanCallback);
    }
    public void stopScan(){
        bluetoothService.stopScanning(scanCallback);
    }
//    public int calculateAverageDistance(){}
    public double calculateDistance(int rssi){
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
            device.addDistance(calculateDistance(rssi));
            activity.getHomeFragment().addDevice(device);
        }else{
            inDevice.addDistance(calculateDistance(rssi));
            activity.getHomeFragment().updateDevices();
        }

    }
}
