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
    FirebaseAuth mAuth;
    FirebaseUser user;
    TextView email, username, phoneNumber;
    ImageView backBtn, profilePic;
    GoogleSignInClient gsc;
    GoogleSignInOptions gso;
    TextInputEditText address, suburb, city, country, postalCode;
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
        logoutBtn = findViewById(R.id.btnSign_out);
        email = findViewById(R.id.email);
        phoneNumber = findViewById(R.id.phone_num);
        username = findViewById(R.id.username);
        profilePic = findViewById(R.id.imageView8);

        address = findViewById(R.id.address);
        suburb = findViewById(R.id.suburb);
        city = findViewById(R.id.city);
        country = findViewById(R.id.country);
        postalCode = findViewById(R.id.postalCode);
        editBtn = findViewById(R.id.editBtn);
        saveBtn = findViewById(R.id.saveBtn);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        gsc = GoogleSignIn.getClient(this, gso);

        // Check if a Google account is signed in
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            String usernametxt = acct.getDisplayName();
            String emailtxt = acct.getEmail();
            if (acct.getPhotoUrl() != null){
                Glide.with(this)
                        .load(acct.getPhotoUrl())
                        .into(profilePic);
            } else {
                profilePic.setImageResource(R.drawable.baseline_perm_identity_24);
            }
            username.setText(usernametxt);
            email.setText(emailtxt);
            phoneNumber.setText("No phone number");
        } else {
            // If Google account is not signed in, check Firebase authentication
            user = mAuth.getCurrentUser();

            if (user == null) {
                // If user is not logged in, redirect to SigninActivity
                Intent intent = new Intent(getApplicationContext(), SigninActivity.class);
                startActivity(intent);
                finish();
            } else {
                // Retrieve data from Firebase Realtime Database
                myRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String usernameText = snapshot.child("username").getValue(String.class);
                            String phoneText = snapshot.child("phone").getValue(String.class);
                            String emailText = snapshot.child("email").getValue(String.class);
                            String addessText = snapshot.child("address").getValue(String.class);
                            String suburbText = snapshot.child("suburb").getValue(String.class);
                            String cityText = snapshot.child("city").getValue(String.class);
                            String countryText = snapshot.child("country").getValue(String.class);
                            String postalCodeText = snapshot.child("postalCode").getValue(String.class);

                            // Set retrieved values to the TextViews
                            username.setText(usernameText != null ? usernameText : "No username");
                            phoneNumber.setText(phoneText != null ? phoneText : "No phone number");
                            email.setText(emailText != null ? emailText : "No email");
                            address.setText(addessText != null ? addessText : "No address");
                            suburb.setText(suburbText != null ? suburbText : "No suburb");
                            city.setText(cityText != null ? cityText : "No city");
                            country.setText(countryText != null ? countryText : "No country");
                            postalCode.setText(postalCodeText != null ? postalCodeText : "No postal code");
                        } else {
                            Toast.makeText(ProfileActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ProfileActivity.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
                    }
                });

                profilePic.setImageResource(R.drawable.baseline_perm_identity_24);
            }
        }

        logoutBtn.setOnClickListener(v -> logoutMethod());

        setVariable();

        // Set the save button as hidden and fields as disabled by default
        saveBtn.setVisibility(View.GONE);
        setShippingFieldsEnabled(false); // disable fields initially

        editBtn.setOnClickListener(v -> {
            setShippingFieldsEnabled(true);  // Enable the fields
            saveBtn.setVisibility(View.VISIBLE);  // Show save button
        });

        // Set up save button click listener
        saveBtn.setOnClickListener(v -> {
            setShippingFieldsEnabled(false);  // Disable the fields
            saveBtn.setVisibility(View.GONE);  // Hide save button
            // Prepare data to save
            HashMap<String, Object> shippingDetails = new HashMap<>();
            shippingDetails.put("address", address.getText().toString());
            shippingDetails.put("suburb", suburb.getText().toString());
            shippingDetails.put("city", city.getText().toString());
            shippingDetails.put("country", country.getText().toString());
            shippingDetails.put("postalCode", postalCode.getText().toString());

            // Save to Firebase Realtime Database under the user's node
            myRef.child(user.getUid()).updateChildren(shippingDetails)
                    .addOnSuccessListener(aVoid -> Toast.makeText(ProfileActivity.this, "Shipping details saved successfully", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Failed to save shipping details", Toast.LENGTH_SHORT).show());
        });
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
