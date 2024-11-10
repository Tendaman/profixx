package com.example.profixx.BussinessActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.profixx.Activity.BaseActivity;
import com.example.profixx.Adapter.ProductListAdapter;
import com.example.profixx.Domain.ItemsDomain;
import com.example.profixx.databinding.ActivityViewProductsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewProductsActivity extends BaseActivity {

    private ActivityViewProductsBinding binding;
    private ProductListAdapter adapter;
    private ArrayList<ItemsDomain> items;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String uid;
    private DatabaseReference productsRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewProductsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeFirebase();
        initializeViews();
        loadWishlistItems();

        backBtn();
    }

    private void backBtn() {
        binding.backBtn.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
            finish();
        });
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        uid = user.getUid();
        // Initialize reference to the user's products sub-node
        productsRef = database.getReference("businesses");
    }

    private void initializeViews() {
        items = new ArrayList<>();
        adapter = new ProductListAdapter(items);
        binding.recyclerViewProducts.setAdapter(adapter);
        binding.recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadWishlistItems() {
        binding.productProgressbar.setVisibility(View.VISIBLE);
        if (uid != null) {
            productsRef.child(uid)
                    .child("products")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                ItemsDomain item = new ItemsDomain();
                                item.setItemId(dataSnapshot.getKey());
                                item.setTitle(dataSnapshot.child("title").getValue(String.class));
                                item.setPrice(dataSnapshot.child("price").getValue(Double.class) != null
                                        ? dataSnapshot.child("price").getValue(Double.class)
                                        : 0.0);

                                item.setOldPrice(dataSnapshot.child("oldPrice").getValue(Double.class) != null
                                        ? dataSnapshot.child("oldPrice").getValue(Double.class)
                                        : null);
                                item.setDescription(dataSnapshot.child("description").getValue(String.class));
                                item.setRating(dataSnapshot.child("rating").getValue(Double.class) != null
                                        ? dataSnapshot.child("rating").getValue(Double.class)
                                        : 0.0);
                                item.setReview(dataSnapshot.child("review").getValue(Integer.class) != null
                                        ? dataSnapshot.child("review").getValue(Integer.class)
                                        : 0);

                                // Handle picUrl if it exists
                                if (dataSnapshot.hasChild("picUrl")) {
                                    ArrayList<String> pics = new ArrayList<>();
                                    for (DataSnapshot picSnapshot : dataSnapshot.child("picUrl").getChildren()) {
                                        pics.add(picSnapshot.getValue(String.class));
                                    }
                                    item.setPicUrl(pics);
                                }

                                items.add(item);
                            }
                            binding.productProgressbar.setVisibility(View.GONE);
                            adapter.updateList(items);

                            // Update visibility based on items list
                            if (items.isEmpty()) {
                                binding.noProductsText.setVisibility(View.VISIBLE);
                                binding.recyclerViewProducts.setVisibility(View.GONE);
                            } else {
                                binding.noProductsText.setVisibility(View.GONE);
                                binding.recyclerViewProducts.setVisibility(View.VISIBLE);
                                binding.recyclerViewProducts.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));
                                binding.recyclerViewProducts.setAdapter(new ProductListAdapter(items));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getApplicationContext(), "Error loading wishlist", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}