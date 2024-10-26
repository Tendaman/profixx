package com.example.profixx.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.profixx.Adapter.CartAdapter;
import com.example.profixx.Helper.ManagmentCart;
import com.example.profixx.R;
import com.example.profixx.databinding.ActivityCartBinding;

public class CartActivity extends BaseActivity {
    ActivityCartBinding binding;
    private double tax;
    private ManagmentCart managerCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        managerCart = new ManagmentCart(this);

        calculatorCart();
        setVariable();
        initCartList();
    }

    private void initCartList() {
        if (managerCart.getListCart().isEmpty()) {
            binding.emptyTxt.setVisibility(View.VISIBLE);
            binding.scrollViewCart.setVisibility(View.GONE);
        }else{
            binding.emptyTxt.setVisibility(View.GONE);
            binding.scrollViewCart.setVisibility(View.VISIBLE);
        }

        binding.cartView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.cartView.setAdapter(new CartAdapter(managerCart.getListCart(), this, () -> calculatorCart()));
    }

    private void setVariable() {
        binding.backBtn.setOnClickListener(v -> finish());

        // Add checkout button click listener
        binding.checkOutBtn.setOnClickListener(v -> {
            if (!managerCart.getListCart().isEmpty()) {
                startActivity(new Intent(CartActivity.this, CheckoutActivity.class));
            }
        });
    }

    private void calculatorCart() {
        double percentTax = 0.02;
        double delivery = 10.00;
        tax = Math.round((managerCart.getTotalFee() * percentTax * 100.0)) / 100.0;

        double total = Math.round((managerCart.getTotalFee() + tax + delivery) * 100.0) / 100.0;
        double itemTotal = Math.round(managerCart.getTotalFee() * 100.0) / 100.0;

        binding.totalFeeTxt.setText("$" + itemTotal);
        binding.deliveryTxt.setText("$" + delivery);
        binding.taxTxt.setText("$" + tax);
        binding.totalTxt.setText("$" + total);

        // Enable/disable checkout button based on cart status
        binding.checkOutBtn.setEnabled(!managerCart.getListCart().isEmpty());
    }
}