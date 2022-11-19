package com.kofikay.pqfreiburg13;


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
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AddorChangePhone extends AppCompatActivity {

    private EditText userPhone;
    private FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;
    FirebaseFirestore fStore;
    private Button btnverifyPhone;
    private ProgressBar verifyProgress;
    int randomNumber;
    String phoneNumber;
    private Dialog dialog;
    PhoneAuthCredential phoneAuthCredential;
    private String verificationId, code;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_change);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Mobile Number");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        fStore = FirebaseFirestore.getInstance();
        userPhone = findViewById(R.id.userPhone);
        verifyProgress = findViewById(R.id.verifyProgress);
        btnverifyPhone = findViewById(R.id.verifyPhone);
        CountryCodePicker countryCodePicker = findViewById(R.id.countryPicker);
        verifyProgress.setVisibility(View.INVISIBLE);
        currentUser = firebaseAuth.getCurrentUser();
        dialog = new Dialog(this, R.style.DialogAnimation);


        ShowMobileNumber();
        btnverifyPhone.setOnClickListener(view -> {

            final String mobileNumber = userPhone.getText().toString();
            final String mobileCode = countryCodePicker.getSelectedCountryCode();

            if (mobileNumber.isEmpty()) {
                userPhone.requestFocus();
                showMessage("provide phone number");
            } else {
                btnverifyPhone.setEnabled(false);
                verifyProgress.setVisibility(View.VISIBLE);
                String fullNumber = "+" + mobileCode + mobileNumber;
                VerifyPhoneNumber(fullNumber);
            }
        });
    }

    private void OTPDialog() {

        dialog.setContentView(R.layout.otp_verify_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        Button verifyOTP = dialog.findViewById(R.id.verifyOTP);
        EditText OTPCodeEdt = dialog.findViewById(R.id.OTPCodeEdt);
        TextView cancelOTP = dialog.findViewById(R.id.cancelOTP);
        ProgressBar otpVerifyProgress = dialog.findViewById(R.id.otpVerifyProgress);
        otpVerifyProgress.setVisibility(View.INVISIBLE);

        cancelOTP.setOnClickListener(view -> dialog.dismiss());
        verifyOTP.setOnClickListener(view -> {
            String OTP = OTPCodeEdt.getText().toString();
            if (OTP.isEmpty()) {
                OTPCodeEdt.requestFocus();
                showMessage("provide OTP code");
            } else if (OTP.equals(code)) {
                verifyOTP.setVisibility(View.INVISIBLE);
                otpVerifyProgress.setVisibility(View.VISIBLE);
                if (currentUser!=null) {
                    for (UserInfo userInfo : currentUser.getProviderData()) {
                        String userID = Objects.requireNonNull(currentUser.getUid());
                        if (userInfo.getProviderId().equals("google.com")) {
                            DocumentReference documentReference = fStore.collection("User Profile").document(userID);
                            documentReference.get().addOnCompleteListener(task -> {
                                DocumentSnapshot document = task.getResult();
                                if (!document.exists()) {
                                    //do nothing
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("Contact", phoneNumber);
                                    user.put("User Name", null);
                                    user.put("Last name", null);
                                    fStore.collection("User Profile").document(userID).set(user);
                                }
                            });
                            fStore.collection("User Profile")
                                    .document(currentUser.getUid())
                                    .update("Contact", phoneNumber).addOnSuccessListener(unused -> {

                                showMessage("Number update success!");
                                userPhone.setText(phoneNumber);
                                dialog.dismiss();
                                finish();

                            }).addOnFailureListener(e -> {
                                showMessage(e.getMessage());
                                dialog.dismiss();
                            });
                        } else {
                            fStore.collection("User Profile")
                                    .document(currentUser.getUid())
                                    .update("Contact", phoneNumber).addOnSuccessListener(unused -> {

                                showMessage("Number update success!");
                                userPhone.setText(phoneNumber);
                                dialog.dismiss();
                                finish();
                            }).addOnFailureListener(e -> {
                                showMessage(e.getMessage());
                                dialog.dismiss();
                            });
                        }
                    }
                }
            }else if (code==null){
                showMessage("code is null");
            }
        });
    }

    private void ShowMobileNumber() {
        if (currentUser != null) {
            DocumentReference documentReference = fStore.collection("User Profile")
                    .document(currentUser.getUid());
            documentReference.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.get("Contact") != null) {
                        String contact = documentSnapshot.getString("Contact");
                        userPhone.setText(contact);
                    } else {
                        userPhone.requestFocus();
                    }
                }
            });
        }
    }
    //commented code
        /*
            @SuppressLint("SetTextI18n")
            private void addPhoneNumber(String phoneNumber, String countryCode) {
                btnverifyPhone.setEnabled(false);
                verifyProgress.setVisibility(View.VISIBLE);
                String fullNumber = "+" + countryCode + phoneNumber;

                if (isPhoneNumberValidate(phoneNumber, countryCode).isValid()) {
                    btnverifyPhone.setEnabled(true);
                    verifyProgress.setVisibility(View.INVISIBLE);
                    VerifyPhoneNumber(phoneNumber);

                    dialog.setContentView(R.layout.number_verify);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    TextView userNumb = dialog.findViewById(R.id.userNumb);
                    Button confirmNumBtn = dialog.findViewById(R.id.confirmNum);
                    TextView cancelNumberBtn = dialog.findViewById(R.id.cancelNumber);
                    ProgressBar NumProgress = dialog.findViewById(R.id.NumProgress);
                    NumProgress.setVisibility(View.INVISIBLE);
                    userNumb.setText(fullNumber + "?");
                    dialog.show();
                    cancelNumberBtn.setOnClickListener(view -> dialog.dismiss());
                    confirmNumBtn.setOnClickListener(view -> {
                        NumProgress.setVisibility(View.VISIBLE);
                        confirmNumBtn.setVisibility(View.INVISIBLE);

                        VerifyPhoneNumber(phoneNumber);
                        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                for (UserInfo userInfo : currentUser.getProviderData()) {
                                    String userID = Objects.requireNonNull(currentUser.getUid());
                                    if (userInfo.getProviderId().equals("google.com")){
                                        DocumentReference documentReference = fStore.collection("User Profile").document(userID);
                                        documentReference.get().addOnCompleteListener(task -> {
                                            DocumentSnapshot document = task.getResult();
                                            if(document.exists()){


                                            }else{
                                                Map<String, Object> user = new HashMap<>();
                                                user.put("Contact",phoneNumber );
                                                user.put("User Name", null);
                                                user.put("Last name", null);
                                                fStore.collection("User Profile").document(userID).set(user);

                                            }
                                        });

                                        fStore.collection("User Profile")
                                                .document(currentUser.getUid())
                                                .update("Contact", phoneNumber).addOnSuccessListener(unused -> {
                                            NumProgress.setVisibility(View.INVISIBLE);
                                            confirmNumBtn.setVisibility(View.VISIBLE);
                                            showMessage("Number update success!");
                                            userPhone.setText(phoneNumber);
                                            finish();
                                            dialog.dismiss();
                                        }).addOnFailureListener(e -> {
                                            showMessage(e.getMessage());
                                            NumProgress.setVisibility(View.INVISIBLE);
                                            confirmNumBtn.setVisibility(View.VISIBLE);
                                            confirmNumBtn.setText("Retry");
                                        });
                                    }else{
                                        fStore.collection("User Profile")
                                                .document(currentUser.getUid())
                                                .update("Contact",phoneNumber).addOnSuccessListener(unused -> {
                                            NumProgress.setVisibility(View.INVISIBLE);
                                            confirmNumBtn.setVisibility(View.VISIBLE);
                                            showMessage("Number update success!");
                                            userPhone.setText(phoneNumber);
                                            finish();
                                            dialog.dismiss();
                                        }).addOnFailureListener(e -> {
                                            showMessage(e.getMessage());
                                            NumProgress.setVisibility(View.INVISIBLE);
                                            confirmNumBtn.setVisibility(View.VISIBLE);
                                            confirmNumBtn.setText("Retry");
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                showMessage(e.getMessage());
                            }

                            @Override
                            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                super.onCodeSent(s, forceResendingToken);
                                showMessage("verification code sent");
                            }

                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                                super.onCodeAutoRetrievalTimeOut(s);

                            }
                        };
                        for (UserInfo userInfo : currentUser.getProviderData()) {
                            String userID = Objects.requireNonNull(currentUser.getUid());
                            if (userInfo.getProviderId().equals("google.com")){
                                DocumentReference documentReference = fStore.collection("User Profile").document(userID);
                                documentReference.get().addOnCompleteListener(task -> {
                                    DocumentSnapshot document = task.getResult();
                                    if(document.exists()){


                                    }else{
                                        Map<String, Object> user = new HashMap<>();
                                        user.put("Contact",phoneNumber );
                                        user.put("User Name", null);
                                        user.put("Last name", null);
                                        fStore.collection("User Profile").document(userID).set(user);

                                    }
                                });

                                fStore.collection("User Profile")
                                        .document(currentUser.getUid())
                                        .update("Contact", phoneNumber).addOnSuccessListener(unused -> {
                                    NumProgress.setVisibility(View.INVISIBLE);
                                    confirmNumBtn.setVisibility(View.VISIBLE);
                                    showMessage("Number update success!");
                                    userPhone.setText(phoneNumber);
                                    finish();
                                    dialog.dismiss();
                                }).addOnFailureListener(e -> {
                                    showMessage(e.getMessage());
                                    NumProgress.setVisibility(View.INVISIBLE);
                                    confirmNumBtn.setVisibility(View.VISIBLE);
                                    confirmNumBtn.setText("Retry");
                                });
                            }else{
                                fStore.collection("User Profile")
                                        .document(currentUser.getUid())
                                        .update("Contact",phoneNumber).addOnSuccessListener(unused -> {
                                    NumProgress.setVisibility(View.INVISIBLE);
                                    confirmNumBtn.setVisibility(View.VISIBLE);
                                    showMessage("Number update success!");
                                    userPhone.setText(phoneNumber);
                                    finish();
                                    dialog.dismiss();
                                }).addOnFailureListener(e -> {
                                    showMessage(e.getMessage());
                                    NumProgress.setVisibility(View.INVISIBLE);
                                    confirmNumBtn.setVisibility(View.VISIBLE);
                                    confirmNumBtn.setText("Retry");
                                });
                            }
                        }
                    });

                     initiateSendOTP();
                    OTPIntent(fullNumber);

                   final AlertDialog.Builder addPhoneNumberDialog =new AlertDialog.Builder(getApplicationContext());
                    addPhoneNumberDialog.setTitle("Confirm Number Update");
                    addPhoneNumberDialog.setMessage("Are you sure you want to add " +fullNumber+"?");
                    addPhoneNumberDialog.setView();

                } else {
                    btnverifyPhone.setEnabled(true);
                    verifyProgress.setVisibility(View.INVISIBLE);
                    dialog.setContentView(R.layout.error_dialog);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    TextView errorInfo = dialog.findViewById(R.id.errorInfo);
                    Button ok = dialog.findViewById(R.id.ok);
                    errorInfo.setText("...hmm, something's wrong with your number.Try again.");
                    dialog.show();
                    ok.setOnClickListener(view -> dialog.dismiss());
                }
            }


            private void OTPIntent(String IntentPhoneNumber) {
                Intent i = new Intent(getApplicationContext(), VerifyOTP.class);
                i.putExtra("phone", IntentPhoneNumber);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_left,
                        R.anim.slide_out_right);

            }

            void initiateSendOTP() {
                try {
                    // Construct data
                    String apiKey = "apikey=" + "AIzaSyAKYWlQmDvMgHDCgDMN6bHx0oq6n0oZsNs";
                    phoneNumber = userPhone.getText().toString();
                    Random random = new Random();
                    randomNumber = random.nextInt(999999);
                    String message = "&message=" + "Hey " + currentUser.getDisplayName() + ", Your OTP is " + randomNumber;
                    String sender = "&sender=" + "PQFreiburg";
                    String numbers = "&numbers=" + phoneNumber;

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
                    showMessage(e.getMessage() + "\nError sending OTP");
                }
            }
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

            */

    private void showMessage(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    private void VerifyPhoneNumber(String phoneNumber) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setActivity(this)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setCallbacks(mCallBack)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            verificationId = s;
            showMessage("verification code sent");
            btnverifyPhone.setEnabled(true);
            verifyProgress.setVisibility(View.INVISIBLE);
            OTPDialog();
            code =  phoneAuthCredential.getSmsCode();

        }

        // this method is called when user
        // receive OTP from Firebase.
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

            //code = phoneAuthCredential.getSmsCode();
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            showMessage(e.getMessage());
        }
    };

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