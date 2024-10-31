package com.example.profixx.Activity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.example.profixx.databinding.ActivityProductUploadBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class UploadProductActivity extends BaseActivity {
    private ActivityProductUploadBinding binding;
    private DatabaseReference databaseRef;
    private StorageReference storageRef;
    private Uri image1Uri, image2Uri;
    private boolean isImage1Selected = false;
    private final List<String> categories = new ArrayList<>();
    private String sellerId; // You should get this from your authentication system

    // Activity result launcher for image selection
    private final ActivityResultLauncher<String> image1Picker = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    image1Uri = uri;
                    binding.productImage1.setImageURI(uri);
                    isImage1Selected = true;
                }
            }
    );

    private final ActivityResultLauncher<String> image2Picker = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    image2Uri = uri;
                    binding.productImage2.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductUploadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        databaseRef = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();

        // Set click listeners
        setupClickListeners();

        // Load categories
        loadCategories();
    }

    private void setupClickListeners() {
        binding.productImage1.setOnClickListener(v -> image1Picker.launch("image/*"));
        binding.productImage2.setOnClickListener(v -> image2Picker.launch("image/*"));
        binding.uploadButton.setOnClickListener(v -> validateAndUpload());
    }

    private void loadCategories() {
        databaseRef.child("categories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categories.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String category = snapshot.getValue(String.class);
                    if (category != null) {
                        categories.add(category);
                    }
                }
                setupCategorySpinner();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UploadProductActivity.this,
                        "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categories
        );
        binding.categorySpinner.setAdapter(adapter);
    }

    private void validateAndUpload() {
        // Get input values
        String name = binding.productNameInput.getText().toString().trim();
        String description = binding.productDescriptionInput.getText().toString().trim();
        String priceStr = binding.priceInput.getText().toString().trim();
        String stockStr = binding.stockInput.getText().toString().trim();
        String category = binding.categorySpinner.getText().toString().trim();

        // Validate inputs
        if (name.isEmpty() || description.isEmpty() || priceStr.isEmpty() ||
                stockStr.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isImage1Selected || image2Uri == null) {
            Toast.makeText(this, "Please select both images", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        int stock;
        try {
            price = Double.parseDouble(priceStr);
            stock = Integer.parseInt(stockStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price or stock value", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress
        binding.uploadProgress.setVisibility(View.VISIBLE);
        binding.uploadButton.setEnabled(false);

        // Upload images and product data
        uploadImages(name, description, price, stock, category);
    }

    private void uploadImages(String name, String description, double price,
                              int stock, String category) {
        String productId = UUID.randomUUID().toString();
        StorageReference image1Ref = storageRef.child("products/" + productId + "/image1");
        StorageReference image2Ref = storageRef.child("products/" + productId + "/image2");

        UploadTask uploadTask1 = image1Ref.putFile(image1Uri);
        UploadTask uploadTask2 = image2Ref.putFile(image2Uri);

        // Upload first image
        uploadTask1.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return image1Ref.getDownloadUrl();
        }).continueWithTask(task -> {
            String image1Url = task.getResult().toString();

            // Upload second image
            return uploadTask2.continueWithTask(task2 -> {
                if (!task2.isSuccessful()) {
                    throw task2.getException();
                }
                return image2Ref.getDownloadUrl();
            }).continueWith(task2 -> {
                String image2Url = task2.getResult().toString();

                // Create product object
                Map<String, Object> product = new HashMap<>();
                product.put("id", productId);
                product.put("name", name);
                product.put("description", description);
                product.put("price", price);
                product.put("stock", stock);
                product.put("category", category);
                product.put("image1Url", image1Url);
                product.put("image2Url", image2Url);
                product.put("sellerId", sellerId);

                // Save to database
                return databaseRef.child("products").child(productId).setValue(product);
            });
        }).addOnCompleteListener(task -> {
            binding.uploadProgress.setVisibility(View.GONE);
            binding.uploadButton.setEnabled(true);

            if (task.isSuccessful()) {
                Toast.makeText(this, "Product uploaded successfully",
                        Toast.LENGTH_SHORT).show();
                finish();
            } else {
                // TODO fix the String base
                Toast.makeText(this, "Failed to upload product: " +
                        task.getException().getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
