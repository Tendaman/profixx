package com.example.profixx.BussinessActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.example.profixx.Activity.BaseActivity;
import com.example.profixx.R;
import com.example.profixx.Users.BusinessLoginActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import java.util.HashMap;
import java.util.Objects;

public class BusinessProfileActivity extends BaseActivity {

    ConstraintLayout logoutBtn;
    ImageView phoneEdit, phoneSave;
    FirebaseAuth mAuth;
    FirebaseUser user;
    LinearLayout homeBtn, profileBtn;
    TextView email, username, businessName;
    ImageView profilePic;
    TextInputEditText businessDesc, phoneNumber;
    ImageView editBtn;
    Button saveBtn;
    DatabaseReference myRef;
    FloatingActionButton fab;

    StorageReference storageReference;
    private Uri imageUri;
    ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_profile);

        // Initialize Firebase Auth and Database reference
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("businesses");
        storageReference = FirebaseStorage.getInstance().getReference();

        logoutBtn = findViewById(R.id.btnSign_out);
        businessName = findViewById(R.id.businessName);
        email = findViewById(R.id.email);
        phoneNumber = findViewById(R.id.phoneNumber);
        phoneEdit = findViewById(R.id.phoneEdit);
        phoneSave = findViewById(R.id.phoneSave);
        username = findViewById(R.id.username);
        profilePic = findViewById(R.id.logo);
        editBtn = findViewById(R.id.editBtn);
        businessDesc = findViewById(R.id.businessDesc);
        saveBtn = findViewById(R.id.saveBtn);
        homeBtn = findViewById(R.id.homeBtn);
        profileBtn = findViewById(R.id.profileBtn);
        fab = findViewById(R.id.fab);

        initFab();
        bottomNav();


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
                                        updateProfilePhotoUrl(user.getUid(), downloadUrl);
                                    }
                                }))
                                .addOnFailureListener(e -> Toast.makeText(BusinessProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show());
                    }
                }
        );


            // If Google account is not signed in, check Firebase authentication
            user = mAuth.getCurrentUser();
            if (user == null) {
                redirectToSignIn();
            } else {
                loadUserData(user.getUid());
            }

        setupClickListeners( user.getUid());
    }

    private void bottomNav() {
        homeBtn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), DashboardActivity.class)));
        profileBtn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), BusinessProfileActivity.class)));
    }

    private void initFab() {
        fab.setOnClickListener(v -> {
            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.bottomsheetlayout);
            dialog.setCanceledOnTouchOutside(true);

            // Find the views within the bottomsheetlayout
            View createProduct = dialog.findViewById(R.id.createProduct);
            View promoteStore = dialog.findViewById(R.id.promoteStore);
            View viewTrans = dialog.findViewById(R.id.viewTrans);
            View closeSlide = dialog.findViewById(R.id.closeSlide);

            // Set up click listeners for each option
            createProduct.setOnClickListener(v1 -> {
                startActivity(new Intent(getApplicationContext(), CreateProductActivity.class));
                dialog.dismiss();
            });

            viewTrans.setOnClickListener(v1 -> {
                Toast.makeText(BusinessProfileActivity.this, "View Transactions Selected", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });

            promoteStore.setOnClickListener(v1 -> {
                Toast.makeText(BusinessProfileActivity.this, "Promote Store Selected", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });

            // GestureDetector to detect swipe-down gestures on closeSlide ImageView
            GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                    // Detect swipe down
                    if (e2.getY() - e1.getY() > 100 && Math.abs(velocityY) > 100) {
                        dialog.dismiss(); // Close dialog on downward swipe
                        return true;
                    }
                    return false;
                }

                @Override
                public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                    // Call performClick for accessibility
                    closeSlide.performClick();
                    return true;
                }
            });

            // Set the GestureDetector on the closeSlide ImageView
            closeSlide.setOnTouchListener((v1, event) -> {
                if (gestureDetector.onTouchEvent(event)) {
                    // Call performClick if the gesture detector handled the touch event
                    v1.performClick();
                    return true;
                }
                return false;
            });

            // Override performClick to log or handle additional click functionality if needed
            closeSlide.setOnClickListener(v1 -> {
                dialog.dismiss();
            });

            // Show the dialog
            dialog.show();
            Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            dialog.getWindow().setGravity(Gravity.BOTTOM);
        });
    }

    private void loadUserData(String userId) {
        // Load user data from Firebase
        myRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Retrieve and set the user's basic info from Firebase
                    String usernameText = snapshot.child("username").getValue(String.class);
                    String emailText = snapshot.child("businessEmail").getValue(String.class);
                    String photoUrl = snapshot.child("logo").getValue(String.class);
                    String businessNametext = snapshot.child("businessName").getValue(String.class);

                    // Profile photo URL

                    // Set values to UI components
                    username.setText(usernameText != null ? usernameText : "No username");
                    email.setText(emailText != null ? emailText : "No email");
                    businessName.setText(businessNametext != null ? businessNametext : "No business name");

                    // Display the photo URL if available, or fallback to default image
                    if (photoUrl != null && !photoUrl.isEmpty()) {
                        Glide.with(BusinessProfileActivity.this)
                                .load(photoUrl)
                                .into(profilePic);
                    } else {
                        profilePic.setImageResource(R.drawable.baseline_perm_identity_24);  // Default image if no photo
                    }

                    // Load shipping details
                    String addressText = snapshot.child("businessDesc").getValue(String.class);
                    String phoneText = snapshot.child("businessPhone").getValue(String.class);

                    // Set shipping details
                    businessDesc.setText(addressText != null ? addressText : "");
                    phoneNumber.setText(phoneText != null ? phoneText : "No phone number");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BusinessProfileActivity.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners(String userId) {
        logoutBtn.setOnClickListener(v -> logoutMethod());

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
        myRef.child(userId).child("logo").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String photoUrl = snapshot.getValue(String.class);

                AlertDialog.Builder builder = new AlertDialog.Builder(BusinessProfileActivity.this);
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
                Toast.makeText(BusinessProfileActivity.this, "Error loading photo options", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfilePhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Image"));
    }



    private void updateProfilePhotoUrl(String userId, String downloadUrl) {
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("logo", downloadUrl);

        myRef.child(userId).updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Glide.with(BusinessProfileActivity.this)
                                .load(downloadUrl)
                                .into(profilePic);
                        Toast.makeText(BusinessProfileActivity.this, "Profile photo updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(BusinessProfileActivity.this, "Failed to update profile photo", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteProfilePhoto(String userId) {
        // Remove photo URL from Firebase Database and clear the ImageView
        myRef.child(userId).child("logo").removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        profilePic.setImageResource(R.drawable.baseline_perm_identity_24);
                        Toast.makeText(BusinessProfileActivity.this, "Profile photo deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(BusinessProfileActivity.this, "Failed to delete profile photo", Toast.LENGTH_SHORT).show();
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
        updates.put("businessPhone", newPhoneNumber);

        myRef.child(userId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(BusinessProfileActivity.this,
                        "Phone number updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(BusinessProfileActivity.this,
                        "Failed to update phone number", Toast.LENGTH_SHORT).show());
    }

    private void saveShippingDetails(String userId) {
        setShippingFieldsEnabled(false);
        saveBtn.setVisibility(View.GONE);

        HashMap<String, Object> shippingDetails = new HashMap<>();
        shippingDetails.put("businessDesc", businessDesc.getText().toString().trim());

        myRef.child(userId).updateChildren(shippingDetails)
                .addOnSuccessListener(aVoid -> Toast.makeText(BusinessProfileActivity.this,
                        "Shipping details saved successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(BusinessProfileActivity.this,
                        "Failed to save shipping details", Toast.LENGTH_SHORT).show());
    }

    private void redirectToSignIn() {
        Intent intent = new Intent(getApplicationContext(), BusinessLoginActivity.class);
        startActivity(intent);
        finish();
    }



    // Function to enable or disable shipping fields
    private void setShippingFieldsEnabled(boolean enabled) {
        editBtn.setEnabled(enabled);
    }

    void logoutMethod() {

            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), BusinessLoginActivity.class);
            startActivity(intent);
            finish();
    }

}
