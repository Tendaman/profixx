package com.example.profixx.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;
import com.example.profixx.Domain.ProductReviewDomain;
import com.example.profixx.Domain.ReviewDomain;
import com.example.profixx.databinding.ViewholderProductReviewBinding;
import com.example.profixx.databinding.ViewholderReviewBinding;

import java.util.ArrayList;

public class ProductReviewAdapter extends RecyclerView.Adapter<ProductReviewAdapter.ViewHolder> {
    private ArrayList<ProductReviewDomain> reviews;
    private Context context;

    public ProductReviewAdapter(ArrayList<ProductReviewDomain> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderProductReviewBinding binding = ViewholderProductReviewBinding.inflate(
                LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductReviewDomain review = reviews.get(position);

        // Set username
        holder.binding.username.setText(review.getUsername());

        // Set review text
        holder.binding.reviewTxt.setText(review.getReviewText());

        // Set rating
        holder.binding.ratingTxt.setText(""+review.getRating());


        // Load user profile photo if available
        if (review.getPhotoUrl() != null && !review.getPhotoUrl().isEmpty()) {
            Glide.with(context)
                    .load(review.getPhotoUrl())
                    .circleCrop()
                    .into(holder.binding.pic);
        }
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderProductReviewBinding binding;

        public ViewHolder(@NonNull ViewholderProductReviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
