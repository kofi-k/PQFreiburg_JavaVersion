package com.kofikay.pqfreiburg13;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ResetPassword extends AppCompatActivity {

    private ProgressBar resetProgress;
    private EditText EmailEdt;
    FirebaseAuth firebaseAuth;
    private Button resetLinkBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password_layout);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Reset Password");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        EmailEdt =findViewById(R.id.ResetLinkEmailEdt);
        resetLinkBtn = findViewById(R.id.resetLinkBtn);
        resetProgress = findViewById(R.id.resetProgress);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser =firebaseAuth.getCurrentUser();
        if(firebaseUser!=null){
            EmailEdt.setText(firebaseUser.getEmail());
           /* resetLinkBtn.setOnClickListener(view -> {
                if(firebaseUser.getEmail().equals(EmailEdt.getText().toString())){
                    sendResetlink(EmailEdt.getText().toString());
                }else{
                    showMessage("Cannot send link to unverified account");
                    finish();
                }
            });*/
        }

        resetProgress.setVisibility(View.INVISIBLE);
        resetLinkBtn.setOnClickListener(v -> {
            resetPassword();
        });
    }

    private void resetPassword() {
        String email = EmailEdt.getText().toString().trim();
        if(email.isEmpty()){
            EmailEdt.requestFocus();
            resetLinkBtn.setVisibility(View.VISIBLE);
            resetLinkBtn.setEnabled(true);
            resetProgress.setVisibility(View.INVISIBLE);
        } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            EmailEdt.requestFocus();
            resetLinkBtn.setVisibility(View.VISIBLE);
            resetLinkBtn.setEnabled(true);
            resetProgress.setVisibility(View.INVISIBLE);
        }
        else{
            sendResetlink(email);
        }
    }

    private void sendResetlink(String email) {
        resetProgress.setVisibility(View.VISIBLE);
        resetLinkBtn.setEnabled(false);
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                resetProgress.setVisibility(View.INVISIBLE);
                resetLinkBtn.setEnabled(true);
                showMessage("Reset link sent successfully, check your inbox");
                finish();
                overridePendingTransition(R.anim.slide_in_right,
                        R.anim.slide_out_left);

            }else{
                resetLinkBtn.setEnabled(true);
                resetProgress.setVisibility(View.INVISIBLE);
                showMessage("...hmm, something's wrong");
            }
        }).addOnFailureListener(e -> {
            resetLinkBtn.setEnabled(true);
            resetProgress.setVisibility(View.INVISIBLE);
            showMessage(e.getMessage());});
    }

    private void showMessage(String text) {
        Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT).show();
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