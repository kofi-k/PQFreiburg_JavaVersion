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

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HelpSettings extends AppCompatActivity {

    private Dialog dialog;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_settings);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Help");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        dialog = new Dialog(this, R.style.DialogAnimation);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        TextView licenses = findViewById(R.id.licenses);
        TextView helpCent = findViewById(R.id.helpCent);
        TextView reportIssue = findViewById(R.id.reportIssue);
        reportIssue.setOnClickListener(v -> {
            dialog.setContentView(R.layout.report_issue);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            EditText Reportmail = dialog.findViewById(R.id.Reportmail);
            EditText reportSubject = dialog.findViewById(R.id.reportSubject);
            EditText reportBody = dialog.findViewById(R.id.reportBody);
            Reportmail.setText(currentUser.getEmail());
            Button sendReport = dialog.findViewById(R.id.sendReport);
            TextView cancelReport = dialog.findViewById(R.id.cancelReport);
            ProgressBar reportProgress = dialog.findViewById(R.id.reportProgress);
            reportProgress.setVisibility(View.INVISIBLE);
            dialog.show();
            cancelReport.setOnClickListener(view -> dialog.dismiss());
            sendReport.setOnClickListener(v1 -> {
                reportProgress.setVisibility(View.VISIBLE);
                sendReport.setVisibility(View.INVISIBLE);
                final String repMail = Reportmail.getText().toString().trim();
                final String repBody = reportBody.getText().toString().trim();
                final String repSubject = reportSubject.getText().toString().trim();

                if(repMail.isEmpty()){
                    Reportmail.requestFocus();
                    reportProgress.setVisibility(View.INVISIBLE);
                    sendReport.setVisibility(View.VISIBLE);
                }else if(repBody.isEmpty()){
                    reportProgress.setVisibility(View.INVISIBLE);
                    sendReport.setVisibility(View.VISIBLE);
                    reportBody.requestFocus();
                }else{
                    reportProgress.setVisibility(View.VISIBLE);
                    sendReport.setVisibility(View.INVISIBLE);
                    if(repSubject.isEmpty()){
                        reportSubject.setText(currentUser.getEmail()+"'s ISSUE REPORT");
                    }else{
                        Toast.makeText(this, "Works fine!", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                }
            });

        });
        licenses.setOnClickListener(v -> {
                dialog.setContentView(R.layout.licenses);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
                Button okayBtn = dialog.findViewById(R.id.licensesBtn);
                okayBtn.setOnClickListener(view1 -> dialog.dismiss());
        });
        helpCent.setOnClickListener(v -> {
        });

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