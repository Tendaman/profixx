package com.example.profixx.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.profixx.Adapter.CategoryAdapter;
import com.example.profixx.Adapter.PopularAdapter;
import com.example.profixx.Domain.CategoryDomain;
import com.example.profixx.Domain.ItemsDomain;
import com.example.profixx.R;
import com.example.profixx.databinding.ActivityBusinessViewBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BusinessViewActivity extends BaseActivity {
    ActivityBusinessViewBinding binding;
    private String businessId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBusinessViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        businessId = getIntent().getStringExtra("businessId");

        initViews();
        initCategory();
        initProducts();
        bottomNavigation();
    }

    private void initViews() {
        DatabaseReference myRef = database.getReference("businesses").child(businessId);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Retrieve the business name and logo URL
                    String businessName = snapshot.child("businessName").getValue(String.class);
                    String logoUrl = snapshot.child("logo").getValue(String.class);

                    // Set business name to TextView
                    binding.busName.setText(businessName != null ? businessName : "Business Name");

                    // Load logo image into ImageView using Glide
                    Glide.with(BusinessViewActivity.this)
                            .load(logoUrl)
                            .placeholder(R.drawable.btn_4)  // Optional placeholder image
                            .into(binding.logo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors here (optional)
            }
        });
    }

    private void bottomNavigation() {
        binding.cartBtn.setOnClickListener(v -> startActivity(new Intent(BusinessViewActivity.this, CartActivity.class)));
        binding.profileBtn.setOnClickListener(v -> startActivity(new Intent(BusinessViewActivity.this, ProfileActivity.class)));
        binding.wishlistBtn.setOnClickListener(v -> startActivity(new Intent(BusinessViewActivity.this, WishlistActivity.class)));
        binding.homeBtn.setOnClickListener(v -> startActivity(new Intent(BusinessViewActivity.this,MainActivity.class)));
    }

    private void initProducts() {
        DatabaseReference myRef = database.getReference("businesses").child(businessId).child("products");

        binding.progressBarPopular.setVisibility(View.VISIBLE);
        ArrayList<ItemsDomain> items = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        ItemsDomain item = issue.getValue(ItemsDomain.class);

                        if (item != null) {
                            item.setBusinessId(businessId);  // Set the businessId
                            item.setItemId(issue.getKey());  // Set the itemId
                            items.add(item);
                        }
                    }
                    if (!items.isEmpty()){
                        binding.recyclerViewPoducts.setLayoutManager(new GridLayoutManager(
                                BusinessViewActivity.this,
                                2
                        ));
                        binding.recyclerViewPoducts.setAdapter(new PopularAdapter(items, businessId));
                    }
                    binding.progressBarPopular.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initCategory() {
        DatabaseReference myRef = database.getReference("businesses");
        binding.progressBarOfficial.setVisibility(View.VISIBLE);
        ArrayList<CategoryDomain> items = new ArrayList<>();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {

                        if (issue.getKey().equals("uWHIULsf3QVu98ev2BTsLyCjGNQ2")){
                            continue;
                        }

                        CategoryDomain category = issue.getValue(CategoryDomain.class);

                        // Manually set the ID from the Firebase key if it's not automatically mapped
                        if (category != null) {
                            category.setId(issue.getKey());  // Set ID from the snapshot key
                            items.add(category);
                        }
                    }
                    if (!items.isEmpty()){
                        binding.recyclerViewOfficial.setLayoutManager(new LinearLayoutManager(
                                BusinessViewActivity.this,
                                LinearLayoutManager.HORIZONTAL,
                                false
                        ));
                        binding.recyclerViewOfficial.setAdapter(new CategoryAdapter(items));
                    }
                    binding.progressBarOfficial.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}