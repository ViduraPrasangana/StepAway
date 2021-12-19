package com.jellybean.stepaway.fragment;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.jellybean.stepaway.MainActivity;
import com.jellybean.stepaway.R;

import org.jetbrains.annotations.NotNull;

public class BottomNavigationDrawerFragment extends BottomSheetDialogFragment {



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bottom_navigation_drawer, container, false);
        NavigationView navView = view.findViewById(R.id.navigation_view);
        navView.setNavigationItemSelectedListener((menuItem) ->{
            ((MainActivity) requireActivity()).changeFragment(menuItem.getItemId());
            dismiss();
            return true;
        });
        return view;
    }

    @NonNull
    @Override public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);


        ((BottomSheetDialog)dialog).getBehavior().addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {

            @Override public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    //In the EXPANDED STATE apply a new MaterialShapeDrawable with rounded cornes
                    MaterialShapeDrawable newMaterialShapeDrawable = createMaterialShapeDrawable(bottomSheet);
                    ViewCompat.setBackground(bottomSheet, newMaterialShapeDrawable);
                }
            }

            @Override public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        return dialog;
    }

    @NotNull
    private MaterialShapeDrawable createMaterialShapeDrawable(@NonNull View bottomSheet) {
        ShapeAppearanceModel shapeAppearanceModel =

                //Create a ShapeAppearanceModel with the same shapeAppearanceOverlay used in the style
                ShapeAppearanceModel.builder(getContext(), 0, R.style.CustomShapeAppearanceBottomSheetDialog)
                        .build();

        //Create a new MaterialShapeDrawable (you can't use the original MaterialShapeDrawable in the BottoSheet)
        MaterialShapeDrawable currentMaterialShapeDrawable = (MaterialShapeDrawable) bottomSheet.getBackground();
        MaterialShapeDrawable newMaterialShapeDrawable = new MaterialShapeDrawable((shapeAppearanceModel));
        //Copy the attributes in the new MaterialShapeDrawable
        newMaterialShapeDrawable.initializeElevationOverlay(getContext());
        newMaterialShapeDrawable.setFillColor(currentMaterialShapeDrawable.getFillColor());
        newMaterialShapeDrawable.setTintList(currentMaterialShapeDrawable.getTintList());
        newMaterialShapeDrawable.setElevation(currentMaterialShapeDrawable.getElevation());
        newMaterialShapeDrawable.setStrokeWidth(currentMaterialShapeDrawable.getStrokeWidth());
        newMaterialShapeDrawable.setStrokeColor(currentMaterialShapeDrawable.getStrokeColor());
        return newMaterialShapeDrawable;
    }
    @Override public int getTheme() {
        return R.style.CustomBottomSheetDialog;
    }
}