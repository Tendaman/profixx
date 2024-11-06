package com.example.profixx.Users;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.profixx.Activity.BaseActivity;
import com.example.profixx.Activity.DashboardActivity;
import com.example.profixx.databinding.ActivityBusinessLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class BusinessLoginActivity extends BaseActivity {
    FirebaseAuth mAuth;
    ActivityBusinessLoginBinding binding;

    @Override
    public void onStart(){
        super.onStart();
        // Ensure mAuth is initialized before calling getCurrentUser()
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBusinessLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnSignIn.setOnClickListener(v -> {
            binding.progressBar.setVisibility(View.VISIBLE);
            String businessEmailTxt = String.valueOf(binding.businessEmail.getText());
            String businessPasswordTxt = String.valueOf(binding.passwordBusiness.getText());

            if (validateInputs(businessEmailTxt, businessPasswordTxt)) {
                mAuth.signInWithEmailAndPassword(businessEmailTxt, businessPasswordTxt)
                        .addOnCompleteListener(task -> {
                            binding.progressBar.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Authentication successful.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        binding.txtSignup.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), BusinessRegisterActivity.class));
            finish();
        });

        binding.btnUser.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), SigninActivity.class));
            finish();
        });
    }
    private boolean validateInputs(String businessEmailTxt, String businessPasswordTxt) {
        if (businessEmailTxt.isEmpty()) {
            binding.businessEmail.setError("Please enter business email");
            binding.businessEmail.requestFocus();
            return false;
        } else if (businessPasswordTxt.isEmpty()) {
            binding.passwordBusiness.setError("Please enter business password");
            binding.passwordBusiness.requestFocus();
            return false;
        }
        return true;
    }
}


