package com.kofikay.pqfreiburg13;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import de.hdodenhof.circleimageview.CircleImageView;

public class settings_activity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Settings");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        TextView NotiSettings = findViewById(R.id.NotiSettings);
        TextView ThemeSettings = findViewById(R.id.ThemeSettings);
        TextView HelpSettings = findViewById(R.id.HelpSettings);
        TextView SecuritySettings = findViewById(R.id.SecuritySettings);

        NotiSettings.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), NotiSettings.class);
            startActivity(intent);
        });
        ThemeSettings.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ThemeSettings.class);
            startActivity(intent);
        });
        HelpSettings.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), HelpSettings.class);
            startActivity(intent);
        });
        SecuritySettings.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), SecuritySetttings.class);
            startActivity(intent);
        });

    }
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,
                R.anim.slide_out_right);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

}