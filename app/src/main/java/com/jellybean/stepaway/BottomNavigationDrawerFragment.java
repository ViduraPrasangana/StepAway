package com.jellybean.stepaway;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BottomNavigationDrawerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BottomNavigationDrawerFragment extends BottomSheetDialogFragment {



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bottom_navigation_drawer, container, false);
        NavigationView navView = view.findViewById(R.id.navigation_view);
        navView.setNavigationItemSelectedListener((menuItem) ->{
            switch (menuItem.getItemId()){
                case R.id.home:
                    return true;
            }
            return true;
        });
        return view;
    }
}