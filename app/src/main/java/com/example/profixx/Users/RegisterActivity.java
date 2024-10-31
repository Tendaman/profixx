package com.example.profixx.Users;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.profixx.Activity.BaseActivity;
import com.example.profixx.Activity.Dashboard;
import com.example.profixx.Model.Seller;
import com.example.profixx.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class RegisterActivity extends BaseActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private boolean isImageSelected = false;

    // View Binding
    private ActivityRegisterBinding binding;
    private ProgressDialog progressDialog;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering seller...");
        progressDialog.setCancelable(false);
    }

    private void setupClickListeners() {
        binding.uploadLogoButton.setOnClickListener(v -> openImagePicker());
        binding.registerButton.setOnClickListener(v -> validateAndRegister());
        binding.loginPromptTextView.setOnClickListener(v -> {
            // Navigate to login activity
            finish();
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Business Logo"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            isImageSelected = true;
            binding.businessLogoImageView.setImageURI(imageUri);
        }
    }

    private void validateAndRegister() {
        String businessName = binding.businessNameEditText.getText().toString().trim();
        String businessEmail = binding.businessEmailEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();
        String phoneNumber = binding.phoneNumberEditText.getText().toString().trim();
        String description = binding.businessDescriptionEditText.getText().toString().trim();

        // Validation
        if (!isImageSelected) {
            Toast.makeText(this, "Please select a business logo", Toast.LENGTH_SHORT).show();
            return;
        }
        if (businessName.isEmpty() || businessEmail.isEmpty() || password.isEmpty()
                || phoneNumber.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password should be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();
        registerSeller(businessName, businessEmail, password, phoneNumber, description);
    }

    private void registerSeller(String businessName, String businessEmail, String password,
                                String phoneNumber, String description) {
        mAuth.createUserWithEmailAndPassword(businessEmail, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String sellerId = task.getResult().getUser().getUid();
                        uploadLogoAndSaveData(sellerId, businessName, businessEmail, phoneNumber, description);
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this,
                                "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadLogoAndSaveData(String sellerId, String businessName, String businessEmail,
                                       String phoneNumber, String description) {
        StorageReference logoRef = storageReference.child("seller_logos/" + sellerId + ".jpg");

        logoRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    logoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String logoUrl = uri.toString();
                        saveSellerData(sellerId, businessName, businessEmail, phoneNumber,
                                description, logoUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this,
                            "Failed to upload logo: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void saveSellerData(String sellerId, String businessName, String businessEmail,
                                String phoneNumber, String description, String logoUrl) {
        DatabaseReference sellersRef = database.getReference("sellers");

        Seller seller = new Seller(
                sellerId,
                businessName,
                businessEmail,
                phoneNumber,
                description,
                logoUrl,
                System.currentTimeMillis()  // registrationDate
        );

        sellersRef.child(sellerId).setValue(seller)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this,
                            "Registration successful!", Toast.LENGTH_SHORT).show();
                    // Navigate to seller dashboard or home
                    startActivity(new Intent(RegisterActivity.this,
                            Dashboard.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this,
                            "Failed to save seller data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up binding
        binding = null;
    }
}

