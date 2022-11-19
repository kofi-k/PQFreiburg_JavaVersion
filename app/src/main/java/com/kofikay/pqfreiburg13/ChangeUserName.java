package com.kofikay.pqfreiburg13;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChangeUserName extends AppCompatActivity {

    private EditText firstName, lastname;
    private FirebaseUser currentUser;
    private ProgressBar changesProgress;
    private Button saveChanges;
    FirebaseFirestore fStore;
    private FirebaseAuth firebaseAuth;

    //private static final String USER_NAME_FIELD  = "User Name";
    //private static final String LAST_NAME_FIELD  ="Last name";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.username_change);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("User Name");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //PQFirebaseServices pqFirebaseServices = new PQFirebaseServices();

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        fStore = FirebaseFirestore.getInstance();
        firstName = findViewById(R.id.firstName);
        lastname = findViewById(R.id.lastname);
        saveChanges = findViewById(R.id.saveChanges);
        changesProgress = findViewById(R.id.changesProgress);
        changesProgress.setVisibility(View.INVISIBLE);

        //firstName.setText(currentUser.getDisplayName());
        LoadUserNameInfo();

        saveChanges.setOnClickListener(view -> {
            changesProgress.setVisibility(View.VISIBLE);
            saveChanges.setEnabled(false);

            final String userFirstName = firstName.getText().toString();
            final String userLastName = lastname.getText().toString();

            if (userFirstName.isEmpty() && userLastName.isEmpty()) {
                saveChanges.setEnabled(true);
                changesProgress.setVisibility(View.INVISIBLE);
                firstName.requestFocus();
                showMessage("fields can not be empty");
            } else {
               saveChangesToCloud(userFirstName, userLastName);
            }
        });
    }

    private void LoadUserNameInfo() {
        //firstName.setText(currentUser.getDisplayName());
        if (currentUser!=null) {
            for (UserInfo userInfo : currentUser.getProviderData()) {
                String userID = Objects.requireNonNull(currentUser.getUid());
                if (userInfo.getProviderId().equals("google.com")) {
                    DocumentReference documentReference = fStore.collection("User Profile")
                            .document(userID);
                    //check for last name availability
                    documentReference.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.get("User Name") == null) {
                                String[] name = Objects.requireNonNull(currentUser.getDisplayName()).split("\\s+");
                                firstName.setText(name[0]);
                            } else {
                                firstName.setText(currentUser.getDisplayName());
                            }
                            if (document.get("Last name") != null) {
                                String lName = document.getString("Last name");
                                lastname.setText(lName);
                            }
                        }
                    });

                } else {
                    firstName.setText(currentUser.getDisplayName());
                    DocumentReference documentReference = fStore.collection("User Profile")
                            .document(userID);
                    //check for last name availability
                    documentReference.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.get("Last name") != null) {
                                String lName = document.getString("Last name");
                                lastname.setText(lName);
                            }
                        }
                    });
                }
            }
        }

    }

    private void saveChangesToCloud(String userFName, String userLName) {
        if (currentUser!=null) {
            for (UserInfo userInfo : currentUser.getProviderData()) {
                String userID = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
                if (userInfo.getProviderId().equals("google.com")) {
                    DocumentReference documentReference = fStore.collection("User Profile").document(userID);
                    documentReference.get().addOnCompleteListener(task -> {
                        DocumentSnapshot document = task.getResult();
                        if (!document.exists()) {

                            Map<String, Object> user = new HashMap<>();
                            user.put("User Name", userFName);
                            user.put("Last name", userLName);
                            user.put("Email", firebaseAuth.getCurrentUser().getEmail());
                            fStore.collection("User Profile").document(userID).set(user);

                        }
                    });
                    fStore.collection("User Profile")
                            .document(currentUser.getUid())
                            .update("User Name", userFName,
                                    "Last name", userLName).addOnSuccessListener(unused -> {
                        UserProfileChangeRequest profileUpdate;
                        if (userFName.isEmpty() && !userLName.isEmpty()) {

                            profileUpdate = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(userLName).build();
                            currentUser.updateProfile(profileUpdate)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            showMessage("Changes saved.");
                                            saveChanges.setEnabled(true);
                                            changesProgress.setVisibility(View.INVISIBLE);
                                            finish();
                                            overridePendingTransition(R.anim.slide_in_left,
                                                    R.anim.slide_out_right);
                                        }
                                    }).addOnFailureListener(e -> {
                                saveChanges.setEnabled(true);
                                changesProgress.setVisibility(View.INVISIBLE);
                                showMessage(e.getMessage());
                            });
                        } else {
                            profileUpdate = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(userFName).build();
                            currentUser.updateProfile(profileUpdate)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            showMessage("Changes saved.");
                                            saveChanges.setEnabled(true);
                                            changesProgress.setVisibility(View.INVISIBLE);
                                            finish();
                                            overridePendingTransition(R.anim.slide_in_left,
                                                    R.anim.slide_out_right);
                                        }
                                    }).addOnFailureListener(e -> {
                                saveChanges.setEnabled(true);
                                changesProgress.setVisibility(View.INVISIBLE);
                                showMessage(e.getMessage());
                            });
                        }
                    }).addOnFailureListener(e -> {
                        showMessage(e.getMessage());
                        saveChanges.setEnabled(true);
                        changesProgress.setVisibility(View.INVISIBLE);
                    });
                } else {

                    fStore.collection("User Profile")
                            .document(currentUser.getUid())
                            .update("User Name", userFName,
                                    "Last name", userLName).addOnSuccessListener(unused -> {
                        UserProfileChangeRequest profileUpdate;
                        if (userFName.isEmpty() && !userLName.isEmpty()) {

                            profileUpdate = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(userLName).build();
                            currentUser.updateProfile(profileUpdate)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            showMessage("Changes saved.");
                                            saveChanges.setEnabled(true);
                                            changesProgress.setVisibility(View.INVISIBLE);
                                            finish();
                                            overridePendingTransition(R.anim.slide_in_left,
                                                    R.anim.slide_out_right);
                                        }
                                    }).addOnFailureListener(e -> {
                                saveChanges.setEnabled(true);
                                changesProgress.setVisibility(View.INVISIBLE);
                                showMessage(e.getMessage());
                            });
                        } else {
                            profileUpdate = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(userFName).build();
                            currentUser.updateProfile(profileUpdate)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            showMessage("Changes saved.");
                                            saveChanges.setEnabled(true);
                                            changesProgress.setVisibility(View.INVISIBLE);
                                            finish();
                                            overridePendingTransition(R.anim.slide_in_left,
                                                    R.anim.slide_out_right);
                                        }
                                    }).addOnFailureListener(e -> {
                                saveChanges.setEnabled(true);
                                changesProgress.setVisibility(View.INVISIBLE);
                                showMessage(e.getMessage());
                            });
                        }
                    }).addOnFailureListener(e -> {
                        showMessage(e.getMessage());
                        saveChanges.setEnabled(true);
                        changesProgress.setVisibility(View.INVISIBLE);
                    }).addOnFailureListener(e -> {
                        saveChanges.setEnabled(true);
                        changesProgress.setVisibility(View.INVISIBLE);
                        showMessage(e.getMessage());
                    });
                }
            }
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