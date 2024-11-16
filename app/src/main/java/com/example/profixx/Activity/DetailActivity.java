package com.example.profixx.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.profixx.Adapter.SizeAdapter;
import com.example.profixx.Adapter.SliderAdapter;
import com.example.profixx.Domain.ItemsDomain;
import com.example.profixx.Domain.SliderItems;
import com.example.profixx.Fragment.DescriptionFragment;
import com.example.profixx.Fragment.ProductReviewFragment;
import com.example.profixx.Fragment.ReviewFragment;
import com.example.profixx.Fragment.ReviewedProductFragment;
import com.example.profixx.Fragment.SoldFragment;
import com.example.profixx.Helper.ManagmentCart;
import com.example.profixx.R;
import com.example.profixx.databinding.ActivityDetailBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DetailActivity extends BaseActivity {
    ActivityDetailBinding binding;
    private ItemsDomain object;
    private final int numberOrder = 1;
    private ManagmentCart managmentCart;
    private Handler slideHandle = new Handler();
    private FirebaseAuth mAuth;
    private DatabaseReference wishlistRef;
    private boolean isInWishlist = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        wishlistRef = database.getReference("users");

        managmentCart = new ManagmentCart(this);

        getBundle();
        loadAndSetRatings();
        initbanners();
        initSize();
        setupViewPager();
        setupFavoriteButton();
        checkIfInWishlist();

    }

    private void setupFavoriteButton() {
        binding.favBtn.setOnClickListener(v -> {
            String userId = getCurrentUserId();
            if (userId != null) {
                if (isInWishlist) {
                    removeFromWishlist(userId);
                } else {
                    addToWishlist(userId);
                }
            } else {
                Toast.makeText(DetailActivity.this, "Please sign in to add items to wishlist", Toast.LENGTH_SHORT).show();
            }
        });
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

    private void checkIfInWishlist() {
        String userId = getCurrentUserId();
        if (userId != null) {
            wishlistRef.child(userId)
                    .child("wishlist")
                    .orderByChild("title")
                    .equalTo(object.getTitle())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            isInWishlist = snapshot.exists();
                            updateFavoriteIcon();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(DetailActivity.this, "Error checking wishlist", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updateFavoriteIcon() {
        // Assuming you have two different drawable resources for favorite states
        binding.favBtn.setImageResource(isInWishlist ?
                R.drawable.fav_filled : R.drawable.fav);
    }

    private void addToWishlist(String userId) {
        // Create wishlist item
        HashMap<String, Object> wishlistItem = new HashMap<>();
        wishlistItem.put("picUrl", object.getPicUrl());
        wishlistItem.put("title", object.getTitle());
        wishlistItem.put("price", object.getPrice());
        wishlistItem.put("rating", object.getRating());
        wishlistItem.put("timestamp", ServerValue.TIMESTAMP);

        // Generate a unique key for the wishlist item
        String wishlistItemId = wishlistRef.child(userId).child("wishlist").push().getKey();

        wishlistRef.child(userId)
                .child("wishlist")
                .child(wishlistItemId)
                .setValue(wishlistItem)
                .addOnSuccessListener(aVoid -> {
                    isInWishlist = true;
                    updateFavoriteIcon();
                    Toast.makeText(DetailActivity.this, "Added to wishlist", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(DetailActivity.this, "Failed to add to wishlist", Toast.LENGTH_SHORT).show()
                );
    }

    private void removeFromWishlist(String userId) {
        wishlistRef.child(userId)
                .child("wishlist")
                .orderByChild("title")
                .equalTo(object.getTitle())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                            itemSnapshot.getRef().removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        isInWishlist = false;
                                        updateFavoriteIcon();
                                        Toast.makeText(DetailActivity.this, "Removed from wishlist", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(DetailActivity.this, "Failed to remove from wishlist", Toast.LENGTH_SHORT).show()
                                    );
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(DetailActivity.this, "Error removing from wishlist", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initSize() {
        ArrayList<String> list = new ArrayList<>();
        list.add("S");
        list.add("M");
        list.add("L");
        list.add("XL");
        list.add("XXL");
        binding.recyclerSize.setAdapter(new SizeAdapter(list));
        binding.recyclerSize.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void initbanners() {
        ArrayList<SliderItems> sliderItems = new ArrayList<>();
        for (int i = 0; i < object.getPicUrl().size(); i++) {
            sliderItems.add(new SliderItems(object.getPicUrl().get(i)));
        }

        binding.viewpageSlider.setAdapter(new SliderAdapter(sliderItems,binding.viewpageSlider));
        binding.viewpageSlider.setClipToPadding(false);
        binding.viewpageSlider.setClipChildren(false);
        binding.viewpageSlider.setOffscreenPageLimit(3);
        binding.viewpageSlider.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

    }

    private void getBundle() {
        object = (ItemsDomain) getIntent().getSerializableExtra("object");
        assert object != null;
        binding.titleTxt.setText(object.getTitle());
        binding.priceTxt.setText("$" + object.getPrice());

        Toast.makeText(this, "businessID:" + object.getBusinessId(), Toast.LENGTH_SHORT).show();

        binding.addTocartBtn.setOnClickListener(v -> {
            object.setNumberInCart(numberOrder);
            object.setBusinessId(object.getBusinessId());
            managmentCart.insertFood(object);
        });
        binding.backBtn.setOnClickListener(v -> finish());
    }

    private void loadAndSetRatings() {
        String businessId = getIntent().getStringExtra("businessId");
        String itemId = getIntent().getStringExtra("itemId");

        // Reference to the product's reviews in Firebase
        assert businessId != null;
        assert itemId != null;
        DatabaseReference ratingsRef = FirebaseDatabase.getInstance()
                .getReference("businesses")
                .child(businessId)
                .child("products")
                .child(itemId)
                .child("reviews");

        ratingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalReviews = (int) snapshot.getChildrenCount();
                double totalRating = 0;

                // Calculate the total rating by summing up individual review ratings
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Double rating = dataSnapshot.child("rating").getValue(Double.class);
                    if (rating != null && rating > 0) {
                        totalRating += rating;
                    }
                }

                // Calculate and set the average rating if there are reviews
                if (totalReviews > 0) {
                    double averageRating = totalRating / totalReviews;
                    binding.ratingTxt.setText(String.format("%.1f", averageRating));
                    binding.ratingBar.setRating((float) averageRating);
                } else {
                    // Set to default values if no reviews are present
                    binding.ratingTxt.setText("0");
                    binding.ratingBar.setRating(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailActivity.this, "Failed to load ratings", Toast.LENGTH_SHORT).show();
                binding.ratingTxt.setText("0");
                binding.ratingBar.setRating(0);
            }
        });
    }
    private void setupViewPager(){
        String businessId = getIntent().getStringExtra("businessId");
        String itemId = getIntent().getStringExtra("itemId");

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        DescriptionFragment tab1 = new DescriptionFragment();
        ProductReviewFragment tab2 = new ProductReviewFragment();
        SoldFragment tab3 = new SoldFragment();

        Bundle bundle1 = new Bundle();
        Bundle bundle2 = new Bundle();
        Bundle bundle3 = new Bundle();

        bundle1.putString("description", object.getDescription());
        bundle3.putString("businessId", businessId);
        bundle3.putString("itemId", itemId);
        bundle2.putString("businessId", businessId);
        bundle2.putString("itemId", itemId);

        tab1.setArguments(bundle1);
        tab2.setArguments(bundle2);
        tab3.setArguments(bundle3);

        adapter.addFrag(tab1, "Description");
        adapter.addFrag(tab2, "Reviews");
        adapter.addFrag(tab3, "Comment");

        binding.viewpager.setAdapter(adapter);
        binding.tabLayout.setupWithViewPager(binding.viewpager);
    }

    private static class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();
        public ViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title){
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}