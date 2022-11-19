package com.kofikay.pqfreiburg13;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.hbb20.CountryCodePicker;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ResetViaMobile extends AppCompatActivity {


    private EditText resetPhoneNumber;
    private ProgressBar progressViaPhone;
    private Button continueBtn;
    private  FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private Dialog dialog;
    private FirebaseFirestore firebaseFirestore;
    private String userId,verificationId;
    private CountryCodePicker CodePicker;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    PhoneAuthProvider.ForceResendingToken token;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_via_phone);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Reset Email via Phone");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        resetPhoneNumber = findViewById(R.id.resetPhoneNumber);
        continueBtn = findViewById(R.id.continueBtn);
        dialog = new Dialog(this, R.style.DialogAnimation);
        progressViaPhone = findViewById(R.id.progressViaPhone);
        progressViaPhone.setVisibility(View.INVISIBLE);
        firebaseFirestore = FirebaseFirestore.getInstance();
        userId = firebaseAuth.getCurrentUser().getUid();
        CodePicker = findViewById(R.id.codePicker);


        ShowMobileNumber();
        continueBtn.setOnClickListener(view -> {
           final String Usercontact = resetPhoneNumber.getText().toString();
           final String MobileNUmber = "+" + CodePicker.getSelectedCountryCode()+Usercontact;

           if(Usercontact.isEmpty()){
               resetPhoneNumber.requestFocus();
               showMessage("verify field");
           }else {
               //VerifyPhoneNumber(MobileNUmber);
              // gotoResetCodeDialog();
              if(isPhoneNumberValidate(Usercontact,CodePicker.getDefaultCountryCode()).isValid()){
                  showMessage("implement OTP");
                  VerifyPhoneNumber(MobileNUmber);

              }else {
                  dialog.setContentView(R.layout.error_dialog);
                  dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                  TextView errorInfo = dialog.findViewById(R.id.errorInfo);
                  Button ok = dialog.findViewById(R.id.ok);
                  errorInfo.setText("...hmm, something's wrong with your number.Try again.");
                  dialog.show();
                  ok.setOnClickListener(view1 -> dialog.dismiss());
              }
           }

        });

        /*
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                AuthenticateUser(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                showMessage(e.getMessage());
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationId =s;
                token =forceResendingToken;

            }

            @Override
            public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                super.onCodeAutoRetrievalTimeOut(s);
            }
        };
*/
       // PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId,OTP);

    }
    private void VerifyPhoneNumber(String phoneNumber) {
        continueBtn.setEnabled(false);
        progressViaPhone.setVisibility(View.VISIBLE);
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setActivity(this)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setCallbacks(callbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
      //  OTPDialog();
        /*if (isPhoneNumberValid(mobileNumber, mobileCode)) {
            showMessage("sending OTP...");
            gotoResetCodeDialog();
            continueBtn.setEnabled(true);
            progressViaPhone.setVisibility(View.INVISIBLE);
        } else {
            continueBtn.setEnabled(true);
            progressViaPhone.setVisibility(View.INVISIBLE);
            showMessage("number not valid");
            resetPhoneNumber.requestFocus();
        }*/
    }



    public void AuthenticateUser (PhoneAuthCredential credential){

        Map<String, Object> edited = new HashMap<>();
        edited.put("Contact", credential);
        firebaseFirestore.collection("User Profile").add(edited)
                .addOnSuccessListener(documentReference -> {
                    showMessage("Mobile number saved.");
                    finish();
                }).addOnFailureListener(e -> showMessage(e.getMessage()));
        /*
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> showMessage("Number Authenticated"))
                .addOnFailureListener(e -> showMessage(e.getMessage()));
*/
    }

    private void showMessage(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }

    private void gotoResetCodeDialog() {

        dialog.setContentView(R.layout.reset_code_mode);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        Button viaSms = dialog.findViewById(R.id.viaSms);
        Button viaCall = dialog.findViewById(R.id.viaCall);
        TextView cancel = dialog.findViewById(R.id.cancel);
        cancel.setOnClickListener(view13 -> dialog.dismiss());

        viaCall.setOnClickListener(this::ErrorDialog);
        viaSms.setOnClickListener(view -> OTPDialog());

    }

    private void ErrorDialog(View view) {

        dialog.setContentView(R.layout.error_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        Button ok = dialog.findViewById(R.id.ok);
        ok.setOnClickListener(view1 -> dialog.dismiss());
    }

    private void OTPDialog (){

        dialog.setContentView(R.layout.otp_verify_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        Button verifyOTP = dialog.findViewById(R.id.verifyOTP);
        EditText OTPCodeEdt = dialog.findViewById(R.id.OTPCodeEdt);
        TextView cancelOTP = dialog.findViewById(R.id.cancelOTP);
        ProgressBar otpVerifyProgress = dialog.findViewById(R.id.otpVerifyProgress);
        otpVerifyProgress.setVisibility(View.INVISIBLE);
        final String contact = resetPhoneNumber.getText().toString();
        final String MobileNumber = "+" + CodePicker.getSelectedCountryCode()+contact;

        VerifyPhoneNumber(MobileNumber);

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                AuthenticateUser(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                showMessage(e.getMessage());
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                otpVerifyProgress.setVisibility(View.INVISIBLE);
                verifyOTP.setVisibility(View.VISIBLE);
                verificationId =s;
                token =forceResendingToken;
                showMessage("verification code sent");
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                super.onCodeAutoRetrievalTimeOut(s);
                verifyOTP.setVisibility(View.VISIBLE);
                verifyOTP.setText("Resend");
                otpVerifyProgress.setVisibility(View.INVISIBLE);

            }
        };


        cancelOTP.setOnClickListener(view -> dialog.dismiss());
        verifyOTP.setOnClickListener(view -> {
            if(OTPCodeEdt.getText().toString().isEmpty()){
                OTPCodeEdt.requestFocus();
                showMessage("provide OTP code");
            }else {
                verifyOTP.setVisibility(View.INVISIBLE);
                otpVerifyProgress.setVisibility(View.VISIBLE);
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId,OTPCodeEdt.getText().toString());
                //AuthenticateUser(credential);
            }
        });
    }

    private void ShowMobileNumber() {
        if (currentUser != null) {
            DocumentReference documentReference = firebaseFirestore.collection("User Profile").document(userId);
            documentReference.addSnapshotListener(this, (documentSnapshot, error) -> {
                String contact = documentSnapshot.getString("Contact");
                resetPhoneNumber.setText(contact);
            });
        }
    }

    //phoneNumberValidation
    public static PhoneValidateResponse isPhoneNumberValidate(String mobNumber, String countryCode) {
        PhoneValidateResponse phoneNumberValidate = new PhoneValidateResponse();
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber phoneNumber;
        boolean finalNumber = false;
        PhoneNumberUtil.PhoneNumberType isMobile = null;
        boolean isValid = false;
        try {
            String isoCode = phoneNumberUtil.getRegionCodeForCountryCode(Integer.parseInt(countryCode));
            phoneNumber = phoneNumberUtil.parse(mobNumber, isoCode);
            isValid = phoneNumberUtil.isValidNumber(phoneNumber);
            isMobile = phoneNumberUtil.getNumberType(phoneNumber);
            phoneNumberValidate.setCode(String.valueOf(phoneNumber.getCountryCode()));
            phoneNumberValidate.setPhone(String.valueOf(phoneNumber.getNationalNumber()));
            phoneNumberValidate.setValid(false);

        } catch (NumberParseException | NumberFormatException | NullPointerException e) {
            e.printStackTrace();
        }
        if (isValid && (PhoneNumberUtil.PhoneNumberType.MOBILE == isMobile ||
                PhoneNumberUtil.PhoneNumberType.FIXED_LINE_OR_MOBILE == isMobile)) {
            finalNumber = true;
            phoneNumberValidate.setValid(true);
        }

        return phoneNumberValidate;
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