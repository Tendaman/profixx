package com.example.profixx.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.profixx.BussinessActivity.AllOrdersActivity;
import com.example.profixx.BussinessActivity.OrdersActivity;
import com.example.profixx.BussinessActivity.ViewOrderingActivity;
import com.example.profixx.Domain.OrdersDomain;
import com.example.profixx.databinding.ActivityFinishedOrdersBinding;
import com.example.profixx.databinding.ViewholderFinishedOrdersBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FinishedOrderAdapter extends RecyclerView.Adapter<FinishedOrderAdapter.ViewHolder> {
    private final ArrayList<OrdersDomain> orders;
    Context context;

    public FinishedOrderAdapter(ArrayList<OrdersDomain> orders) {
        this.orders = orders;
    }

    @NonNull
    @Override
    public FinishedOrderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderFinishedOrdersBinding binding = ViewholderFinishedOrdersBinding.inflate(
                LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FinishedOrderAdapter.ViewHolder holder, int position) {
        holder.binding.username.setText(orders.get(position).getUserName());
        holder.binding.userEmail.setText(orders.get(position).getEmail());
        holder.binding.userId.setText(orders.get(position).getUserId());
        holder.binding.status.setText(orders.get(position).getStatus());
        if (orders.get(position).getStatus().equals("successful")) {
            holder.binding.status.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.binding.status.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
        }
        Glide.with(context)
                .load(orders.get(position).getPhotoUrl())
                .circleCrop()
                .into(holder.binding.profilePic);

        OrdersDomain order = orders.get(position);
        String orderId = order.getOrderId();
        String businessId = FirebaseAuth.getInstance().getUid();

        holder.binding.orderLayout.setOnClickListener(v -> {

            Toast.makeText(context, "OrderId: " + orderId, Toast.LENGTH_SHORT).show();
            Toast.makeText(context, "BusinessId: " + businessId, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, ViewOrderingActivity.class);
            intent.putExtra("orderId", order.getOrderId());
            intent.putExtra("businessId", order.getBusinessId());
            context.startActivity(intent);

        });

        if (orderId == null || orderId.isEmpty() || businessId == null || businessId.isEmpty()) {
            Log.e("UserOrdersAdapter", "Order ID or Business ID is null/empty for order: " + orders.get(position));
            holder.binding.numItems.setText("0");
            return;
        }

        // Correctly reference the `products` node
        DatabaseReference productsRef = FirebaseDatabase.getInstance()
                .getReference("businesses")
                .child(businessId)
                .child("orders")
                .child(orderId)
                .child("products");

        Toast.makeText(context, "OrderId: " + orderId, Toast.LENGTH_SHORT).show();
        Toast.makeText(context, "BusinessId: " + businessId, Toast.LENGTH_SHORT).show();

        // Fetch the number of items under the `products` node
        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int numItems = (int) snapshot.getChildrenCount();
                    if (holder.getBindingAdapterPosition() == orders.indexOf(order)) {
                        holder.binding.numItems.setText(String.valueOf(numItems));
                    }
                } else {
                    if (holder.getBindingAdapterPosition() != orders.indexOf(order)) {
                        holder.binding.numItems.setText("0");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("UserOrdersAdapter", "Failed to fetch number of items: " + error.getMessage());
                holder.binding.numItems.setText("0");
            }
        });

    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderFinishedOrdersBinding binding;
        public ViewHolder(@NonNull ViewholderFinishedOrdersBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
