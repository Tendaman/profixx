package com.example.profixx.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
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

public class InvoiceActivity extends BaseActivity {
    private ActivityInvoiceBinding binding;
    private ManagmentCart managerCart;

    GoogleSignInClient gsc;
    GoogleSignInOptions gso;
    FirebaseAuth mAuth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityInvoiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        managerCart = new ManagmentCart(this);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        gsc = GoogleSignIn.getClient(this, gso);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            String usernametxt = acct.getDisplayName();
            String emailtxt = acct.getEmail();
            binding.username.setText(usernametxt);
            binding.email.setText(emailtxt);
            binding.phoneNumber.setText("No phone number");
        }else{
            user = mAuth.getCurrentUser();

            if (user == null) {
                Intent intent = new Intent(getApplicationContext(), SigninActivity.class);
                startActivity(intent);
                finish();
            }else{
                binding.email.setText(user.getEmail());
                binding.phoneNumber.setText("No phone number");
            }
        }

        calculateCartTotals();
        setVariable();
    }

    private void initUser() {

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