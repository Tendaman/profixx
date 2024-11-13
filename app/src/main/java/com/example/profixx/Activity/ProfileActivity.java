package com.example.profixx.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.example.profixx.R;
import com.example.profixx.Users.SigninActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.util.HashMap;

public class ProfileActivity extends BaseActivity {

    ConstraintLayout logoutBtn;
    ImageView phoneEdit, phoneSave;
    FirebaseAuth mAuth;
    FirebaseUser user;
    TextView email, username;
    ImageView backBtn, profilePic;
    GoogleSignInClient gsc;
    GoogleSignInOptions gso;
    TextInputEditText address, suburb, city, province, country, postalCode, phoneNumber;
    ImageView editBtn;
    Button saveBtn;
    DatabaseReference myRef;

    StorageReference storageReference;
    private Uri imageUri;
    ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Auth and Database reference
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");
        storageReference = FirebaseStorage.getInstance().getReference();

        // Initialize UI elements
        initializeViews();



        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();

                        // Upload the image to Firebase Storage
                        StorageReference fileReference = storageReference.child("profile_pics/" + System.currentTimeMillis() + ".jpg");
                        fileReference.putFile(imageUri)
                                .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                    String downloadUrl = uri.toString();
                                    if (user != null) {
                                        updateProfilePhotoUrl(user.getUid(), downloadUrl, null);
                                    } else {
                                        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(ProfileActivity.this);
                                        if (acct != null) {
                                            updateProfilePhotoUrl(acct.getId(), downloadUrl, acct);
                                        }
                                    }
                                }))
                                .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show());
                    }
                }
        );



        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        gsc = GoogleSignIn.getClient(this, gso);

        // Check if a Google account is signed in
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            String userId = acct.getId(); // Get Google user ID
            loadUserData(userId, acct);
        } else {
            // If Google account is not signed in, check Firebase authentication
            user = mAuth.getCurrentUser();
            if (user == null) {
                redirectToSignIn();
            } else {
                loadUserData(user.getUid(), null);
            }
        }

        setupClickListeners(acct != null ? acct.getId() : user.getUid());
    }

    private void loadUserData(String userId, GoogleSignInAccount acct) {
        // Load user data from Firebase
        myRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Retrieve and set the user's basic info from Firebase
                    String usernameText = snapshot.child("username").getValue(String.class);
                    String emailText = snapshot.child("email").getValue(String.class);
                    String photoUrl = snapshot.child("photoUrl").getValue(String.class);  // Profile photo URL

                    // Set values to UI components
                    username.setText(usernameText != null ? usernameText : "No username");
                    email.setText(emailText != null ? emailText : "No email");

                    // Display the photo URL if available, or fallback to default image
                    if (photoUrl != null && !photoUrl.isEmpty()) {
                        Glide.with(ProfileActivity.this)
                                .load(photoUrl)
                                .into(profilePic);
                    } else {
                        profilePic.setImageResource(R.drawable.baseline_perm_identity_24);  // Default image if no photo
                    }

                    // Load shipping details
                    String addressText = snapshot.child("address").getValue(String.class);
                    String suburbText = snapshot.child("suburb").getValue(String.class);
                    String cityText = snapshot.child("city").getValue(String.class);
                    String countryText = snapshot.child("country").getValue(String.class);
                    String provinceText = snapshot.child("province").getValue(String.class);
                    String postalCodeText = snapshot.child("postalCode").getValue(String.class);
                    String phoneText = snapshot.child("phone").getValue(String.class);

                    // Set shipping details
                    address.setText(addressText != null ? addressText : "");
                    suburb.setText(suburbText != null ? suburbText : "");
                    city.setText(cityText != null ? cityText : "");
                    country.setText(countryText != null ? countryText : "");
                    province.setText(provinceText != null ? provinceText : "");
                    postalCode.setText(postalCodeText != null ? postalCodeText : "");
                    phoneNumber.setText(phoneText != null ? phoneText : "No phone number");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners(String userId) {
        logoutBtn.setOnClickListener(v -> logoutMethod());
        backBtn.setOnClickListener(v -> finish());

        profilePic.setOnClickListener(v -> showImageOptionsDialog(userId));

        // Set the save button as hidden and fields as disabled by default
        saveBtn.setVisibility(View.GONE);
        phoneSave.setVisibility(View.GONE);
        setShippingFieldsEnabled(false);
        phoneNumber.setEnabled(false);

        editBtn.setOnClickListener(v -> {
            setShippingFieldsEnabled(true);
            saveBtn.setVisibility(View.VISIBLE);
        });

        saveBtn.setOnClickListener(v -> saveShippingDetails(userId));

        // Setup phone number editing
        phoneEdit.setOnClickListener(v -> {
            phoneNumber.setEnabled(true);
            phoneEdit.setVisibility(View.GONE);
            phoneSave.setVisibility(View.VISIBLE);
            phoneNumber.requestFocus();
        });

        phoneSave.setOnClickListener(v -> savePhoneNumber(userId));
    }

    private void showImageOptionsDialog(String userId) {
        // Create and show a dialog based on the presence of a photo URL in the database
        myRef.child(userId).child("photoUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String photoUrl = snapshot.getValue(String.class);

                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                builder.setTitle("Profile Image Options");

                if (photoUrl != null && !photoUrl.isEmpty()) {
                    // Photo exists: show Update and Delete options
                    builder.setItems(new String[]{"Update Profile Image", "Delete profile Image"}, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                // Handle photo update
                                updateProfilePhoto();
                                break;
                            case 1:
                                // Handle photo deletion
                                deleteProfilePhoto(userId);
                                break;
                        }
                    });
                } else {
                    // No photo URL: show Insert Photo option
                    builder.setItems(new String[]{"Insert Photo"}, (dialog, which) -> {
                        // Handle photo insert
                        insertProfilePhoto();
                    });
                }

                builder.setCancelable(true);
                builder.show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Error loading photo options", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfilePhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Image"));
    }



    private void updateProfilePhotoUrl(String userId, String downloadUrl, GoogleSignInAccount acct) {
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("photoUrl", downloadUrl);

        myRef.child(userId).updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Glide.with(ProfileActivity.this)
                                .load(downloadUrl)
                                .into(profilePic);
                        Toast.makeText(ProfileActivity.this, "Profile photo updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Failed to update profile photo", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteProfilePhoto(String userId) {
        // Remove photo URL from Firebase Database and clear the ImageView
        myRef.child(userId).child("photoUrl").removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        profilePic.setImageResource(R.drawable.baseline_perm_identity_24);
                        Toast.makeText(ProfileActivity.this, "Profile photo deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Failed to delete profile photo", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void insertProfilePhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Image"));
    }

    private void savePhoneNumber(String userId) {
        String newPhoneNumber = phoneNumber.getText().toString().trim();

        // Basic validation
        if (newPhoneNumber.isEmpty()) {
            phoneNumber.setError("Phone number cannot be empty");
            return;
        }

        // Disable editing and hide save button
        phoneNumber.setEnabled(false);
        phoneSave.setVisibility(View.GONE);
        phoneEdit.setVisibility(View.VISIBLE);

        // Save to Firebase
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("phone", newPhoneNumber);

        myRef.child(userId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(ProfileActivity.this,
                        "Phone number updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this,
                        "Failed to update phone number", Toast.LENGTH_SHORT).show());
    }

    private void saveShippingDetails(String userId) {
        setShippingFieldsEnabled(false);
        saveBtn.setVisibility(View.GONE);

        HashMap<String, Object> shippingDetails = new HashMap<>();
        shippingDetails.put("address", address.getText().toString().trim());
        shippingDetails.put("suburb", suburb.getText().toString().trim());
        shippingDetails.put("city", city.getText().toString().trim());
        shippingDetails.put("country", country.getText().toString().trim());
        shippingDetails.put("province", province.getText().toString().trim());
        shippingDetails.put("postalCode", postalCode.getText().toString().trim());

        myRef.child(userId).updateChildren(shippingDetails)
                .addOnSuccessListener(aVoid -> Toast.makeText(ProfileActivity.this,
                        "Shipping details saved successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this,
                        "Failed to save shipping details", Toast.LENGTH_SHORT).show());
    }

    private void redirectToSignIn() {
        Intent intent = new Intent(getApplicationContext(), SigninActivity.class);
        startActivity(intent);
        finish();
    }

    private void initializeViews() {
        logoutBtn = findViewById(R.id.btnSign_out);
        email = findViewById(R.id.email);
        phoneNumber = findViewById(R.id.phoneNumber);
        phoneEdit = findViewById(R.id.phoneEdit);
        phoneSave = findViewById(R.id.phoneSave);
        username = findViewById(R.id.username);
        profilePic = findViewById(R.id.imageView8);
        address = findViewById(R.id.address);
        suburb = findViewById(R.id.suburb);
        city = findViewById(R.id.city);
        country = findViewById(R.id.country);
        province = findViewById(R.id.province);
        postalCode = findViewById(R.id.postalCode);
        editBtn = findViewById(R.id.editBtn);
        saveBtn = findViewById(R.id.saveBtn);
        backBtn = findViewById(R.id.backBtn);
    }

    // Function to enable or disable shipping fields
    private void setShippingFieldsEnabled(boolean enabled) {
        address.setEnabled(enabled);
        suburb.setEnabled(enabled);
        city.setEnabled(enabled);
        country.setEnabled(enabled);
        province.setEnabled(enabled);
        postalCode.setEnabled(enabled);
    }

    void logoutMethod() {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if(acct != null) {
            gsc.signOut().addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(getApplicationContext(), SigninActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        } else {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), SigninActivity.class);
            startActivity(intent);
            finish();
        }
    }

}
