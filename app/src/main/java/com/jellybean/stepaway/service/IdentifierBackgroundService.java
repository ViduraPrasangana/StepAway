package com.jellybean.stepaway.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

import com.jellybean.stepaway.MainActivity;
import com.jellybean.stepaway.R;
import com.jellybean.stepaway.model.Device;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static com.jellybean.stepaway.MainActivity.myPref;

public class IdentifierBackgroundService extends Service {
    public final static String START = "START";
    public final static String STOP = "STOP";

    PowerManager.WakeLock wakeLock= null;
    boolean serviceStarted = false;

    DeviceIdentifierService deviceIdentifierService;

    private final IBinder binder = new LocalBinder();
    private MainActivity serviceCallbacks;

    public IdentifierBackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setCallbacks(MainActivity callbacks) {
        serviceCallbacks = callbacks;
    }

    public MainActivity getServiceCallbacks() {
        return serviceCallbacks;
    }

    public boolean isServiceStarted() {
        return serviceStarted;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }
    public void startService(){
        if(serviceStarted) return;
        serviceStarted = true;

        wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"StepAway::lock");
        wakeLock.acquire(100*60*1000L /*10 minutes*/);

        deviceIdentifierService = new DeviceIdentifierService(this);
        deviceIdentifierService.startService();
        Notification notification = createNotification();
        startForeground(1, notification);
    }

    public void stopService(){
        deviceIdentifierService.stopService();
        if(wakeLock.isHeld()){
            wakeLock.release();
        }
        stopForeground(true);
        stopSelf();
        serviceStarted = false;
    }

    private Notification createNotification(){
        String NOTIFICATION_CHANNEL_ID = "STEP_AWAY_CHANNEL";
        Notification.Builder notifiBuilder;
        Intent notificationIntent = new Intent(this, MainActivity.class);
//        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            NotificationManager notificationManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,"Step away notification",NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Step away notification");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(false);
            notificationManager.createNotificationChannel(notificationChannel);

            notifiBuilder = new Notification.Builder(this,NOTIFICATION_CHANNEL_ID);
        } else {
            notifiBuilder = new Notification.Builder(this);
        }


        return notifiBuilder.setContentTitle("Step away")
                .setContentText("Scanning...")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Step Away")
                .build();
    }

    public class LocalBinder extends Binder {
        public IdentifierBackgroundService getService() {
            // Return this instance of MyService so clients can call public methods
            return IdentifierBackgroundService.this;
        }
    }
    public interface ServiceCallbacks {
        void updateDevices(ArrayList<Device> devices);
    }

    public ArrayList<Device> getDevices(){
        return deviceIdentifierService.getDevices();
    }
    public boolean getPreferenceValue(String setting)
    {
        SharedPreferences sp = getSharedPreferences(myPref,0);
        return sp.getBoolean(setting,true);
    }

    public void writeToPreference(String setting,boolean thePreference)
    {
        SharedPreferences.Editor editor = getSharedPreferences(myPref,0).edit();
        editor.putBoolean(setting, thePreference);
        editor.apply();
    }
}
