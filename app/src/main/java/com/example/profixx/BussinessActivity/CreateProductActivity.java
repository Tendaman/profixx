package com.example.profixx.BussinessActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.profixx.Activity.BaseActivity;
import com.example.profixx.databinding.ActivityCreateProductBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class CreateProductActivity extends BaseActivity {
    FirebaseAuth mAuth;
    String uid;
    FirebaseDatabase database;
    DatabaseReference productsRef;
    ActivityCreateProductBinding binding;
    ActivityResultLauncher<Intent> imagePickerLauncher;
    Uri imageUri;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initVariables();
        uploadImage();
        saveBtn();
    }

    private void saveBtn() {
        binding.btnSave.setOnClickListener(view -> {
            binding.progressBar2.setVisibility(View.VISIBLE);
            binding.btnSign.setVisibility(View.GONE);
            binding.textView21.setVisibility(View.GONE);

            String itemNameTxt = String.valueOf(binding.itemName.getText());
            String oldPriceTxt = String.valueOf(binding.oldPrice.getText());
            String priceTxt = String.valueOf(binding.price.getText());
            String itemDescTxt = String.valueOf(binding.productDesc.getText());

            // Validate fields before uploading
            if (itemNameTxt.isEmpty() || priceTxt.isEmpty() || itemDescTxt.isEmpty() || imageUri == null) {
                Toast.makeText(this, "Please complete all required fields", Toast.LENGTH_SHORT).show();
                binding.progressBar2.setVisibility(View.GONE);
                binding.btnSign.setVisibility(View.VISIBLE);
                binding.textView21.setVisibility(View.VISIBLE);
                return;
            }

            // Proceed with data upload
            uploadData(uid, itemNameTxt, oldPriceTxt, priceTxt, itemDescTxt);
        });
    }

    private void uploadData(String uid, String itemNameTxt, String oldPriceTxt, String priceTxt, String itemDescTxt) {
        // Generate unique identifier for each product image
        String uniqueID = UUID.randomUUID().toString();
        StorageReference imageRef = storageReference.child("product_images/" + uniqueID + ".jpg");

        // Upload image and retrieve the URL to save in database
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imgUrl = uri.toString();
                    saveProductDataToDatabase(itemNameTxt, oldPriceTxt, priceTxt, itemDescTxt, imgUrl);
                }))
                .addOnFailureListener(e -> {
                    binding.progressBar2.setVisibility(View.GONE);
                    binding.btnSign.setVisibility(View.VISIBLE);
                    binding.textView21.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveProductDataToDatabase(String itemNameTxt, String oldPriceTxt, String priceTxt, String itemDescTxt, String imgUrl) {
        // Create a unique key for each product under the "products" sub-node
        DatabaseReference newProductRef = productsRef.push();

        HashMap<String, Object> productData = new HashMap<>();
        productData.put("productImage", imgUrl);
        productData.put("itemName", itemNameTxt);
        productData.put("oldPrice", oldPriceTxt);
        productData.put("price", priceTxt);
        productData.put("itemDesc", itemDescTxt);

        // Save the product data
        newProductRef.setValue(productData).addOnCompleteListener(task -> {
            binding.progressBar2.setVisibility(View.GONE);
            binding.btnSign.setVisibility(View.VISIBLE);
            binding.textView21.setVisibility(View.VISIBLE);

            if (task.isSuccessful()) {
                Toast.makeText(this, "Product saved successfully", Toast.LENGTH_SHORT).show();
                // Optionally clear fields after successful save
                clearFields();
            } else {
                Toast.makeText(this, "Failed to save product", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadImage() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        binding.productImage.setImageURI(imageUri); // Set the selected image
                    }
                }
        );

        binding.imageButton.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            imagePickerLauncher.launch(Intent.createChooser(intent, "Select Image"));
        });
    }

    private void initVariables() {
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        // Initialize reference to the user's products sub-node
        productsRef = database.getReference("businesses").child(uid).child("products");
    }

    private void clearFields() {
        binding.itemName.setText("");
        binding.oldPrice.setText("");
        binding.price.setText("");
        binding.productDesc.setText("");
        binding.productImage.setImageURI(null);
        imageUri = null;
    }
}
