package com.kofikay.pqfreiburg13;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class profile_activity extends AppCompatActivity {


    private FirebaseUser currentUser;
    private CircleImageView userPhoto;
    private StorageReference storageReference;
    private FirebaseAuth mFirebaseAuth;
    FirebaseFirestore fStore;
    private String userID;
    private static final int CAMERA_REQUEST = 100;
    private static final int STORAGE_REQUEST = 200;
    Uri pickedImgUri;
    private ProgressBar ImageUpLoadProgress;
    private static String Imageuri;
    String[] cameraPermission;
    String[] storagePermission;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_layout);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Account settings");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mFirebaseAuth = FirebaseAuth.getInstance();
        currentUser = mFirebaseAuth.getCurrentUser();
        userPhoto = findViewById(R.id.Photo);
        storageReference = FirebaseStorage.getInstance().getReference();
        ImageView photo = findViewById(R.id.Photo);
        ImageUpLoadProgress = findViewById(R.id.ImageUpLoadProgress);
        ImageUpLoadProgress.setVisibility(INVISIBLE);
        fStore = FirebaseFirestore.getInstance();

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        userData();
        photo.setOnClickListener(view -> showBottomSheetDialog());
        TextView usertxt = findViewById(R.id.usertxt);
        TextView numbertxt = findViewById(R.id.numbertxt);
        TextView emailtxt2 = findViewById(R.id.emailtxt2);
        TextView passwordtxt = findViewById(R.id.passwordtxt);

        usertxt.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ChangeUserName.class);
            startActivity(intent);
        });
        numbertxt.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), AddorChangePhone.class);
            startActivity(intent);
        });
        emailtxt2.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), EmailChange.class);
            startActivity(intent);
        });
        passwordtxt.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), PasswordChange.class);
            startActivity(intent);
        });

    }

    @SuppressLint("SetTextI18n")
    public void userData() {
        TextView emailtxt = findViewById(R.id.emailtxt);
        TextView userContact = findViewById(R.id.userContact);
        TextView Usernametxt = findViewById(R.id.Usernametxt);
        emailtxt.setText(currentUser.getEmail());
        Usernametxt.setText(currentUser.getDisplayName());
        if (currentUser.getPhotoUrl() != null) {
            Glide.with(this).load(currentUser.getPhotoUrl()).into(userPhoto);
        } else {
            setImage(userPhoto);
        }


        if (currentUser != null) {
            DocumentReference documentReference = fStore.collection("User Profile")
                    .document(currentUser.getUid());
            //check for last name availability
            documentReference.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.get("Contact") != null) {
                        String contact = document.getString("Contact");
                        userContact.setText("+233" + contact);
                    } else {
                        userContact.setText("Mobile number");
                    }
                }
            });
        }

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
                        showMessage("Please Enable Camera and Storage Permissions");
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
                        showMessage("Please Enable Camera and Storage Permissions");
                    }
                }
            }
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == Activity.RESULT_OK && data != null) {
                ImageUpLoadProgress.setVisibility(VISIBLE);
                Uri resultUri = result.getUri();
                Picasso.get().load(resultUri).into(userPhoto);
                pickedImgUri = data.getData();
                userPhoto.setImageURI(resultUri);
                upLoadImageToFStore(resultUri);
                Imageuri = String.valueOf(resultUri);
            }
        }
        if(requestCode==777){
            if(resultCode == Activity.RESULT_OK ) {
                String updatedName = data.getStringExtra("updatedName");
                TextView Usernametxt = findViewById(R.id.Usernametxt);
                Usernametxt.setText(updatedName);
            }
        }
    }

    private void upLoadImageToFStore(Uri imageUri) {
        final StorageReference fileRef = storageReference.child("user photos/" + currentUser.getEmail() + "/profile.jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Picasso.get().load(uri).into(userPhoto);
            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(uri)
                    .build();
            currentUser.updateProfile(profileUpdate)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            ImageUpLoadProgress.setVisibility(INVISIBLE);
                            showMessage("Image saved");
                        }
                    }).addOnFailureListener(e -> {
                ImageUpLoadProgress.setVisibility(INVISIBLE);
                showMessage(e.getMessage());

            });
        }).addOnFailureListener(e -> {
            ImageUpLoadProgress.setVisibility(INVISIBLE);
            showMessage(e.getMessage());
        })).addOnFailureListener(e -> {
            ImageUpLoadProgress.setVisibility(INVISIBLE);
            showMessage(e.getMessage());
        });
    }

    private void pickFromGallery() {
        CropImage.activity().start(this);
    }

    private void showMessage(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void showBottomSheetDialog() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.btmsheet2);
        bottomSheetDialog.setCanceledOnTouchOutside(true);
        TextView changeProfile = bottomSheetDialog.findViewById(R.id.changeProfile);
        TextView removeProfile = bottomSheetDialog.findViewById(R.id.removeProfilePhoto);
        TextView cancelProfileImageChange = bottomSheetDialog.findViewById(R.id.cancelImageChange);

        if (currentUser.getPhotoUrl() == null) {
            changeProfile.setText("Add profile photo");
        }
        changeProfile.setOnClickListener(view -> {
            showImagePicDialog();

            bottomSheetDialog.dismiss();
        });
        removeProfile.setOnClickListener(view -> {
            removeProfileImage();
            bottomSheetDialog.dismiss();
        });

        cancelProfileImageChange.setOnClickListener(view -> bottomSheetDialog.dismiss());

        bottomSheetDialog.setOnDismissListener(dialog -> {
            // Instructions on bottomSheetDialog Dismiss
            bottomSheetDialog.dismiss();
        });
        bottomSheetDialog.show();
    }

    private void removeProfileImage() {

        final StorageReference fileRef = storageReference.child("user photos/" + currentUser.getEmail() + "/profile.jpg");
        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            if (Objects.equals(fileRef, null)) {
                showMessage("Nothing to remove");
            } else {
                setImage(userPhoto);
                fileRef.delete().addOnSuccessListener(aVoid -> {
                    UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                            .setPhotoUri(null).build();
                    Imageuri=null;
                    currentUser.updateProfile(profileUpdate).addOnSuccessListener(unused -> {
                        ImageUpLoadProgress.setVisibility(INVISIBLE);
                        showMessage("image removed successfully");
                        Log.e("Picture", "#deleted");
                    });
                }).addOnFailureListener(e -> {
                            ImageUpLoadProgress.setVisibility(INVISIBLE);
                            showMessage("Uh-oh, an error occurred!\n" + e.getMessage());
                        }
                );
            }
        });
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
    protected void onResume() {
        super.onResume();
        userData();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}