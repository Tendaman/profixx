package com.example.profixx.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.profixx.Activity.DetailActivity;
import com.example.profixx.Domain.ItemsDomain;
import com.example.profixx.Domain.ProductReviewDomain;
import com.example.profixx.Domain.ReviewDomain;
import com.example.profixx.R;
import com.example.profixx.databinding.ViewholderPopListBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PopularAdapter extends RecyclerView.Adapter<PopularAdapter.ViewHolder>{
    ArrayList<ItemsDomain> items;
    Context context;
    private final String businessId;

    public PopularAdapter(ArrayList<ItemsDomain> items, String businessId) {
        this.items = items;
        this.businessId = businessId;
    }

    @NonNull
    @Override
    public PopularAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderPopListBinding binding = ViewholderPopListBinding.inflate(LayoutInflater.from(context),parent,false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PopularAdapter.ViewHolder holder, int position) {
        holder.binding.title.setText(items.get(position).getTitle());
        holder.binding.reviewTxt.setText(""+items.get(position).getReview());
        holder.binding.priceTxt.setText("$"+items.get(position).getPrice());
        if (items.get(position).getOldPrice() == null || items.get(position).getOldPrice() == 0) {
            holder.binding.oldPriceTxt.setVisibility(View.GONE);
        } else {
            holder.binding.oldPriceTxt.setVisibility(View.VISIBLE);
            holder.binding.oldPriceTxt.setText("$" + items.get(position).getOldPrice());
            holder.binding.oldPriceTxt.setPaintFlags(holder.binding.oldPriceTxt.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transform(new CenterCrop());

        Glide.with(context)
                .load(items.get(position).getPicUrl().get(0))
                .apply(requestOptions)
                .into(holder.binding.pic);

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) { // Check that the position is valid
                ItemsDomain currentItem = items.get(currentPosition);

                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("object", currentItem);
                intent.putExtra("businessId", businessId);
                intent.putExtra("itemId", currentItem.getItemId());

                context.startActivity(intent);
            }
        });

        loadReviewCount(holder, items.get(position).getItemId());

        loadRating(holder, items.get(position).getItemId());

    }

    private void loadRating(ViewHolder holder, String itemId) {
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

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ReviewDomain review = dataSnapshot.getValue(ReviewDomain.class);
                    if (review != null && review.getRating() > 0) {
                        totalRating += review.getRating();
                    }
                }

                if (totalReviews > 0) {
                    double averageRating = totalRating / totalReviews;
                    holder.binding.ratingTxt.setText(String.format("%.1f", averageRating));
                    holder.binding.ratingBar.setRating((float) averageRating);
                } else {
                    holder.binding.ratingTxt.setText("0");
                    holder.binding.ratingBar.setRating(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.binding.ratingTxt.setText("0");
            }
        });
    }

    private void loadReviewCount(PopularAdapter.ViewHolder holder, String productId) {
        DatabaseReference reviewsRef = FirebaseDatabase.getInstance()
                .getReference("businesses")
                .child(businessId)
                .child("products")
                .child(productId)
                .child("reviews");

        reviewsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int reviewCount = (int) snapshot.getChildrenCount();
                holder.binding.reviewTxt.setText(String.valueOf(reviewCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.binding.reviewTxt.setText("0");
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderPopListBinding binding;
        public ViewHolder(@NonNull ViewholderPopListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
