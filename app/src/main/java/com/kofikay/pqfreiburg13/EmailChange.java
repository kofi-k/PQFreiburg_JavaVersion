package com.kofikay.pqfreiburg13;


import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EmailChange extends AppCompatActivity {


    private EditText email_user;
    private FirebaseUser currentUser;
    private Dialog dialog;
    private TextView isEmailVerified, verifyNowTxt;
    private Button btnVerifyEmail;
    private ProgressBar verifyNowTxtProgress, EmailchangesProgress;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.email_change);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Email");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        isEmailVerified = findViewById(R.id.isEmailVerified);
        email_user = findViewById(R.id.email_user);
        btnVerifyEmail = findViewById(R.id.verifyEmail);
        verifyNowTxt = findViewById(R.id.verifyNowTxt);
        verifyNowTxtProgress = findViewById(R.id.verifyNowTxtProgress);
        EmailchangesProgress = findViewById(R.id.EmailchangesProgress);

       /* SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.refreshLayout);
        swipeRefreshLayout.setOnRefreshListener(
                () -> {
                    CheckVerifyStatus();
                    // This line is important as it explicitly
                    // refreshes only once
                    // If "true" it implicitly refreshes forever
                    swipeRefreshLayout.setRefreshing(false);
                }
        );*/
        dialog = new Dialog(this, R.style.DialogAnimation);
        CheckVerifyStatus();

        email_user.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                email_user.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        btnVerifyEmail.setEnabled(false);
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        btnVerifyEmail.setEnabled(true);
                        btnVerifyEmail.setOnClickListener(view -> CheckVerificationIfUserAlreadyVerified());

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        btnVerifyEmail.setEnabled(true);
                        btnVerifyEmail.setOnClickListener(view -> CheckVerificationIfUserAlreadyVerified());


                    }
                });

            } else {
                EmailchangesProgress.setVisibility(View.INVISIBLE);
                btnVerifyEmail.setEnabled(false);
            }
        });

    }

    private void ProceedToVerifyPassword() {
        dialog.setContentView(R.layout.provide_password);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        EditText PasswordConfirmation = dialog.findViewById(R.id.PasswordConfirmation);
        TextView CancelproceedBtn = dialog.findViewById(R.id.CancelproceedBtn);
        Button proceedBtn = dialog.findViewById(R.id.proceedBtn);
        ProgressBar proceedBtnProgress = dialog.findViewById(R.id.proceedBtnProgress);
        proceedBtnProgress.setVisibility(View.INVISIBLE);
        String UserPassword = PasswordConfirmation.getText().toString();

        final  String OldEmail = currentUser.getEmail();

        CancelproceedBtn.setOnClickListener(view1 -> dialog.dismiss());
        proceedBtn.setOnClickListener(view12 -> {
            proceedBtnProgress.setVisibility(View.VISIBLE);
            proceedBtn.setEnabled(false);

            AuthCredential authCredential = EmailAuthProvider.getCredential(OldEmail, UserPassword);
            currentUser.reauthenticate(authCredential).addOnSuccessListener(unused -> {
                showMessage("Password valid");
                String NewEmail = email_user.getText().toString();
                currentUser.updateEmail(NewEmail).addOnSuccessListener(unused1 -> {
                    DocumentReference documentReference = fStore.collection("User Profile")
                            .document(currentUser.getUid());
                    Map<String, Object> edited = new HashMap<>();
                    edited.put("Email", NewEmail);
                    documentReference.update(edited);
                    showMessage("Email changes saved successfully.");
                    logout();
                }).addOnFailureListener(e -> {showMessage(e.getMessage());finish();});
            })
                    .addOnFailureListener(e -> {showMessage(e.getMessage());finish();});
        });
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right,
                R.anim.slide_out_left);
        finish();
    }

    private void CheckVerifyStatus() {
        if (currentUser.isEmailVerified()) {
            email_user.setText(currentUser.getEmail());
            isEmailVerified.setText("Email is verified ✅");
        }
        else if (email_user.getText().toString().isEmpty()) {
            btnVerifyEmail.setEnabled(false);

            email_user.setText(currentUser.getEmail());
            isEmailVerified.setText("Email is not verified ❌");
            verifyNowTxt.setVisibility(View.VISIBLE);

            verifyNowTxt.setOnClickListener(view -> {

                String inputEmail = email_user.getText().toString();
                if (currentUser.getEmail().equals(inputEmail)) {
                    verifyNowTxtProgress.setVisibility(View.VISIBLE);
                    verifyNowTxt.setVisibility(View.INVISIBLE);

                    currentUser.sendEmailVerification().addOnSuccessListener(unused -> {
                        showMessage("verification mail sent to " + inputEmail);
                        verifyNowTxtProgress.setVisibility(View.INVISIBLE);
                        verifyNowTxt.setVisibility(View.INVISIBLE);
                    })
                            .addOnFailureListener(e -> {
                                showMessage(e.getMessage());
                                verifyNowTxtProgress.setVisibility(View.INVISIBLE);
                                verifyNowTxt.setVisibility(View.VISIBLE);
                            });
                }
                else if (inputEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(inputEmail).matches()) {
                    email_user.requestFocus();
                    email_user.setError("verify field");
                    verifyNowTxtProgress.setVisibility(View.INVISIBLE);
                    verifyNowTxt.setVisibility(View.VISIBLE);
                } else {
                    showMessage("Email provided is different from signed in user.\nProvide password to proceed");
                    ProceedToVerifyPassword();
                }

            });
        }
    }

    private void CheckVerificationIfUserAlreadyVerified() {

        String Email = email_user.getText().toString();
        btnVerifyEmail.setVisibility(View.VISIBLE);
        verifyNowTxtProgress.setVisibility(View.INVISIBLE);

        if (Email.equals(currentUser.getEmail())&&currentUser.isEmailVerified()) {
            showMessage("Email verified");
        } else if (Email.isEmpty()) {
            showMessage("verify field");
            email_user.requestFocus();
            btnVerifyEmail.setEnabled(false);
        } else if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
            showMessage("verify field");
            email_user.requestFocus();
        } else {
            showMessage("Email provided is different from signed in user.\nProvide password to proceed");
            ProceedToVerifyPassword();
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