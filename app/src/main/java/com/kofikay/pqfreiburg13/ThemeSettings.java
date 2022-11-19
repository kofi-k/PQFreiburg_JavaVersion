package com.kofikay.pqfreiburg13;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeSettings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.theme_settings);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Theme");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        boolean isDarkModeOn = sharedPreferences.getBoolean("isDarkModeOn", false);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        RadioButton darkThemeRadioButton = findViewById(R.id.darkThemeRadioButton);
        RadioButton lightThemeRadioButton = findViewById(R.id.lightThemeRadioButton);
        RadioButton sysDefThemeRadioButton = findViewById(R.id.sysDefThemeRadioButton);

        if (isDarkModeOn) {
            darkThemeRadioButton.setChecked(true);
        } else {
            lightThemeRadioButton.setChecked(true);
        }
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {

            switch (checkedId) {
                case R.id.lightThemeRadioButton:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    editor.putBoolean("isDarkModeOn", false);
                    editor.apply();
                    break;
                case R.id.darkThemeRadioButton:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    editor.putBoolean("isDarkModeOn", true);
                    editor.apply();
                    break;
                case R.id.sysDefThemeRadioButton:
                    sysDefThemeRadioButton.setChecked(true);
                    switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                        case Configuration.UI_MODE_NIGHT_YES:
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            editor.putBoolean("isDarkModeOn", true);
                            editor.apply();
                            break;
                        case Configuration.UI_MODE_NIGHT_NO:
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            editor.putBoolean("isDarkModeOn", false);
                            editor.apply();
                            break;
                    }
                    break;
            }
        });
    }

    @Override
    public void onBackPressed() {
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