package com.example.profixx.Activity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.profixx.databinding.ActivitySellerprofileBinding;
import com.example.profixx.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SellerProfileActivity extends BaseActivity{
    private static final int PICK_IMAGE_REQUEST = 1;

    private ActivitySellerprofileBinding binding;
    private String sellerId;
    private Uri imageUri;
    private String currentLogoUrl;
    private DatabaseReference sellerRef;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySellerprofileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase references
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Get seller ID from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("SellerPrefs", MODE_PRIVATE);
        sellerId = prefs.getString("sellerId", null);
        if (sellerId == null) {
            // Handle case where seller is not logged in
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        sellerRef = database.getReference("sellers").child(sellerId);

        setupClickListeners();
        loadSellerData();
    }

    private void setupClickListeners() {
        binding.changeLogoButton.setOnClickListener(v -> openImageChooser());
        binding.saveProfileButton.setOnClickListener(v -> saveProfileChanges());
    }

    private void loadSellerData() {
        showProgress(true);
        sellerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Load text data
                    binding.businessNameEditText.setText(dataSnapshot.child("businessName").getValue(String.class));
                    binding.businessEmailEditText.setText(dataSnapshot.child("businessEmail").getValue(String.class));
                    binding.businessPhoneEditText.setText(dataSnapshot.child("businessPhone").getValue(String.class));
                    binding.businessDescriptionEditText.setText(dataSnapshot.child("businessDescription").getValue(String.class));

                    // Load logo
                    currentLogoUrl = dataSnapshot.child("logoUrl").getValue(String.class);
                    if (currentLogoUrl != null && !currentLogoUrl.isEmpty()) {
                        Glide.with(SellerProfileActivity.this)
                                .load(currentLogoUrl)
                                .placeholder(R.drawable.profix_big_p)
                                .error(R.drawable.profix_big_p)
                                .into(binding.businessLogoImageView);
                    }
                }
                showProgress(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showProgress(false);
                Toast.makeText(SellerProfileActivity.this,
                        "Error loading profile: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Business Logo"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this)
                    .load(imageUri)
                    .into(binding.businessLogoImageView);
        }
    }

    private void saveProfileChanges() {
        if (!validateInputs()) {
            return;
        }

        showProgress(true);

        if (imageUri != null) {
            // Upload new image first
            uploadImageAndSaveData();
        } else {
            // Save data without changing the image
            saveDataToFirebase(currentLogoUrl);
        }
    }

    private boolean validateInputs() {
        String businessName = binding.businessNameEditText.getText().toString().trim();
        String businessEmail = binding.businessEmailEditText.getText().toString().trim();
        String businessPhone = binding.businessPhoneEditText.getText().toString().trim();

        if (businessName.isEmpty()) {
            binding.businessNameEditText.setError("Business name is required");
            return false;
        }

        if (businessEmail.isEmpty()) {
            binding.businessEmailEditText.setError("Business email is required");
            return false;
        }

        if (businessPhone.isEmpty()) {
            binding.businessPhoneEditText.setError("Business phone is required");
            return false;
        }

        return true;
    }

    private void uploadImageAndSaveData() {
        String imageFileName = "business_logos/" + sellerId + "/" + UUID.randomUUID().toString();
        StorageReference imageRef = storageRef.child(imageFileName);

        UploadTask uploadTask = imageRef.putFile(imageUri);
        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return imageRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUrl = task.getResult();
                // Delete old logo if exists
                if (currentLogoUrl != null && !currentLogoUrl.isEmpty()) {
                    StorageReference oldImageRef = FirebaseStorage.getInstance()
                            .getReferenceFromUrl(currentLogoUrl);
                    oldImageRef.delete();
                }
                saveDataToFirebase(downloadUrl.toString());
            } else {
                showProgress(false);
                Toast.makeText(SellerProfileActivity.this,
                        "Failed to upload image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveDataToFirebase(String logoUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("businessName", binding.businessNameEditText.getText().toString().trim());
        updates.put("businessEmail", binding.businessEmailEditText.getText().toString().trim());
        updates.put("businessPhone", binding.businessPhoneEditText.getText().toString().trim());
        updates.put("businessDescription", binding.businessDescriptionEditText.getText().toString().trim());
        if (logoUrl != null) {
            updates.put("logoUrl", logoUrl);
        }

        sellerRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    showProgress(false);
                    Toast.makeText(SellerProfileActivity.this,
                            "Profile updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    Toast.makeText(SellerProfileActivity.this,
                            "Failed to update profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void showProgress(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.saveProfileButton.setEnabled(!show);
        binding.changeLogoButton.setEnabled(!show);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;  // Avoid memory leaks
    }
}
