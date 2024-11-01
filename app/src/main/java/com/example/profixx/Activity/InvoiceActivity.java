package com.example.profixx.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.profixx.Helper.ManagmentCart;
import com.example.profixx.R;
import com.example.profixx.Users.SigninActivity;
import com.example.profixx.databinding.ActivityInvoiceBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class InvoiceActivity extends BaseActivity {
    private ActivityInvoiceBinding binding;
    private ManagmentCart managerCart;
    private DatabaseReference myRef;

    GoogleSignInClient gsc;
    GoogleSignInOptions gso;
    FirebaseAuth mAuth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityInvoiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");

        managerCart = new ManagmentCart(this);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        gsc = GoogleSignIn.getClient(this, gso);

        initUser();
        calculateCartTotals();
        setVariable();
    }

    private void initUser() {
        // Check if a Google account is signed in
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            String userId = acct.getId();
            loadUserData(userId, acct);
        } else {
            // If Google account is not signed in, check Firebase authentication
            user = mAuth.getCurrentUser();
            if (user != null) {
                loadUserData(user.getUid(), null);
            }
        }
    }

    private void loadUserData(String userId, GoogleSignInAccount acct) {
        if (acct != null) {
            // Set Google account information
            binding.username.setText(acct.getDisplayName());
            binding.email.setText(acct.getEmail());
            binding.phoneNumber.setText("No phone number");
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
                    String provinceText = snapshot.child("province").getValue(String.class);
                    String postalCodeText = snapshot.child("postalCode").getValue(String.class);
                    String phoneText = snapshot.child("phone").getValue(String.class);

                    // Set shipping details
                    binding.address.setText(addressText != null ? addressText : "No address");
                    binding.suburb.setText(suburbText != null ? suburbText : "No suburb");
                    binding.city.setText(cityText != null ? cityText : "No city");
                    binding.country.setText(countryText != null ? countryText : "No country");
                    binding.postalCode.setText(postalCodeText != null ? postalCodeText : "No postal code");
                    binding.province.setText(provinceText != null ? provinceText : "No province");
                    binding.phoneNumber.setText(phoneText != null ? phoneText : "No phone number");

                    // If it's a regular user, also load their basic info
                    if (acct == null && user != null) {
                        String usernameText = snapshot.child("username").getValue(String.class);
                        String emailText = snapshot.child("email").getValue(String.class);

                        binding.username.setText(usernameText != null ? usernameText : "No username");
                        binding.email.setText(emailText != null ? emailText : user.getEmail());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(InvoiceActivity.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setVariable() {
        binding.btnReturn.setOnClickListener(v -> {
            Intent intent = new Intent(InvoiceActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void calculateCartTotals() {
        // Calculate total number of items
        int totalQuantity = 0;
        for (int i = 0; i < managerCart.getListCart().size(); i++) {
            totalQuantity += managerCart.getListCart().get(i).getNumberInCart();
        }
        binding.totalQuantity.setText(String.valueOf(totalQuantity));

        // Calculate other totals
        double percentTax = 0.02;
        double delivery = 10.00;
        double tax = Math.round((managerCart.getTotalFee() * percentTax * 100.0)) / 100.0;

        double total = Math.round((managerCart.getTotalFee() + tax + delivery) * 100.0) / 100.0;
        double itemTotal = Math.round(managerCart.getTotalFee() * 100.0) / 100.0;

        binding.totalFeeTxt.setText("$" + itemTotal);
        binding.deliveryTxt.setText("$" + delivery);
        binding.taxTxt.setText("$" + tax);
        binding.totalTxt.setText("$" + total);
    }
}