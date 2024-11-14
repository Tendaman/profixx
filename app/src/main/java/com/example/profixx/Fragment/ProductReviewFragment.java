package com.example.profixx.Fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.profixx.Adapter.ProductReviewAdapter;
import com.example.profixx.Domain.ProductReviewDomain;
import com.example.profixx.databinding.FragmentProductReviewBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProductReviewFragment extends Fragment {

    private FragmentProductReviewBinding binding;
    private String businessId;
    private String itemId;
    private ProductReviewAdapter adapter;
    private ArrayList<ProductReviewDomain> reviews;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            businessId = getArguments().getString("businessId");
            itemId = getArguments().getString("itemId");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProductReviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initReviewsList();
        loadReviews();
    }

    private void initReviewsList() {
        reviews = new ArrayList<>();
        binding.reviewViewRev2.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProductReviewAdapter(reviews);
        binding.reviewViewRev2.setAdapter(adapter);
    }

    public void refreshReviews() {
        if (getArguments() != null) {
            itemId = getArguments().getString("itemId");
            businessId = getArguments().getString("businessId");
        }

        if (itemId == null || businessId == null) {
            showError("Missing item or business ID.");
            return;
        }

        loadReviews();
    }

    private void loadReviews() {
        if (itemId == null) {
            return;
        }
        if (businessId == null) {
            return;
        }

        binding.progressBarRev2.setVisibility(View.VISIBLE);
        binding.noReviewsTextRev2.setVisibility(View.GONE);

        DatabaseReference reviewsRef = FirebaseDatabase.getInstance()
                .getReference("businesses")
                .child(businessId)
                .child("products")
                .child(itemId)
                .child("reviews");

        reviewsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reviews.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                        ProductReviewDomain review = reviewSnapshot.getValue(ProductReviewDomain.class);
                        if (review != null) {
                            review.setReviewId(reviewSnapshot.getKey());
                            reviews.add(review);
                        }
                    }
                }

                binding.progressBarRev2.setVisibility(View.GONE);

                if (reviews.isEmpty()) {
                    binding.noReviewsTextRev2.setVisibility(View.VISIBLE);
                    binding.reviewViewRev2.setVisibility(View.GONE);
                } else {
                    binding.noReviewsTextRev2.setVisibility(View.GONE);
                    binding.reviewViewRev2.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBarRev2.setVisibility(View.GONE);
                showError("Error loading reviews: " + error.getMessage());
            }
        });
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
