package com.kofikay.pqfreiburg13;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {


    private static final int RC_SIGN_IN = 101;
    private FirebaseAuth mAuth;
    private EditText EmailEdt, PasswordEdt;
    private ProgressBar loginProgress;
    private Button loginBtn;
    GoogleSignInClient mGoogleSignInClient;
    ProgressDialog progressDialog;
    FirebaseFirestore fStore;
 //   private Switch aSwitch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Log In");

     //   aSwitch =findViewById(R.id.switch2);
        mAuth = FirebaseAuth.getInstance();
        mAuth.getCurrentUser();
        EmailEdt = findViewById(R.id.EmailEdt);
        PasswordEdt = findViewById(R.id.PasswordEdt);
        loginProgress = findViewById(R.id.loginProgress);
        loginBtn = findViewById(R.id.loginBtn);
        fStore = FirebaseFirestore.getInstance();
        Button signUpBtn = findViewById(R.id.signUpBtn);
        ImageButton googleBtn = findViewById(R.id.googleBtn);
        ImageButton fbBtn = findViewById(R.id.FbBtn);
        ImageButton twitterBtn = findViewById(R.id.twitterBtn);
        TextView forgotPassword = findViewById(R.id.forgotPassword);



        signUpBtn.setOnClickListener(this::gotoSignUpPage);
        loginProgress.setVisibility(View.INVISIBLE);
        loginBtn.setOnClickListener(v -> {

            loginProgress.setVisibility(View.VISIBLE);
            loginBtn.setEnabled(false);
            loginBtn.setText("");
            final String userEmail = EmailEdt.getText().toString().trim();
            final String userPassword = PasswordEdt.getText().toString().trim();

            //checking for empty fields
            if (userEmail.isEmpty() ) {
                EmailEdt.requestFocus();
                loginBtn.setEnabled(true);
                loginBtn.setText("Log in");
                loginProgress.setVisibility(View.INVISIBLE);
            }else if (userPassword.isEmpty()) {
                PasswordEdt.requestFocus();
                loginBtn.setEnabled(true);
                loginBtn.setText("Log in");
                loginProgress.setVisibility(View.INVISIBLE);
            }else {signIn(userEmail, userPassword);}

        });
        forgotPassword.setOnClickListener(this::gotoResetPassword);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("108128146448-qbba4j4r5koc95p6vpekk93spmhiic4l.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if(googleSignInAccount !=null || mAuth.getCurrentUser() !=null){
            showMessage("logged in!");
            updateUI();
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading your accounts ...");
        progressDialog.setTitle("Google Sign In");
        googleBtn.setOnClickListener(view -> {
            progressDialog.show();
            signInWithGoogle();
        });
        fbBtn.setOnClickListener(v -> showMessage("Login with Google for now or use email and password"));
        twitterBtn.setOnClickListener(v -> showMessage("Login with Google for now or use email and password"));

    }

    private void gotoResetPassword(View view) {
        Intent intent = new Intent(getApplicationContext(), ResetPassword.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right,
                R.anim.slide_out_left);
    }

    private void signIn(String userEmail, String userPassword) {
        mAuth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        loginProgress.setVisibility(View.INVISIBLE);
                        loginBtn.setEnabled(true);
                        loginBtn.setText("Log in");
                        updateUI();
                    } else {
                        // If sign in fails, display a message to the user.
                        loginBtn.setEnabled(true);
                        loginBtn.setText("Log in");
                        loginProgress.setVisibility(View.INVISIBLE);
                        showMessage(task.getException().getMessage());
                    }
                });
    }
    public void signInWithGoogle(){
        mGoogleSignInClient.signOut();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        progressDialog.setMessage("Signing you in...");
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);

                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());

            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately

                Log.w(TAG, "Google sign in failed", e);
                Log.d(TAG, "Failure: " +e.getMessage());
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
                showMessage(e.getMessage());
                progressDialog.dismiss();
                mGoogleSignInClient.signOut ();
                mAuth.signOut ();
            }
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        progressDialog.setMessage("Signing you in...");
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        progressDialog.dismiss();
                        updateUI();
                    }
                    else {
                        // If sign in fails, display a message to the user.
                        progressDialog.dismiss();
                        showMessage(Objects.requireNonNull(task.getException()).getMessage());
                        Log.d(TAG, task.getException().getMessage());
                        // updateUI(null);
                        mGoogleSignInClient.signOut ();
                        mAuth.signOut ();
                        onVisibleBehindCanceled();
                    }
                });
    }


    private void updateUI() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right,
                R.anim.slide_out_left);
        finish();
    }
    private void showMessage(String text) {
        Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if(currentUser != null || googleSignInAccount !=null){
            updateUI();
        }
    }

    public void gotoSignUpPage(View view) {
        Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right,
                R.anim.slide_out_left);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        MenuItem itemswitch = menu.findItem(R.id.switch_action_bar);
        itemswitch.setActionView(R.layout.use_switch);
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        final SharedPreferences.Editor editor= sharedPreferences.edit();
        final boolean isDarkModeOn = sharedPreferences.getBoolean("isDarkModeOn", false);
        final Switch sw = menu.findItem(R.id.switch_action_bar).getActionView().findViewById(R.id.switch2);

        if (isDarkModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            sw.setChecked(true);
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            sw.setChecked(false);
        }

        sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                // if dark mode is on it will turn it off
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                editor.putBoolean("isDarkModeOn", false);
                editor.apply();
                sw.setChecked(false);

            }else{
                // if dark mode is off it will turn it on
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                editor.putBoolean("isDarkModeOn", true);
                editor.apply();
                sw.setChecked(true);
            }
        });

        return true;
    }

}