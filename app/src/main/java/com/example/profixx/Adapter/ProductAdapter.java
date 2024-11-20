package com.example.profixx.Adapter;

import android.app.Activity;
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
import com.example.profixx.Domain.OrdersDomain;
import com.example.profixx.R;
import com.example.profixx.databinding.ViewholderOrderProductsBinding;
import com.example.profixx.databinding.ViewholderSearchProductsBinding;

import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private final ArrayList<ItemsDomain> items;
    private Context context;
    private final String businessId;

    public ProductAdapter(ArrayList<ItemsDomain> items, String businessId) {
        // Safeguard against null input
        this.items = items;
        this.businessId = businessId;
    }

    @NonNull
    @Override
    public ProductAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderSearchProductsBinding binding = ViewholderSearchProductsBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductAdapter.ViewHolder holder, int position) {
        if (items == null || position >= items.size()) {
            return; // Prevent null or out-of-bounds access
        }

        ItemsDomain item = items.get(position);

        // Bind data to views
        holder.binding.itemTitle.setText(item.getTitle());
        holder.binding.priceTxt.setText("$" + item.getPrice());


        RequestOptions requestOptions = new RequestOptions().transform(new CenterCrop());

        Glide.with(context)
                .load(item.getPicUrl() != null && !item.getPicUrl().isEmpty() ? item.getPicUrl().get(0) : R.drawable.shopping_basket_24)
                .apply(requestOptions)
                .into(holder.binding.itemPic);

        holder.binding.itemViewer.setOnClickListener(v -> {
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
    }

    @Override
    public int getItemCount() {
        return (items != null) ? items.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderSearchProductsBinding binding;

        public ViewHolder(@NonNull ViewholderSearchProductsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
