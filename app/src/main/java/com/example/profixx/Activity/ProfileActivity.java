package com.example.profixx.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    TextInputEditText address, suburb, city, country, postalCode, phoneNumber;
    ImageView editBtn;
    Button saveBtn;
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Auth and Database reference
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");

        // Initialize UI elements
        initializeViews();

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
        if (acct != null) {
            // Set Google account information
            username.setText(acct.getDisplayName());
            email.setText(acct.getEmail());
            phoneNumber.setText("No phone number");

            if (acct.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(acct.getPhotoUrl())
                        .into(profilePic);
            } else {
                profilePic.setImageResource(R.drawable.baseline_perm_identity_24);
            }
        }

        // Load shipping details from Firebase for both Google and regular users
        myRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Load shipping details
                    String addressText = snapshot.child("address").getValue(String.class);
                    String suburbText = snapshot.child("suburb").getValue(String.class);
                    String cityText = snapshot.child("city").getValue(String.class);
                    String countryText = snapshot.child("country").getValue(String.class);
                    String postalCodeText = snapshot.child("postalCode").getValue(String.class);

                    // Set shipping details
                    address.setText(addressText != null ? addressText : "");
                    suburb.setText(suburbText != null ? suburbText : "");
                    city.setText(cityText != null ? cityText : "");
                    country.setText(countryText != null ? countryText : "");
                    postalCode.setText(postalCodeText != null ? postalCodeText : "");

                    // If it's a regular user, also load their basic info
                    if (acct == null) {
                        String usernameText = snapshot.child("username").getValue(String.class);
                        String phoneText = snapshot.child("phone").getValue(String.class);
                        String emailText = snapshot.child("email").getValue(String.class);

                        username.setText(usernameText != null ? usernameText : "No username");
                        phoneNumber.setText(phoneText != null ? phoneText : "No phone number");
                        email.setText(emailText != null ? emailText : "No email");
                        profilePic.setImageResource(R.drawable.baseline_perm_identity_24);
                    }
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

        // Set the save button as hidden and fields as disabled by default
        saveBtn.setVisibility(View.GONE);
        setShippingFieldsEnabled(false);

        editBtn.setOnClickListener(v -> {
            setShippingFieldsEnabled(true);
            saveBtn.setVisibility(View.VISIBLE);
        });

        saveBtn.setOnClickListener(v -> saveShippingDetails(userId));
    }

    private void saveShippingDetails(String userId) {
        setShippingFieldsEnabled(false);
        saveBtn.setVisibility(View.GONE);

        HashMap<String, Object> shippingDetails = new HashMap<>();
        shippingDetails.put("address", address.getText().toString().trim());
        shippingDetails.put("suburb", suburb.getText().toString().trim());
        shippingDetails.put("city", city.getText().toString().trim());
        shippingDetails.put("country", country.getText().toString().trim());
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

    private void setVariable() {
        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> finish());
    }
}
