package com.jellybean.stepaway;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jellybean.stepaway.fragment.BottomNavigationDrawerFragment;
import com.jellybean.stepaway.fragment.HistoryFragment;
import com.jellybean.stepaway.fragment.HomeFragment;
import com.jellybean.stepaway.fragment.SettingsFragment;
import com.jellybean.stepaway.model.Device;
import com.jellybean.stepaway.service.DeviceIdentifierService;
import com.jellybean.stepaway.service.IdentifierBackgroundService;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements IdentifierBackgroundService.ServiceCallbacks {
    public static final int REQUEST_ACCESS_COARSE_LOCATION = 1;
    public static final int REQUEST_ENABLE_BLUETOOTH = 11;
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 21;
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 31;
    final int REQUEST_ENABLE_BT = 1;
    public static final String myPref = "StepAwaySettings";

    private BottomAppBar bottomAppBar;
    private TextView title;
    private TextView status;

    FrameLayout fragFrame;
    HomeFragment homeFragment;
    HistoryFragment historyFragment;
    SettingsFragment settingsFragment;

    FloatingActionButton fab;

    boolean searchStatus = false;

    private IdentifierBackgroundService myService;
    private boolean bound = false;
    Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomAppBar = findViewById(R.id.bottomAppBar);
        title = findViewById(R.id.title);
        status = findViewById(R.id.status);
        bottomAppBar.replaceMenu(R.menu.main_menu);
        fragFrame = findViewById(R.id.frag_frame);
        fab = findViewById(R.id.fab);

        getPermissions();
        if(!BluetoothAdapter.getDefaultAdapter().isEnabled()){
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        homeFragment = HomeFragment.newInstance(searchStatus);
        historyFragment = new HistoryFragment();
        settingsFragment = new SettingsFragment();

        changeFragment(R.id.home);

        fab.setOnClickListener(v -> {
            toggleSearch();
        });
        setSupportActionBar(bottomAppBar);
        BottomNavigationDrawerFragment bottomNavigationDrawerFragment = new BottomNavigationDrawerFragment();

        bottomAppBar.setNavigationOnClickListener(v -> {
            bottomNavigationDrawerFragment.show(getSupportFragmentManager(),bottomNavigationDrawerFragment.getTag());
        });
        title.setOnClickListener(v -> {
            bottomNavigationDrawerFragment.show(getSupportFragmentManager(),bottomNavigationDrawerFragment.getTag());
        });


    }

    public boolean isSearchStatus() {
        return searchStatus;
    }

    public void setStatusText(String status){
        this.status.setText(status);
    }
    public void toggleSearch(){
        searchStatus = !searchStatus;
        visualizeState();
        if(myService.isServiceStarted()){
            myService.stopService();
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(i);
                startService(i);
            }
            myService.startService();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        bound = false;
    }

    public void visualizeState(){
        homeFragment.setRipple(searchStatus);
        fab.setImageDrawable(ContextCompat.getDrawable(this, searchStatus? R.drawable.ic_outline_pause_24: R.drawable.ic_baseline_track_changes_24));
    }

    public HomeFragment getHomeFragment() {
        return homeFragment;
    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (IdentifierBackgroundService.class.getName().equals(service.service.getClassName())) {

                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        i = new Intent(this, IdentifierBackgroundService.class);
        bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);

//        if(isMyServiceRunning()) {
//            searchStatus = true;
//            visualizeState();
//        }
    }

    public void changeFragment(int menu_id){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        switch (menu_id){
            case R.id.history:
                ft.replace(R.id.frag_frame, historyFragment).commit();
                break;
            case R.id.settings:
                ft.replace(R.id.frag_frame, settingsFragment).commit();
                break;
            default:
                ft.replace(R.id.frag_frame, homeFragment).commit();
                break;

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Access coarse location allowed. You can scan bluetooth devices", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Access coarse location forbidden. You can't scan Bluetooth devices", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void getPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                if (this.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    if (this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("This app needs background location access");
                        builder.setMessage("Please grant location access so this app can detect beacons in the background.");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                        PERMISSION_REQUEST_BACKGROUND_LOCATION);
                            }

                        });
                        builder.show();
                    }
                    else {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Functionality limited");
                        builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons in the background.  Please go to Settings -> Applications -> Permissions and grant background location access to this app.");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                            @Override
                            public void onDismiss(DialogInterface dialog) {
                            }

                        });
                        builder.show();
                    }

                }
            } else {
                if (!this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                            PERMISSION_REQUEST_FINE_LOCATION);
                }
                else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.  Please go to Settings -> Applications -> Permissions and grant location access to this app.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }

            }
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // cast the IBinder and get MyService instance
            IdentifierBackgroundService.LocalBinder binder = (IdentifierBackgroundService.LocalBinder) service;
            myService = binder.getService();
            bound = true;
            System.out.println("################# bound");
            myService.setCallbacks(MainActivity.this); // register
            if(myService.isServiceStarted()){
                searchStatus = true;
                visualizeState();
                updateDevices(myService.getDevices());
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    @Override
    public void updateDevices(ArrayList<Device> devices) {
        this.homeFragment.setDevices(devices);
    }
}