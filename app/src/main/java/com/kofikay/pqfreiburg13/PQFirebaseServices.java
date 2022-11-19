package com.kofikay.pqfreiburg13;

import android.content.Context;
import android.widget.Toast;

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

public class PQFirebaseServices {
    Context context;
    FirebaseFirestore fStore =FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser = firebaseAuth.getCurrentUser();

    public interface PQFbListener{
        void onSaved(String Data);
        void onError(String error);

    }

    public void SaveToCollection(String field, String fieldData){

        if (currentUser!=null) {
            for (UserInfo userInfo : FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
                String userID = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
                DocumentReference documentReference = fStore.collection("User Profile").document(userID);

                if (userInfo.getProviderId().equals("google.com")) {
                    documentReference.get().addOnCompleteListener(task -> {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {

                        } else {
                            Map<String, Object> user = new HashMap<>();
                            user.put("User Name", null);
                            user.put("Last name", null);
                            user.put("Contact", null);
                            user.put("Email", firebaseAuth.getCurrentUser().getEmail());
                            fStore.collection("User Profile").document(userID).set(user);
                        }
                    });
                    if (field.equals("User Name")) {
                        fStore.collection("User Profile")
                                .document(currentUser.getUid())
                                .update(field, fieldData).addOnSuccessListener(unused -> {
                            UserProfileChangeRequest profileUpdate;
                            profileUpdate = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(fieldData).build();
                            currentUser.updateProfile(profileUpdate)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(context, "Save success", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(e -> {
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }).addOnFailureListener(e -> {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        fStore.collection("User Profile")
                                .document(currentUser.getUid())
                                .update(field, fieldData)
                                .addOnSuccessListener(unused -> Toast.makeText(context, "Save success", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                } else {
                    if (field.equals("User Name")) {
                        fStore.collection("User Profile")
                                .document(currentUser.getUid())
                                .update(field, fieldData).addOnSuccessListener(unused -> {
                            UserProfileChangeRequest profileUpdate;
                            profileUpdate = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(fieldData).build();
                            currentUser.updateProfile(profileUpdate)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(context, "Save success", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(e -> {
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }).addOnFailureListener(e -> {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        });

                    } else {
                        fStore.collection("User Profile")
                                .document(currentUser.getUid())
                                .update(field, fieldData)
                                .addOnSuccessListener(unused -> Toast.makeText(context, "Save success", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }
            }
        }
    }
    public String ReadFromCollection(String field){
        final String[] readData = {null};
        if (currentUser!=null) {
            for (UserInfo userInfo : currentUser.getProviderData()) {
                String userID = Objects.requireNonNull(currentUser.getUid());
                DocumentReference documentReference = fStore.collection("User Profile")
                        .document(userID);
                if (userInfo.getProviderId().equals("google.com")) {
                    //check for last name availability
                    if (field.equals("User Name")) {
                        documentReference.get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.get("User Name") == null) {
                                    String name[] = currentUser.getDisplayName().split("\\s+");
                                    readData[0] = name[0];
                                } else {
                                    readData[0] = currentUser.getDisplayName();
                                }
                            }
                        });
                    } else {
                        documentReference.get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.get(field) != null) {
                                    readData[0] = document.getString(field);
                                }
                            }
                        });
                    }


                } else {
                    if (field.equals("User Name")) {
                        readData[0] = currentUser.getDisplayName();
                    } else {
                        documentReference.get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.get(field) != null) {
                                    readData[0] = document.getString(field);
                                }
                            }
                        });
                    }
                }
            }
        }
        return readData[0];
    }
    public  void SaveToStorage(){}
    public void ReadFromStorage(){}
}
