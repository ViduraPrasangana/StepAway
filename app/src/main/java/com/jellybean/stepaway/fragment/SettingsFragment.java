package com.jellybean.stepaway.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.firebase.auth.FirebaseAuth;
import com.jellybean.stepaway.LoginActivity;
import com.jellybean.stepaway.R;

import java.util.Objects;

import static com.jellybean.stepaway.MainActivity.myPref;


public class SettingsFragment extends Fragment {

    Switch vibration,ring,notification;
    Button logout;

    public static String VIBRATE_PREF = "vibrate";
    public static String RING_PREF = "ring";
    public static String NOTIFICATION_PREF = "notification";

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_settings, container, false);
        vibration = view.findViewById(R.id.vibration);
        ring = view.findViewById(R.id.ring);
        notification = view.findViewById(R.id.notification);
        logout = view.findViewById(R.id.logout);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(requireContext(), LoginActivity.class);
                startActivity(i);
                requireActivity().finish();
            }
        });
        vibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                writeToPreference(VIBRATE_PREF,isChecked);
            }
        });
        ring.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                writeToPreference(RING_PREF,isChecked);
            }
        });
        notification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                writeToPreference(NOTIFICATION_PREF,isChecked);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        vibration.setChecked(getPreferenceValue(VIBRATE_PREF));
        ring.setChecked(getPreferenceValue(RING_PREF));
        notification.setChecked(getPreferenceValue(NOTIFICATION_PREF));
    }

    public boolean getPreferenceValue(String setting)
    {
        SharedPreferences sp = requireActivity().getSharedPreferences(myPref,0);
         return sp.getBoolean(setting,true);
    }

    public void writeToPreference(String setting,boolean thePreference)
    {
        SharedPreferences.Editor editor = requireActivity().getSharedPreferences(myPref,0).edit();
        editor.putBoolean(setting, thePreference);
        editor.apply();
    }
}