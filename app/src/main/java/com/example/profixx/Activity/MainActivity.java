package com.example.profixx.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

import com.example.profixx.Adapter.CategoryAdapter;
import com.example.profixx.Adapter.PopularAdapter;
import com.example.profixx.Adapter.ProductAdapter;
import com.example.profixx.Adapter.SliderAdapter;
import com.example.profixx.Domain.CategoryDomain;
import com.example.profixx.Domain.ItemsDomain;
import com.example.profixx.Domain.SliderItems;
import com.example.profixx.databinding.ActivityMainBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class MainActivity extends BaseActivity {
    private ActivityMainBinding binding;
    String businessId;
    private ArrayList<ItemsDomain> allProducts = new ArrayList<>();
    private ProductAdapter searchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        businessId = "wIa0mvasuYNhgTkpnOGRhI1xklI3";

        initBanner();
        initCategory();
        initPopular();
        setupSearch();
        bottomNavigation();
    }

    private void setupSearch() {
        // Initialize search RecyclerView
        binding.procuctRecycler.setLayoutManager(new LinearLayoutManager(this));
        searchAdapter = new ProductAdapter(new ArrayList<>(), businessId);
        binding.procuctRecycler.setAdapter(searchAdapter);

        // Add TextWatcher to search EditText
        binding.editTextText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchText = s.toString().toLowerCase().trim();

                if (searchText.isEmpty()) {
                    // Hide dialog when search is empty
                    binding.procuctRecycler.setVisibility(View.GONE);
                } else {
                    // Show dialog and filter products
                    filterProducts(searchText);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Fetch all products for searching
        fetchAllProducts();
    }

    private void fetchAllProducts() {
        DatabaseReference myRef = database.getReference("businesses");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allProducts.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot businessSnapshot : snapshot.getChildren()) {
                        String businessId = businessSnapshot.getKey();

                        // Skip if businessId is null or empty
                        if (businessId == null || businessId.isEmpty()) {
                            continue;
                        }

                        // Get products for each business
                        DataSnapshot productsSnapshot = businessSnapshot.child("products");
                        for (DataSnapshot productSnapshot : productsSnapshot.getChildren()) {
                            ItemsDomain item = productSnapshot.getValue(ItemsDomain.class);
                            if (item != null) {
                                item.setBusinessId(businessId);
                                item.setItemId(productSnapshot.getKey());
                                allProducts.add(item);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void filterProducts(String searchText) {
        // Filter products based on search text
        ArrayList<ItemsDomain> filteredProducts = allProducts.stream()
                .filter(item -> item.getTitle().toLowerCase().contains(searchText))
                .collect(Collectors.toCollection(ArrayList::new));

        if (!filteredProducts.isEmpty()) {
            // Update search adapter with filtered products
            searchAdapter = new ProductAdapter(filteredProducts, businessId);
            binding.procuctRecycler.setAdapter(searchAdapter);

            // Show dialog
            binding.procuctRecycler.setVisibility(View.VISIBLE);
        } else {
            // Hide dialog if no products match
            binding.procuctRecycler.setVisibility(View.GONE);
        }
    }


    private void bottomNavigation() {
        binding.cartBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this,CartActivity.class)));
        binding.profileBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this,ProfileActivity.class)));
        binding.wishlistBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this,WishlistActivity.class)));
        binding.homeBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this,MainActivity.class)));
    }

    private void initPopular() {
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
                            item.setBusinessId(businessId);
                            item.setItemId(issue.getKey());
                            items.add(item);
                        }
                    }
                    if (!items.isEmpty()){
                        binding.recyclerViewPopular.setLayoutManager(new GridLayoutManager(
                                MainActivity.this,
                                2
                                ));
                        binding.recyclerViewPopular.setAdapter(new PopularAdapter(items, "wIa0mvasuYNhgTkpnOGRhI1xklI3"));
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
                    for (DataSnapshot businessSnapshot : snapshot.getChildren()) {
                        // Skip only the specified business ID
                        if (businessSnapshot.getKey().equals("wIa0mvasuYNhgTkpnOGRhI1xklI3")) {
                            continue; // Ignore this business and move to the next one
                        }

                        CategoryDomain category = businessSnapshot.getValue(CategoryDomain.class);

                        // Set ID from the snapshot key if the category is not null
                        if (category != null) {
                            category.setId(businessSnapshot.getKey()); // Set ID from the business snapshot key
                            items.add(category);
                        }
                    }
                    if (!items.isEmpty()){
                        binding.recyclerViewOfficial.setLayoutManager(new LinearLayoutManager(
                                MainActivity.this,
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

    private void initBanner() {
        DatabaseReference myRef = database.getReference("Banner");
        binding.progressBarBanner.setVisibility(View.VISIBLE);
        ArrayList<SliderItems> items = new ArrayList<>();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot issue:snapshot.getChildren()){
                        items.add(issue.getValue(SliderItems.class));
                    }
                    banner(items);
                    binding.progressBarBanner.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void banner(ArrayList<SliderItems> items) {
        binding.viewpagerSlider.setAdapter(new SliderAdapter(items,binding.viewpagerSlider));
        binding.viewpagerSlider.setClipToPadding(false);
        binding.viewpagerSlider.setClipChildren(false);
        binding.viewpagerSlider.setOffscreenPageLimit(3);
        binding.viewpagerSlider.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));

        binding.viewpagerSlider.setPageTransformer(compositePageTransformer);
    }
}