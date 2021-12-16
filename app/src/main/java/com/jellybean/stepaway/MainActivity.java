package com.jellybean.stepaway;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jellybean.stepaway.fragment.HistoryFragment;
import com.jellybean.stepaway.fragment.HomeFragment;
import com.jellybean.stepaway.fragment.SettingsFragment;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_ACCESS_COARSE_LOCATION = 1;
    public static final int REQUEST_ENABLE_BLUETOOTH = 11;
    private BluetoothAdapter bluetoothAdapter;
    Vibrator vibrator;
    private BottomAppBar bottomAppBar;
    private TextView title;
    int k,l,m;

    FrameLayout fragFrame;
    HomeFragment homeFragment;
    HistoryFragment historyFragment;
    SettingsFragment settingsFragment;

    FloatingActionButton fab;

    boolean searchStatus = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomAppBar = findViewById(R.id.bottomAppBar);
        title = findViewById(R.id.title);
        bottomAppBar.replaceMenu(R.menu.main_menu);
        fragFrame = findViewById(R.id.frag_frame);
        fab = findViewById(R.id.fab);

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
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        checkBluetoothstate();

        new IntentFilter(BluetoothDevice.ACTION_FOUND);

        registerReceiver(devicesFoundReciever, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(devicesFoundReciever, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(devicesFoundReciever, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

//        t.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (t.isChecked())
//                {
//                    if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
//                        if (checkCoarseLocationPermission()) {
//                            listAdapter.clear();
//                            l=1;
//                            m=9;
//                            bluetoothAdapter.startDiscovery();
//                            Toast.makeText(MainActivity.this, "Scanning Started", Toast.LENGTH_SHORT).show();
//
//                        }
//                    } else {
//                        checkBluetoothstate();
//                    }
//
//                }
//                else {
//                    m=2;
//                    listAdapter.clear();
//                    macadapter.clear();
//                }
//            }
//        });


        checkCoarseLocationPermission();
    }

    public void toggleSearch(){
        searchStatus = !searchStatus;
        homeFragment.setRipple(searchStatus);
        fab.setImageDrawable(ContextCompat.getDrawable(this, searchStatus? R.drawable.ic_outline_pause_24: R.drawable.ic_baseline_track_changes_24));
    }

    private boolean checkCoarseLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ACCESS_COARSE_LOCATION);
            return false;
        } else return true;

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
    private void checkBluetoothstate() {
        if (bluetoothAdapter == null)
            Toast.makeText(this, "Bluetooth is not supported in your device !", Toast.LENGTH_SHORT).show();
        else {
            if (bluetoothAdapter.isEnabled()) {
                if (bluetoothAdapter.isDiscovering()) {
                    Toast.makeText(this, "Device discovering process ...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Bluetooth is enabled", Toast.LENGTH_SHORT).show();
//                    t.setEnabled(true);
                }
            } else {
                Toast.makeText(this, "You need to enable bluetooth", Toast.LENGTH_SHORT).show();
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            checkBluetoothstate();
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