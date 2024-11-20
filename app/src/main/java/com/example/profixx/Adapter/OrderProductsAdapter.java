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
import com.example.profixx.Domain.OrdersDomain;
import com.example.profixx.R;
import com.example.profixx.databinding.ViewholderOrderProductsBinding;

import java.util.ArrayList;

public class OrderProductsAdapter extends RecyclerView.Adapter<OrderProductsAdapter.ViewHolder> {
    private ArrayList<OrdersDomain> items;
    private Context context;

    public OrderProductsAdapter(ArrayList<OrdersDomain> items) {
        // Safeguard against null input
        this.items = (items != null) ? items : new ArrayList<>();
    }

    @NonNull
    @Override
    public OrderProductsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderOrderProductsBinding binding = ViewholderOrderProductsBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderProductsAdapter.ViewHolder holder, int position) {
        if (items == null || position >= items.size()) {
            return; // Prevent null or out-of-bounds access
        }

        OrdersDomain item = items.get(position);

        // Bind data to views
        holder.binding.itemTitle.setText(item.getTitle());
        holder.binding.priceTxt.setText("$" + item.getTotalAmount());
        holder.binding.itemIdTextView.setText(item.getItemId());


        RequestOptions requestOptions = new RequestOptions().transform(new CenterCrop());

        Glide.with(context)
                .load(item.getPicUrl() != null && !item.getPicUrl().isEmpty() ? item.getPicUrl().get(0) : R.drawable.shopping_basket_24)
                .apply(requestOptions)
                .into(holder.binding.itemPic);

        holder.binding.quantity.setText(String.valueOf(item.getQuantity()));
    }

    @Override
    public int getItemCount() {
        return (items != null) ? items.size() : 0;
    }

    public void setItems(ArrayList<OrdersDomain> newItems) {
        this.items = (newItems != null) ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderOrderProductsBinding binding;

        public ViewHolder(@NonNull ViewholderOrderProductsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
