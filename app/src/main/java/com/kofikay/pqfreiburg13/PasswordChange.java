package com.kofikay.pqfreiburg13;


import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Pattern;

public class PasswordChange extends AppCompatActivity {


    private Dialog dialog;
    private FirebaseUser currentUser;
    private Button verifyPassordChange;
    private ProgressBar PasswordchangesProgress;
    FirebaseFirestore fStore;
    private EditText changePasswordEdt;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" + "(?=.*[0-9])"+//at least number
                    "(?=.*[a-z])(?=.*[A-Z])"+ //at least 1 lower and upper case letter
                    "(?=.*[@#$%^&+=])" +// at least 1 special character
                    "(?=\\S+$)" +            // no white spaces
                    ".{4,}" +                // at least 4 characters
                    "$");


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_change);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Change password");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        verifyPassordChange = findViewById(R.id.verifyPassordChange);
        PasswordchangesProgress = findViewById(R.id.PasswordchangesProgress);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        changePasswordEdt = findViewById(R.id.changePasswordEdt);
        TextView forgotPsd = findViewById(R.id.forgotPsd);
        dialog = new Dialog(this, R.style.DialogAnimation);
        //verifyPassordChange.setVisibility(View.INVISIBLE);
        PasswordchangesProgress.setVisibility(View.INVISIBLE);

        forgotPsd.setOnClickListener(this::OpenPasswordChangeDialog);

        final String userPassword = changePasswordEdt.getText().toString().trim();
        verifyPassordChange.setOnClickListener(v -> {
            verifyPassordChange.setEnabled(false);
            PasswordchangesProgress.setVisibility(View.VISIBLE);
            if (currentUser!=null) {
                for (UserInfo userInfo : currentUser.getProviderData()) {
                    if (userInfo.getProviderId().equals("google.com")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Send password resent link").
                                setMessage("You will receive an email to reset your password at:\n" + currentUser.getEmail())
                                .setPositiveButton("Proceed", (dialog, which) -> {
                                    sendResetlink(currentUser.getEmail());
                                    dialog.dismiss();
                                    finish();
                                }).setNegativeButton("Cancel", (dialog, which) -> {
                            dialog.dismiss();
                            verifyPassordChange.setEnabled(true);
                            PasswordchangesProgress.setVisibility(View.INVISIBLE);
                        });
                        builder.create().show();

                    } else {
                        if (userPassword.isEmpty()) {
                            verifyPassordChange.setVisibility(View.VISIBLE);
                            verifyPassordChange.setEnabled(true);
                            PasswordchangesProgress.setVisibility(View.INVISIBLE);
                            changePasswordEdt.requestFocus();
                        } else {
                            verifyPassordChange.setEnabled(false);
                            PasswordchangesProgress.setVisibility(View.VISIBLE);
                            verifyOldPassword();
                        }
                    }
                }
            }
        });

    }
    private void ProceedToVerifyPassword() {
        final String NewPassword = changePasswordEdt.getText().toString().trim();
        if (currentUser!=null) {
            for (UserInfo userInfo : currentUser.getProviderData()) {
                if (userInfo.getProviderId().equals("google.com")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Send password resent link").
                            setMessage("You will receive an email to reset your password at:\n" + currentUser.getEmail())
                            .setPositiveButton("Proceed", (dialog, which) -> {
                                sendResetlink(currentUser.getEmail());
                                dialog.dismiss();
                            }).setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    });
                    builder.create().show();

                } else {
                    AuthCredential authCredential = EmailAuthProvider.getCredential(currentUser.getEmail(), NewPassword);
                    currentUser.reauthenticate(authCredential).addOnSuccessListener(unused -> {
                        showMessage("Password valid");
                        currentUser.updatePassword(NewPassword).addOnSuccessListener(unused1 -> {
                            //DocumentReference documentReference = fStore.collection("User Profile").document(currentUser.getUid());
                            fStore.collection("User Profile")
                                    .document(currentUser.getUid())
                                    .update("Password", NewPassword);
                            //Map<String, Object> edited = new HashMap<>();
                            //edited.put("Password", NewPassword);
                            //documentReference.update(edited);
                            showMessage("Password changed successfully.");
                            verifyPassordChange.setEnabled(true);
                            PasswordchangesProgress.setVisibility(View.INVISIBLE);
                            logout();
                        }).addOnFailureListener(e -> showMessage(e.getMessage()));
                    }).addOnFailureListener(e -> showMessage(e.getMessage()));
                }
            }
        }
    }
    private void verifyOldPassword(){
      //  verifyPassordChange.setEnabled(false);
        PasswordchangesProgress.setVisibility(View.VISIBLE);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("User Profile");
        final String oldPass=changePasswordEdt.getText().toString().trim();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String currentPass= (String) dataSnapshot.child("Password").getValue();
                if (oldPass.equals(currentPass)){
                    verifyPassordChange.setEnabled(true);
                    PasswordchangesProgress.setVisibility(View.INVISIBLE);
                    setNewPassword();
                }else if(!oldPass.equals(currentPass)){
                    verifyPassordChange.setEnabled(true);
                    PasswordchangesProgress.setVisibility(View.INVISIBLE);
                    showMessage("wrong password");
                    finish();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                showMessage(databaseError.getMessage());
            }
        });
    }
    public void setNewPassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Password").
                setMessage("New Password ");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("Proceed", (dialog, which) -> {
            String newPass = input.getText().toString();
            if (!PASSWORD_PATTERN.matcher(newPass).matches()) {
                showMessage("Password is too weak");
                input.requestFocus();
            }else if(newPass.isEmpty()){
                input.requestFocus();
            }else{
                ProceedToVerifyPassword();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.create().show();
    }

    private void sendResetlink(String email) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if(task.isSuccessful()){

                showMessage("Reset link sent successfully");
                finish();
                overridePendingTransition(R.anim.slide_in_right,
                        R.anim.slide_out_left);

            }else{

                showMessage("...hmm, something's wrong");
            }
        }).addOnFailureListener(e -> {

            showMessage(e.getMessage());});
    }
    private void OpenPasswordChangeDialog(View view) {
        dialog.setContentView(R.layout.password_change_mode);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        Button viaPhone = dialog.findViewById(R.id.viaPhone);
        Button viaEmail = dialog.findViewById(R.id.viaEmail);
        TextView cancel = dialog.findViewById(R.id.cancelReset);

        viaEmail.setOnClickListener(this::gotoPasswordResetViaEmail);
        viaPhone.setOnClickListener(this::gotoPasswordResetViaPhone);
        cancel.setOnClickListener(view13 -> dialog.dismiss());
    }
    public void gotoPasswordResetViaEmail(View view) {

        dialog.dismiss();
        Intent intent = new Intent(getApplicationContext(), ResetPassword.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right,
                R.anim.slide_out_left);

    }
    public void gotoPasswordResetViaPhone(View view) {
        dialog.dismiss();
        Intent intent = new Intent(getApplicationContext(), ResetViaMobile.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right,
                R.anim.slide_out_left);

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