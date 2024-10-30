package com.example.profixx.Users;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import com.example.profixx.Activity.BaseActivity;
import com.example.profixx.Activity.Dashboard;
import com.example.profixx.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends BaseActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);
    }

    private void setupClickListeners() {
        // Login button click
        binding.loginButton.setOnClickListener(v -> validateAndLogin());

        // Register prompt click
        binding.registerPromptTextView.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // Forgot password click
        binding.forgotPasswordTextView.setOnClickListener(v -> handleForgotPassword());
    }

    private void validateAndLogin() {
        String email = binding.emailEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();

        // Validation
        if (email.isEmpty()) {
            binding.emailLayout.setError("Email is required");
            binding.emailEditText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.setError("Please enter a valid email");
            binding.emailEditText.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            binding.passwordLayout.setError("Password is required");
            binding.passwordEditText.requestFocus();
            return;
        }

        // Clear any previous errors
        binding.emailLayout.setError(null);
        binding.passwordLayout.setError(null);

        progressDialog.show();
        loginSeller(email, password);
    }

    private void loginSeller(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Get current user ID
                        String sellerId = mAuth.getCurrentUser().getUid();
                        // Verify seller exists in database
                        verifySellerAndProceed(sellerId);
                    } else {
                        progressDialog.dismiss();
                        // Handle specific authentication errors
                        handleAuthError(task.getException());
                    }
                });
    }

    private void verifySellerAndProceed(String sellerId) {
        DatabaseReference sellerRef = database.getReference("sellers").child(sellerId);
        sellerRef.get().addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    // Save seller ID to SharedPreferences for future use
                    saveSellerSession(sellerId);

                    // Navigate to seller dashboard
                    Intent intent = new Intent(LoginActivity.this, Dashboard.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    // User exists in Auth but not in database
                    mAuth.signOut();
                    Toast.makeText(LoginActivity.this,
                            "No seller account found. Please register first.",
                            Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(LoginActivity.this,
                        "Login failed. Please try again.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleAuthError(Exception exception) {
        String message = "Login failed. Please try again.";

        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            message = "Invalid email or password";
        } else if (exception instanceof FirebaseAuthInvalidUserException) {
            message = "No account found with this email";
        }

        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
    }

    private void handleForgotPassword() {
        String email = binding.emailEditText.getText().toString().trim();

        if (email.isEmpty()) {
            binding.emailLayout.setError("Enter your email first");
            binding.emailEditText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.setError("Please enter a valid email");
            binding.emailEditText.requestFocus();
            return;
        }

        progressDialog.setMessage("Sending reset email...");
        progressDialog.show();

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this,
                                "Password reset email sent",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Failed to send reset email. Please try again.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveSellerSession(String sellerId) {
        SharedPreferences prefs = getSharedPreferences("SellerPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("sellerId", sellerId);
        editor.putBoolean("isLoggedIn", true);
        editor.apply();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already logged in
        if (mAuth.getCurrentUser() != null) {
            verifySellerAndProceed(mAuth.getCurrentUser().getUid());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        binding = null;
    }
}