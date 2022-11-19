package com.kofikay.pqfreiburg13;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;

public class BottomSheet extends BottomSheetDialogFragment {
    public BottomSheet(){
        //required empty public constructor

    }

    private Context mContext;
    private View view;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext=context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.bottom_sheets_content, container, false);

       // NavigationView menuItemBottomLayout=view.findViewById(R.id.menuItemBottomLayout);

       /* menuItemBottomLayout.setNavigationItemSelectedListener(menuItem -> {

            switch (menuItem.getItemId()){
                case R.id.setting:
                case R.id.help:
                case R.id.privacy:
                case R.id.logoutButton:
                    return true;

            }
            return false;
        });
       */
        return view;

    }
}