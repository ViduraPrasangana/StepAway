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
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;

import com.jellybean.stepaway.R;
import com.jellybean.stepaway.model.Device;

import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;


public class BluetoothService {
    final int REQUEST_ENABLE_BT = 1;
    DeviceIdentifierService deviceIdentifierService;

    BluetoothAdapter bluetoothAdapter;
    BluetoothManager bluetoothManager;
    BluetoothLeScanner bluetoothLeScanner;
    BluetoothLeAdvertiser bluetoothLeAdvertiser;

    AdvertiseData advertiseData;
    AdvertiseSettings advertiseSettings;

    Intent enableIntent;

    boolean advertisable = true;

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
            System.out.println("############ found " +result.getDevice().getAddress()+" "+result.getDevice().getName());
            deviceIdentifierService.addDevice(new Device(
                    result.getDevice().getAddress(),
                    System.currentTimeMillis(),
                    Device.Threat.LEVEL3
            ),result.getRssi());
        }
    };

    public BluetoothService(DeviceIdentifierService deviceIdentifierService) {
        this.deviceIdentifierService = deviceIdentifierService;
    }

    public void initBLE(Activity context){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();

        advertisable = bluetoothAdapter.isMultipleAdvertisementSupported();
        deviceIdentifierService.setAdvertisable(advertisable);

        if(!bluetoothAdapter.isEnabled()){
            enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        advertiseSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY )
                .setTxPowerLevel( AdvertiseSettings.ADVERTISE_TX_POWER_HIGH )
                .setConnectable( false )
                .build();

        ParcelUuid pUuid = new ParcelUuid( UUID.fromString( context.getString( R.string.ble_uuid ) ) );

        advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName( false )
                .setIncludeTxPowerLevel(false)
                .addServiceUuid( pUuid )
                .build();
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
