package com.example.profixx.BussinessActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.profixx.Activity.BaseActivity;
import com.example.profixx.Adapter.FinishedOrderAdapter;
import com.example.profixx.Adapter.UserOrdersAdapter;
import com.example.profixx.Domain.OrdersDomain;
import com.example.profixx.R;
import com.example.profixx.databinding.ActivityFinishedOrdersBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FinishedOrdersActivity extends BaseActivity {

    ActivityFinishedOrdersBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFinishedOrdersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initViews();
        backBtn();
    }

    private void backBtn() {
        binding.backBtn.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
            finish();
        });
    }

    private String getUserId(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        return null;
    }

    private void initViews() {
        String businessId = getUserId(); // Retrieve the current user's business ID
        assert businessId != null;

        // Reference to the "orders" node for the current business
        DatabaseReference myRef = FirebaseDatabase.getInstance()
                .getReference("businesses")
                .child(businessId)
                .child("orders");

        binding.orderProgressbar.setVisibility(View.VISIBLE);
        ArrayList<OrdersDomain> ordersList = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                        OrdersDomain order = new OrdersDomain();

                        // Retrieve the orderId
                        String orderId = orderSnapshot.getKey();
                        order.setOrderId(orderId);

                        // Retrieve `userData` (user details for the order)
                        DataSnapshot userDataSnapshot = orderSnapshot.child("userData");
                        if (userDataSnapshot.exists()) {
                            order.setUserId(userDataSnapshot.child("userId").getValue(String.class));
                            order.setUserName(userDataSnapshot.child("userName").getValue(String.class));
                            order.setEmail(userDataSnapshot.child("email").getValue(String.class));
                            order.setPhotoUrl(userDataSnapshot.child("photoUrl").getValue(String.class));
                            order.setPhone(userDataSnapshot.child("phone").getValue(String.class));
                            order.setAddress(userDataSnapshot.child("address").getValue(String.class));
                            order.setCity(userDataSnapshot.child("city").getValue(String.class));
                            order.setProvince(userDataSnapshot.child("province").getValue(String.class));
                            order.setPostalCode(userDataSnapshot.child("postalCode").getValue(String.class));
                            order.setSuburb(userDataSnapshot.child("suburb").getValue(String.class));
                            order.setCountry(userDataSnapshot.child("country").getValue(String.class));
                            order.setStatus(userDataSnapshot.child("status").getValue(String.class));
                            String status = userDataSnapshot.child("status").getValue(String.class);
                            if ("successful".equals(status)) {
                                // If status is pending, fetch the products and add to the list
                                DataSnapshot productsSnapshot = orderSnapshot.child("products");
                                if (productsSnapshot.exists()) {
                                    order.setNumItems((int) productsSnapshot.getChildrenCount()); // Set the number of items
                                } else {
                                    order.setNumItems(0); // Set to 0 if no products
                                }

                                // Add the populated `OrdersDomain` object to the list
                                ordersList.add(order);
                            }
                        }
                    }

                    // Set up RecyclerView with data
                    if (!ordersList.isEmpty()) {
                        binding.recyclerViewFinished.setLayoutManager(new LinearLayoutManager(
                                FinishedOrdersActivity.this,
                                LinearLayoutManager.VERTICAL,
                                false
                        ));
                        binding.recyclerViewFinished.setAdapter(new FinishedOrderAdapter(ordersList));
                    } else {
                        binding.noOrderText.setVisibility(View.VISIBLE); // Show "No Orders" message
                    }
                } else {
                    binding.noOrderText.setVisibility(View.VISIBLE); // Show "No Orders" message
                }
                binding.orderProgressbar.setVisibility(View.GONE); // Hide progress bar
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.orderProgressbar.setVisibility(View.GONE);
                Log.e("ViewOrderActivity", "Database error: " + error.getMessage());
            }
        });
    }
}