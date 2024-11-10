package com.example.profixx.Adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.profixx.Domain.ItemsDomain;
import com.example.profixx.databinding.ViewholderProductlistBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ViewHolder>{
    private ArrayList<ItemsDomain> items;
    private Context context;
    private DatabaseReference myRef;
    FirebaseAuth mAuth;

    public ProductListAdapter(ArrayList<ItemsDomain> items) {
        this.items = items;
        this.mAuth = FirebaseAuth.getInstance();
        this.myRef = FirebaseDatabase.getInstance().getReference("businesses");
    }

    @NonNull
    @Override
    public ProductListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderProductlistBinding binding = ViewholderProductlistBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductListAdapter.ViewHolder holder, int position) {
        holder.binding.title.setText(items.get(position).getTitle());
        holder.binding.reviewTxt.setText(""+items.get(position).getReview());
        holder.binding.priceTxt.setText("$"+items.get(position).getPrice());
        holder.binding.ratingTxt.setText("("+items.get(position).getRating()+")");
        if (items.get(position).getOldPrice() == null || items.get(position).getOldPrice() == 0) {
            holder.binding.oldPriceTxt.setVisibility(View.GONE);
        } else {
            holder.binding.oldPriceTxt.setVisibility(View.VISIBLE);
            holder.binding.oldPriceTxt.setText("$" + items.get(position).getOldPrice());
            holder.binding.oldPriceTxt.setPaintFlags(holder.binding.oldPriceTxt.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        holder.binding.ratingBar.setRating((float)items.get(position).getRating());

        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transform(new CenterCrop());

        Glide.with(context)
                .load(items.get(position).getPicUrl().get(0))
                .apply(requestOptions)
                .into(holder.binding.pic);

        final int adapterPosition = position;
        holder.binding.deleteItem.setOnClickListener(v -> removeItem(adapterPosition));
    }

    private String getCurrentUserId() {
        // Check for Firebase user
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            return user.getUid();
        }

        return null;
    }

    private void removeItem(final int position) {
        String userId = getCurrentUserId();
        if (userId != null && position >= 0 && position < items.size()) {
            ItemsDomain itemToRemove = items.get(position);
            String itemId = itemToRemove.getItemId();

                // Reference to the specific item in the user's products node using the product ID
                DatabaseReference itemRef = myRef
                        .child(userId)
                        .child("products")
                        .child(itemId);

                // Remove from the local list to prevent race conditions
                items.remove(position);
                notifyItemRemoved(position);

                // Remove from Firebase
                itemRef.removeValue()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "Product removed successfully", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            // If Firebase deletion fails, add the item back to the list
                            if (position < getItemCount()) {
                                items.add(position, itemToRemove);
                            } else {
                                items.add(itemToRemove);
                            }
                            notifyDataSetChanged();
                            Toast.makeText(context, "Failed to remove item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });

        } else if (userId == null) {
            Toast.makeText(context, "Please sign in to remove items", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateList(ArrayList<ItemsDomain> newList) {
        items = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderProductlistBinding binding;

        public ViewHolder(@NonNull ViewholderProductlistBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
