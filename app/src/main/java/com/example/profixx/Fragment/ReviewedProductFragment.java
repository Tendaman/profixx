package com.example.profixx.Fragment;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.profixx.Adapter.ReviewedProductAdapter;
import com.example.profixx.Domain.ItemsDomain;
import com.example.profixx.R;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ReviewedProductFragment extends Fragment implements ReviewedProductAdapter.OnProductSelectedListener {

    private RecyclerView recyclerViewProducts;
    private TextView noProductsTextRev;
    private ProgressBar progressBar;
    private String businessId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            businessId = getArguments().getString("businessId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reviewed_products, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initProductList(view);
    }

    private void initProductList(View view) {
        recyclerViewProducts = view.findViewById(R.id.reviewedProducts);
        progressBar = view.findViewById(R.id.progressBarRev);
        noProductsTextRev = view.findViewById(R.id.noProductsTextRev);

        progressBar.setVisibility(View.VISIBLE);

        if (businessId != null) {
            DatabaseReference reviewProductRef = FirebaseDatabase.getInstance()
                    .getReference("businesses")
                    .child(businessId)
                    .child("products");

            ArrayList<ItemsDomain> items = new ArrayList<>();

            final ReviewedProductFragment fragmentReference = this;

            reviewProductRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    items.clear();
                    if (snapshot.exists()) {
                        for (DataSnapshot issue : snapshot.getChildren()) {
                            ItemsDomain item = issue.getValue(ItemsDomain.class);
                            if (item != null) {
                                item.setItemId(issue.getKey());
                                items.add(item);
                            }
                        }

                        if (!items.isEmpty()) {
                            recyclerViewProducts.setVisibility(View.VISIBLE);
                            noProductsTextRev.setVisibility(View.GONE);
                            recyclerViewProducts.setLayoutManager(new GridLayoutManager(getContext(), 1));
                            recyclerViewProducts.setAdapter(new ReviewedProductAdapter(items, businessId, fragmentReference));
                        } else {
                            recyclerViewProducts.setVisibility(View.GONE);
                            noProductsTextRev.setVisibility(View.VISIBLE);
                        }
                    } else {
                        recyclerViewProducts.setVisibility(View.GONE);
                        noProductsTextRev.setVisibility(View.VISIBLE);
                    }
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error loading products: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public void onProductSelected(String itemId) {
        ViewPager viewPager = requireActivity().findViewById(R.id.viewpager2);
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();

        // Get the current fragment instance using the ViewPager's tag format
        ProductReviewFragment reviewFragment = (ProductReviewFragment) fragmentManager
                .findFragmentByTag("android:switcher:" + R.id.viewpager2 + ":" + 1);

        if (reviewFragment != null) {
            // Update the arguments and call refreshReviews
            Bundle args = new Bundle();
            args.putString("itemId", itemId);
            args.putString("businessId", businessId);
            reviewFragment.setArguments(args);
            reviewFragment.refreshReviews();
        } else {
            Toast.makeText(getContext(), "Review fragment not found.", Toast.LENGTH_SHORT).show();
        }

        TabLayout tabLayout = requireActivity().findViewById(R.id.tabLayout);
        TabLayout.Tab secondTab = tabLayout.getTabAt(1);
        if (secondTab != null) {
            secondTab.view.setClickable(true);
            secondTab.view.setBackgroundColor(Color.TRANSPARENT); // Restore default background color
            secondTab.view.setAlpha(1f); // Restore default alpha
        }

        // Switch to the review tab in the ViewPager
        viewPager.setCurrentItem(1);
    }
}
