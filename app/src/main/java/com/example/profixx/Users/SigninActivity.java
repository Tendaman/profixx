package com.example.profixx.Users;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;

import com.example.profixx.Activity.BaseActivity;
import com.example.profixx.Activity.MainActivity;
import com.example.profixx.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SigninActivity extends BaseActivity {
    TextInputEditText email, password;
    Button login;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView signup;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(SigninActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        mAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.emailtxt);
        password = findViewById(R.id.password_txt);
        login = findViewById(R.id.btnSignIn);
        progressBar = findViewById(R.id.progressBar);
        signup = findViewById(R.id.txtSignup);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String emailtxt = String.valueOf(email.getText());
                String passwordtxt = String.valueOf(password.getText());
                if (TextUtils.isEmpty(emailtxt)) {
                    Toast.makeText(SigninActivity.this, "Enter Email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(passwordtxt)) {
                    Toast.makeText(SigninActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(emailtxt) && TextUtils.isEmpty(passwordtxt)) {
                    Toast.makeText(SigninActivity.this, "Fill in your details", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(emailtxt, passwordtxt)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(SigninActivity.this, "Authentication successful.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(SigninActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(SigninActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SigninActivity.this, SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}