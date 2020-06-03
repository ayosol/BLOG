package com.example.blog.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.blog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private EditText user_email, user_password;
    private ProgressBar loadingProgressBar;
    private Button btn_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        loadingProgressBar = findViewById(R.id.progressBar);
        user_email = findViewById(R.id.txt_login_mail);
        user_password = findViewById(R.id.txt_login_password);
        btn_login = findViewById(R.id.btn_login);

        loadingProgressBar.setVisibility(View.INVISIBLE);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);

                String email = user_email.getText().toString().trim();
                String password = user_password.getText().toString().trim();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
                    //Show Error message to user
                    showToastMessage("Please verify all fields correctly");
                    loadingProgressBar.setVisibility(View.INVISIBLE);
                    btn_login.setVisibility(View.VISIBLE);
                }
                else {
                    //Fields are complete and okay
                    signIn(email, password);
                }
            }
        });
    }


    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            showToastMessage("Sign In successful");
                            updateUI();
                        } else {
                            // If sign in fails, display a message to the user.
                            showToastMessage("Authentication failed \n" + task.getException().getMessage());
                        }

                    }
                });

    }

    private void updateUI() {
        Intent homeActivityIntent = new Intent(LoginActivity.this, HomeNavActivity.class);
        startActivity(homeActivityIntent);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            updateUI();
        }
    }


    //Custom Toast message Method
    private void showToastMessage(String message) {
        Toast.makeText(getApplicationContext() , message, Toast.LENGTH_LONG).show();
    }

}