package com.kofikay.pqfreiburg13;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpActivity extends AppCompatActivity {


    CircleImageView  userPhoto;
    ImageButton addImageBtn;
    private static final int CAMERA_REQUEST = 100;
    private static final int STORAGE_REQUEST = 200;
    private Uri pickedImgUri;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" + "(?=.*[0-9])" +//at least number
                    "(?=.*[a-z])(?=.*[A-Z])" + //at least 1 lower and upper case letter
                    "(?=.*[@#$%^&+=])" +// at least 1 special character
                    "(?=\\S+$)" +            // no white spaces
                    ".{4,}" +                // at least 4 characters
                    "$");

    private FirebaseAuth mFirebaseAuth;
    FirebaseFirestore fStore;
    private EditText EmailEdt, PasswordEdt, UsernameEdt, ConfirmPasswordEdt;
    private Button signUpButton;
    private ProgressBar signUpProgress;
    private StorageReference storageReference;
    private Dialog dialog;
    private String userID;
    private FirebaseUser firebaseUser;
    String[] cameraPermission;
    String[] storagePermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_layout);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Sign Up");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mFirebaseAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        firebaseUser = mFirebaseAuth.getCurrentUser();
        EmailEdt = findViewById(R.id.EmailEdt);
        PasswordEdt = findViewById(R.id.PasswordEdt);
        ConfirmPasswordEdt = findViewById(R.id.ConfirmPasswordEdt);
        UsernameEdt = findViewById(R.id.UsernameEdt);
        signUpButton = findViewById(R.id.signUpButton);
        TextView termsAndConditions = findViewById(R.id.termsAndConditions);
        signUpProgress = findViewById(R.id.signUpProgress);
        dialog = new Dialog(this, R.style.DialogAnimation);
        addImageBtn = findViewById(R.id.addImageBtn);
        userPhoto = findViewById(R.id.regUserPhoto);

        termsAndConditions.setOnClickListener(this::openTermsAndConditions);

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        setImage(userPhoto);

        signUpProgress.setVisibility(View.INVISIBLE);
        signUpButton.setOnClickListener(view -> {
            signUpButton.setEnabled(false);
            signUpProgress.setVisibility(View.VISIBLE);

            final String userName = UsernameEdt.getText().toString().trim();
            final String userEmail = EmailEdt.getText().toString().trim();
            final String userPassword = PasswordEdt.getText().toString().trim();
            final String confirmUserPassword = ConfirmPasswordEdt.getText().toString().trim();

            //checking for empty fields
            if (userName.isEmpty()) {
                UsernameEdt.requestFocus();
                signUpButton.setEnabled(true);
                signUpProgress.setVisibility(View.INVISIBLE);
            } else if (userEmail.isEmpty() || !(Patterns.EMAIL_ADDRESS.matcher(userEmail).matches())) {
                signUpButton.setEnabled(true);
                signUpProgress.setVisibility(View.INVISIBLE);
                EmailEdt.requestFocus();
            } else if (!PASSWORD_PATTERN.matcher(userPassword).matches()) {
                showMessage("Password is too weak");
                PasswordEdt.requestFocus();
                signUpButton.setEnabled(true);
                signUpProgress.setVisibility(View.INVISIBLE);
            } else if (userPassword.isEmpty()) {
                signUpButton.setEnabled(true);
                signUpProgress.setVisibility(View.INVISIBLE);
                PasswordEdt.requestFocus();
            } else if (!(confirmUserPassword.equals(userPassword))) {
                signUpButton.setEnabled(true);
                signUpProgress.setVisibility(View.INVISIBLE);
                ConfirmPasswordEdt.requestFocus();
                showMessage("passwords do not match");
            } else {
                CreateUserAccount(userName, userEmail, userPassword);
            }
        });

        userPhoto.setOnClickListener(v -> showImagePicDialog());
        addImageBtn.setOnClickListener(v -> showImagePicDialog());

    }

    private void CreateUserAccount(String userName, final String userEmail, final String userPassword) {

        mFirebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(SignUpActivity.this, task -> {
                    if (task.isSuccessful()) {
                        userID = Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).getUid();
                        DocumentReference documentReference = fStore.collection("User Profile").document(userID);
                        Map<String, Object> user = new HashMap<>();
                        user.put("User Name", userName);
                        user.put("Email", userEmail);
                        user.put("Password", userPassword);
                        user.put("Contact", null);
                        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                        DatabaseReference databaseReference = firebaseDatabase.getReference("User Profile");
                        databaseReference.child(userID).setValue(user);
                        documentReference.set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "onSuccess: user Profile is created for " + userID);
                                })
                                .addOnFailureListener(e -> Log.d(TAG, "onFailure: " + e.toString()));

                        if (pickedImgUri != null) {

                            updateUserInfo(userName, pickedImgUri, mFirebaseAuth.getCurrentUser());
                        } else {
                            updateUserInfoWithoutPhoto(userName, Objects.requireNonNull(mFirebaseAuth.getCurrentUser()));
                        }

                    } else {
                        showMessage("oops!,...something is wrong. Try again");
                        signUpButton.setEnabled(true);
                        signUpProgress.setVisibility(View.INVISIBLE);
                    }
                }).addOnFailureListener(e -> {
            showMessage(e.getMessage());
            Log.d(TAG, "Failure: " + e.getMessage());
            signUpButton.setEnabled(true);
            signUpProgress.setVisibility(View.INVISIBLE);
        });


    }

    //show image pick dialog

    private void showImagePicDialog() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image From");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                if (!checkCameraPermission()) {
                    requestCameraPermission();
                } else {
                    pickFromGallery();
                }
            } else if (which == 1) {
                if (!checkStoragePermission()) {
                    requestStoragePermission();
                } else {
                    pickFromGallery();
                }
            }
        });
        builder.create().show();
    }

    // checking storage permissions
    private Boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
    }
    // Requesting  gallery permission
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(storagePermission, STORAGE_REQUEST);
        }
    }

    // checking camera permissions
    private Boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    // Requesting camera permission
    private void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(cameraPermission, CAMERA_REQUEST);
        }
    }
    // Requesting camera and gallery
    // permission if not given
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST: {
                if (grantResults.length > 0) {
                    boolean camera_accepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageaccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (camera_accepted && writeStorageaccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Please Enable Camera and Storage Permissions", Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST: {
                if (grantResults.length > 0) {
                    boolean writeStorageaccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageaccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Please Enable Storage Permissions", Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
        }
    }

    // Here we will pick image from gallery or camera
    private void pickFromGallery() {
        CropImage.activity().start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri resultUri = result.getUri();
                Picasso.get().load(resultUri).into(userPhoto);
                //pickedImgUri = data.getData();
                pickedImgUri = resultUri;
                userPhoto.setImageURI(resultUri);
                //upLoadImageToFStore(resultUri);
            }
        }
    }

    private void openTermsAndConditions(View view) {
        dialog.setContentView(R.layout.t_and_c);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        Button okayBtn = dialog.findViewById(R.id.okayBtn);
        okayBtn.setOnClickListener(view1 -> dialog.dismiss());

    }

    private void upLoadImageToFStore(Uri imageUri) {
        storageReference = FirebaseStorage.getInstance().getReference().child("user photos");
        final StorageReference fileRef = storageReference.child(mFirebaseAuth.getCurrentUser().getEmail() + "/profile.jpg" /*+ imageUri.getLastPathSegment()*/);
        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    Picasso.get().load(uri).into(userPhoto);
                    showMessage("...all set up and fresh!");
                }).addOnFailureListener(e -> showMessage(e.getMessage()))).addOnFailureListener(e -> showMessage(e.getMessage()));

    }

    private void updateUserInfo(String userName, Uri pickedImgUri, FirebaseUser currentUser) {

        upLoadImageToFStore(pickedImgUri);

        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                .setDisplayName(userName)
                .setPhotoUri(pickedImgUri)
                .build();

        currentUser.updateProfile(profileUpdate)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                     //   showMessage("...all set up and fresh!");
                        updateUI();
                    }
                });
    }

    private void updateUserInfoWithoutPhoto(String userName, FirebaseUser currentUser) {
        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                .setDisplayName(userName)
                .build();

        currentUser.updateProfile(profileUpdate)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // user info updated successfully
                        showMessage("Register Complete");
                        updateUI();
                    }
                });
    }

    private void updateUI() {
        loginActivity();
    }

    private void showMessage(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    private void loginActivity() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right,
                R.anim.slide_out_left);
        finish();
    }
    public void setImage(CircleImageView userImage){
        Drawable color = new ColorDrawable(getResources().getColor(R.color.grey));
        Drawable image = getResources().getDrawable(R.drawable.user);
        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{color, image});
        Glide.with(this).load(layerDrawable).into(userImage);
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