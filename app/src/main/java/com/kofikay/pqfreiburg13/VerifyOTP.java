package com.kofikay.pqfreiburg13;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class VerifyOTP extends AppCompatActivity {

    int randonnumber;
    String phonenumber;
    private Button  confirmOTPBtn;
    private ProgressBar otpVerifyProgress;
    private EditText sentOTPCodeEdt;
    private FirebaseUser currentUser;
    String otp_text;
    private TextView codeSentToTxt, cancelOTPSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.otp_home);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Verify OTP");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        final StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        cancelOTPSent = findViewById(R.id.cancelOTPSent);
        confirmOTPBtn = findViewById(R.id.confirmOTPBtn);

        otpVerifyProgress = findViewById(R.id.otpVerifyProgress);
        sentOTPCodeEdt = findViewById(R.id.sentOTPCodeEdt);
        codeSentToTxt = findViewById(R.id.codeSentToTxt);
        otpVerifyProgress.setVisibility(View.INVISIBLE);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        Intent intent = getIntent();
        phonenumber = intent.getStringExtra("phone");
        codeSentToTxt.setText("Provide the the 6 digit code sent to the number " + phonenumber + ".");

        confirmOTPBtn.setOnClickListener(view -> verifyOTP());
        cancelOTPSent.setOnClickListener(view -> {
            onBackPressed();
            //finish();
        });


    }

    void initiateSendOTP() {
        try {
            // Construct data
            String apiKey = "apikey=" + "AIzaSyAKYWlQmDvMgHDCgDMN6bHx0oq6n0oZsNs";

            Random random = new Random();
            randonnumber = random.nextInt(999999);
            String message = "&message=" +  "Hey "+currentUser.getDisplayName()+", Your OTP is " + randonnumber;
            String sender = "&sender=" + "PQFreiburg";
            String numbers = "&numbers=" + phonenumber;

            // Send data
            HttpURLConnection conn = (HttpURLConnection) new URL("https://api.textlocal.in/send/?").openConnection();
            String data = apiKey + numbers + message + sender;
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Length", Integer.toString(data.length()));
            conn.getOutputStream().write(data.getBytes("UTF-8"));
            final BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            final StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                stringBuffer.append(line);
            }
            rd.close();
        } catch (Exception e) {
            System.out.println("Error SMS " + e);

        }
    }

    public void GetOTP(View view) {
        initiateSendOTP();
        showMessage("OTP sent to "+phonenumber);
        confirmOTPBtn.setVisibility(View.GONE);
    }

    public void verifyOTP() {
        confirmOTPBtn.setVisibility(View.GONE);
        otpVerifyProgress.setVisibility(View.VISIBLE);

        otp_text = sentOTPCodeEdt.getText().toString().trim();

        if (otp_text.equals(String.valueOf(randonnumber))) {

            showMessage("Number verified!");
            confirmOTPBtn.setVisibility(View.VISIBLE);
            otpVerifyProgress.setVisibility(View.INVISIBLE);

            finish();


        } else {
            showMessage("Invalid OTP\nTry again....");
            confirmOTPBtn.setVisibility(View.VISIBLE);
            otpVerifyProgress.setVisibility(View.INVISIBLE);

        }
    }
    private void showMessage(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
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

