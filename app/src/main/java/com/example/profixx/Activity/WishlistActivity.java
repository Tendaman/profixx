// Modified WishlistActivity.java
package com.example.profixx.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.profixx.Adapter.WishlistAdapter;
import com.example.profixx.Domain.ItemsDomain;
import com.example.profixx.databinding.ActivityWishlistBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class WishlistActivity extends BaseActivity {
    private ActivityWishlistBinding binding;
    private WishlistAdapter adapter;
    private ArrayList<ItemsDomain> items;
    private FirebaseAuth mAuth;
    private DatabaseReference wishlistRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWishlistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeFirebase();
        initializeViews();
        loadWishlistItems();
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        wishlistRef = database.getReference("users");
    }

    private void initializeViews() {
        items = new ArrayList<>();
        adapter = new WishlistAdapter(items);
        binding.recyclerViewWishlist.setAdapter(adapter);
        binding.recyclerViewWishlist.setLayoutManager(new LinearLayoutManager(this));

        // Set up clear all button
        binding.imageView12.setOnClickListener(v -> clearWishlist());

        // Set up back button
        binding.imageView11.setOnClickListener(v -> finish());
    }

    private String getCurrentUserId() {
        // Check for Google Sign In first
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            return acct.getId();
        }

        // Check for Firebase user
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            return user.getUid();
        }

        return null;
    }

    private void loadWishlistItems() {
        String userId = getCurrentUserId();
        if (userId != null) {
            wishlistRef.child(userId)
                    .child("wishlist")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            items.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                ItemsDomain item = new ItemsDomain();
                                item.setItemId(dataSnapshot.getKey());
                                item.setTitle(dataSnapshot.child("title").getValue(String.class));
                                item.setPrice(dataSnapshot.child("price").getValue(Double.class));
                                item.setRating(dataSnapshot.child("rating").getValue(Double.class));

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
                            adapter.updateList(items);

                            // Update visibility based on items list
                            if (items.isEmpty()) {
                                binding.emptyWishList.setVisibility(View.VISIBLE);
                                binding.recyclerViewWishlist.setVisibility(View.GONE);
                            } else {
                                binding.emptyWishList.setVisibility(View.GONE);
                                binding.recyclerViewWishlist.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(WishlistActivity.this, "Error loading wishlist", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void clearWishlist() {
        String userId = getCurrentUserId();
        if (userId != null) {
            wishlistRef.child(userId)
                    .child("wishlist")
                    .removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(WishlistActivity.this, "Wishlist cleared", Toast.LENGTH_SHORT).show();
                        items.clear();
                        adapter.updateList(items);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(WishlistActivity.this, "Failed to clear wishlist", Toast.LENGTH_SHORT).show()
                    );
        }
    }
}