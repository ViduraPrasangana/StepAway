package com.jellybean.stepaway.service;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;

import com.jellybean.stepaway.MainActivity;
import com.jellybean.stepaway.R;
import com.jellybean.stepaway.model.Device;

import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


public class BluetoothService {
    DeviceIdentifierService deviceIdentifierService;
    CloudService cloudService;

    BluetoothAdapter bluetoothAdapter;
    BluetoothManager bluetoothManager;
    BluetoothLeScanner bluetoothLeScanner;
    BluetoothLeAdvertiser bluetoothLeAdvertiser;

    AdvertiseData advertiseData;
    AdvertiseSettings advertiseSettings;

    Intent enableIntent;

    boolean advertisable = true;
    Context context ;

    AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.e( "BLE", "Advertising onStartFailure: " + errorCode );
            super.onStartFailure(errorCode);
        }
    };

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            String data = "";
            ScanRecord scanRecord = result.getScanRecord();
            String user = null;

            if(scanRecord != null){
                for (Map.Entry<ParcelUuid, byte[]> entry : scanRecord.getServiceData().entrySet()) {
                    System.out.println(entry.getKey().toString()+" "+new String(entry.getValue(),Charset.forName( "UTF-8" )));
                    if(entry.getKey().toString().equals(context.getString( R.string.ble_uuid ))){
                        user = "+94"+new String(entry.getValue(),Charset.forName( "UTF-8" ));
                    }
                }

//                data = new String(scanRecord.getServiceData().get(scanRecord.getServiceUuids().get(0)), Charset.forName( "UTF-8" ));
            }
            System.out.println("############ found " +result.getDevice().getAddress()+" "+result.getDevice().getName()+" " + data);
            Device device = new Device(
                    result.getDevice().getAddress(),
                    System.currentTimeMillis(),
                    Device.Threat.LEVEL3,
                    user
            );
            deviceIdentifierService.addDevice(device,result.getRssi());
            CloudService cloudService = CloudService.getInstance();
            if(user!=null)cloudService.getUserName(user,device, (IdentifierBackgroundService) context);

        }
    };

    public BluetoothService(DeviceIdentifierService deviceIdentifierService) {
        this.deviceIdentifierService = deviceIdentifierService;
    }

    public void initBLE(Context context){
        this.context = context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();

        advertisable = bluetoothAdapter.isMultipleAdvertisementSupported();
        deviceIdentifierService.setAdvertisable(advertisable);
        this.cloudService = CloudService.getInstance();

        advertiseSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY )
                .setTxPowerLevel( AdvertiseSettings.ADVERTISE_TX_POWER_HIGH )
                .setConnectable( false )
                .build();

        ParcelUuid pUuid = new ParcelUuid( UUID.fromString( context.getString( R.string.ble_uuid ) ) );
        advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName( false )
                .setIncludeTxPowerLevel(false)
                .addServiceData(pUuid, Objects.requireNonNull(cloudService.getUser().getPhoneNumber().substring(3)).getBytes( Charset.forName( "UTF-8" ) ))
                .build();
        System.out.println(advertiseData.toString());
        for (Map.Entry<ParcelUuid, byte[]> entry : advertiseData.getServiceData().entrySet()) {
            System.out.println(entry.getKey().toString()+" "+Arrays.toString(entry.getValue()));
        }
    }

    public void startScanning() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                bluetoothLeScanner.startScan(scanCallback);
                System.out.println("############ searching");
            }
        });
    }
    public void stopScanning() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                bluetoothLeScanner.stopScan(scanCallback);
                System.out.println("############ stopping");
            }
        });
    }

    public void startAdvertising(){
        if(advertisable) bluetoothLeAdvertiser.startAdvertising(advertiseSettings,advertiseData,advertisingCallback);
    }
    public void stopAdvertising(){
        if(advertisable) bluetoothLeAdvertiser.stopAdvertising(advertisingCallback);
    }


}
