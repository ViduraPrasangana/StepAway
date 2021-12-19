package com.jellybean.stepaway;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
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
import com.jellybean.stepaway.service.DeviceIdentifierService;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_ACCESS_COARSE_LOCATION = 1;
    public static final int REQUEST_ENABLE_BLUETOOTH = 11;
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 21;
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 31;

    private BluetoothAdapter bluetoothAdapter;
    Vibrator vibrator;
    private BottomAppBar bottomAppBar;
    private TextView title;
    private TextView status;
    int k,l,m;

    FrameLayout fragFrame;
    HomeFragment homeFragment;
    HistoryFragment historyFragment;
    SettingsFragment settingsFragment;

    FloatingActionButton fab;

    DeviceIdentifierService deviceIdentifierService;
    boolean searchStatus = false;

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

        deviceIdentifierService = new DeviceIdentifierService(this);

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
        homeFragment.setRipple(searchStatus);
        fab.setImageDrawable(ContextCompat.getDrawable(this, searchStatus? R.drawable.ic_outline_pause_24: R.drawable.ic_baseline_track_changes_24));
        if(searchStatus) deviceIdentifierService.startService();
        else deviceIdentifierService.stopService();
    }

    public HomeFragment getHomeFragment() {
        return homeFragment;
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

    private final BroadcastReceiver devicesFoundReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

//            if (m == 9) {
//                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                    t.setText("\n\noff");
//                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                    rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
//                    String s7 = device.getAddress();
//                    String s9 = device.getName();
//                    listAdapter.add(device.getName() + "\n" + device.getAddress());
//                    String s6 = Integer.toString(rssi);
//                    int h = listAdapter.getPosition(s7);
//                    long z = listAdapter.getItemId(h);
//
//
//                    if (rssi > -70) {
//
//                        int l = macadapter.getPosition(s7);
//                        long min = macadapter.getItemId(l);
//                        if (l == -1) {
//                            macadapter.add(s7);
//                            Toast.makeText(MainActivity.this, s9, Toast.LENGTH_SHORT).show();
//                            vibrator.vibrate(1500);
//                            vibrator.vibrate(1500);
//
//                            ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
//                            toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 2000);
//                        }
//                    }
//
//
//                    listAdapter.notifyDataSetChanged();
//                    macadapter.notifyDataSetChanged();
//                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
//                    t.setText("\n\noff");
//                    k++;
//                    if (k == 200) {
//                        macadapter.clear();
//                        k = 1;
//                    }
//
//                    listAdapter.clear();
//                    bluetoothAdapter.startDiscovery();
//                } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
//
//                    {
//                        t.setText("\n\noff");
//                    }
//
//                }
//            }

        }
    };
}