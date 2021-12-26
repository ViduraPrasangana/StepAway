package com.jellybean.stepaway.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;

import com.jellybean.stepaway.MainActivity;
import com.jellybean.stepaway.R;
import com.jellybean.stepaway.fragment.SettingsFragment;
import com.jellybean.stepaway.model.Device;

import java.util.ArrayList;
import java.util.Iterator;

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
    IdentifierBackgroundService service;

    Runnable serviceRunnable;
    Runnable serviceRunnable2;
    Handler serviceHandler = new Handler();

    boolean advertisable = true;

    private ArrayList<Device> identifiedDevices;

    Runnable garbageRunnable = new Runnable() {
        @Override
        public void run() {
            Iterator<Device> itr = identifiedDevices.iterator();
            while (itr.hasNext()) {
                Device d = itr.next();
                if(System.currentTimeMillis()-d.getLastIdentifiedTime()>RECYCLE_DEVICE_TIMEOUT){
                    itr.remove();
                    clearDevice(d);
                }
            }

            for (Device device:
                 identifiedDevices) {
                if(System.currentTimeMillis()-device.getLastIdentifiedTime()>RECYCLE_DEVICE_TIMEOUT){
                    clearDevice(device);
                }
            }
            serviceHandler.postDelayed(this,5000);

        }
    };


    public DeviceIdentifierService(IdentifierBackgroundService service) {
        this.service = service;
        bluetoothService = new BluetoothService(this);
        cloudService = CloudService.getInstance();
        bluetoothService.initBLE(service);
    }

    public void clearDevice(Device device){
        cloudService.sendDeviceToDB(device);
        if(service.getServiceCallbacks() != null) service.getServiceCallbacks().getHomeFragment().updateDevices();
    }

    public void startService(){
        identifiedDevices = new ArrayList<>();
        System.out.println("Scanning");
        if(service.getServiceCallbacks() != null) service.getServiceCallbacks().updateDevices(identifiedDevices);

        serviceRunnable = new Runnable() {
            @Override
            public void run() {
                bluetoothService.stopAdvertising();
                bluetoothService.startScanning();
                if(service.getServiceCallbacks() != null) service.getServiceCallbacks().setStatusText("Scanning...");
                serviceRunnable2 = new Runnable() {
                    @Override
                    public void run() {
                        bluetoothService.stopScanning();
                        bluetoothService.startAdvertising();
                        if(service.getServiceCallbacks() != null)  service.getServiceCallbacks().setStatusText("Advertising...");
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
        Iterator<Device> itr = identifiedDevices.iterator();
        while (itr.hasNext()) {
            Device d = itr.next();
            clearDevice(d);
            itr.remove();
        }

        serviceHandler.removeCallbacks(garbageRunnable);
        identifiedDevices = new ArrayList<>();
        if(service.getServiceCallbacks() != null) service.getServiceCallbacks().updateDevices(identifiedDevices);

        serviceHandler.removeCallbacks(serviceRunnable);
        serviceHandler.removeCallbacks(serviceRunnable2);
        bluetoothService.stopAdvertising();
        bluetoothService.stopScanning();
        if(service.getServiceCallbacks() != null) service.getServiceCallbacks().setStatusText("");
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
            if(service.getServiceCallbacks() != null) service.getServiceCallbacks().getHomeFragment().updateDevices();
            if(service.getPreferenceValue(SettingsFragment.VIBRATE_PREF)) vibrateDevice();
            if(service.getPreferenceValue(SettingsFragment.NOTIFICATION_PREF)) createTempNotification();
            if(service.getPreferenceValue(SettingsFragment.RING_PREF)) ring();

        }else{
            inDevice.addRssi(rssi);
            inDevice.setAverageDistance(calculateAverageDistance(inDevice));
            inDevice.setLastIdentifiedTime(System.currentTimeMillis());
            if(service.getServiceCallbacks() != null)  service.getServiceCallbacks().getHomeFragment().updateDevices();
        }

    }

    private void vibrateDevice() {
        Vibrator v = (Vibrator) service.getSystemService(Context.VIBRATOR_SERVICE);
// Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(3000, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(3000);
        }
    }
    public void ring(){
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(service.getApplicationContext(), notification);
        r.play();
    }
    private void createTempNotification(){
        String NOTIFICATION_CHANNEL_ID = "STEP_AWAY_CHANNEL_2";
        Notification.Builder notifiBuilder;
        NotificationManager notificationManager =(NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,"Step away notification",NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Step away notification");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(false);
            notificationManager.createNotificationChannel(notificationChannel);

            notifiBuilder = new Notification.Builder(service,NOTIFICATION_CHANNEL_ID);
        } else {
            notifiBuilder = new Notification.Builder(service);
        }


        Notification notification = notifiBuilder.setContentTitle("Nearby person detected")
                .setContentText("Take necessary action")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Step Away")
                .build();

        notificationManager.notify(identifiedDevices.size()+100,notification);

    }

    public ArrayList<Device> getDevices() {
        return identifiedDevices;
    }
}
