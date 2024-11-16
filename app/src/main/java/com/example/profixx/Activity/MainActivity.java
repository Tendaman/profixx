package com.example.profixx.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

import com.example.profixx.Adapter.CategoryAdapter;
import com.example.profixx.Adapter.PopularAdapter;
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

public class MainActivity extends BaseActivity {
    private ActivityMainBinding binding;

    String businessId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        businessId = "wIa0mvasuYNhgTkpnOGRhI1xklI3";

        Toast.makeText(this, "BusinessId: " + businessId, Toast.LENGTH_SHORT).show();

        initBanner();
        initCategory();
        initPopular();
        bottomNavigation();
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