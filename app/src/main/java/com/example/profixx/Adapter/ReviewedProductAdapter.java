package com.example.profixx.Adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.profixx.Domain.ItemsDomain;
import com.example.profixx.R;
import com.example.profixx.databinding.ViewholderReviewedProductBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ReviewedProductAdapter extends RecyclerView.Adapter<ReviewedProductAdapter.ViewHolder> {

    public interface OnProductSelectedListener {
        void onProductSelected(String itemId);
    }

    private final ArrayList<ItemsDomain> items;
    private Context context;
    private final String businessId;
    private int selectedItemPosition = -1;
    private final OnProductSelectedListener listener;

    public ReviewedProductAdapter(ArrayList<ItemsDomain> items, String businessId, OnProductSelectedListener listener) {
        this.items = items;
        this.businessId = businessId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderReviewedProductBinding binding = ViewholderReviewedProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.binding.productTitle.setText(items.get(position).getTitle());

        if (items.get(position).getOldPrice() == null || items.get(position).getOldPrice() == 0) {
            holder.binding.oldPriceTxt.setVisibility(View.GONE);
        } else {
            holder.binding.oldPriceTxt.setVisibility(View.VISIBLE);
            holder.binding.oldPriceTxt.setText("$" + items.get(position).getOldPrice());
            holder.binding.oldPriceTxt.setPaintFlags(holder.binding.oldPriceTxt.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        holder.binding.priceTxt.setText("$" + items.get(position).getPrice());

        RequestOptions requestOptions = new RequestOptions().transform(new CenterCrop());
        Glide.with(context)
                .load(items.get(position).getPicUrl().get(0))
                .apply(requestOptions)
                .into(holder.binding.productImage);

        if (selectedItemPosition == holder.getAdapterPosition()) {
            holder.binding.linearLayoutProduct.setBackgroundResource(R.drawable.selected_border);
        } else {
            holder.binding.linearLayoutProduct.setBackgroundResource(R.drawable.top_navbar);
        }

        holder.binding.linearLayoutProduct.setOnClickListener(v -> {
            int clickedPosition = holder.getAdapterPosition();
            if (clickedPosition != RecyclerView.NO_POSITION) {
                selectedItemPosition = clickedPosition;
                ItemsDomain currentItem = items.get(clickedPosition);
                listener.onProductSelected(currentItem.getItemId());
                notifyItemChanged(clickedPosition);
                notifyDataSetChanged();
            }
        });

        loadReviewCount(holder, items.get(position).getItemId());
    }

    private void loadReviewCount(ViewHolder holder, String productId) {
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
        ViewholderReviewedProductBinding binding;
        public ViewHolder(@NonNull ViewholderReviewedProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
