package com.example.profixx.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.profixx.R;
import com.example.profixx.databinding.ActivityDashboardBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class Dashboard extends BaseActivity {
    private ActivityDashboardBinding binding;
    private long backPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize view binding
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set click listeners
        setClickListeners();
        // Load dashboard data
        loadDashboardData();
    }

    private void setClickListeners() {
        binding.uploadProductCard.setOnClickListener(v -> navigateToUploadProduct());
        binding.viewOrdersCard.setOnClickListener(v -> navigateToViewOrders());
        binding.sellerProfileCard.setOnClickListener(v -> navigateToSellerProfile());
        binding.customerReviewsCard.setOnClickListener(v -> navigateToCustomerReviews());
    }

    private void loadDashboardData() {
        // Set welcome message with seller's name
        String sellerName = getSellerName();
        binding.welcomeText.setText(R.string.welcomeText + sellerName);

        // Set current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
        binding.dateText.setText(dateFormat.format(new Date()));

        // Load statistics
        loadStatistics();
    }

    private String getSellerName() {
        // TODO: Implement getting seller name from backend
        return "Seller"; // Placeholder
    }

    private void loadStatistics() {
        // TODO: Implement loading statistics from your backend
        // These are placeholder values
        binding.pendingOrdersCount.setText("5");
        binding.todayRevenue.setText("$1,250");
    }

    private void navigateToUploadProduct() {
        Intent intent = new Intent(this, UploadProductActivity.class);
        startActivity(intent);
    }

    private void navigateToViewOrders() {
        Intent intent = new Intent(this, ViewOrdersActivity.class);
        startActivity(intent);
    }

    private void navigateToSellerProfile() {
        Intent intent = new Intent(this, SellerProfileActivity.class);
        startActivity(intent);
    }

    private void navigateToCustomerReviews() {
        Intent intent = new Intent(this, CustomerReviewsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData(); // Refresh data when returning to the dashboard
    }

    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            return;
        } else {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
        backPressedTime = System.currentTimeMillis();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up the binding instance
        binding = null;
    }

}
