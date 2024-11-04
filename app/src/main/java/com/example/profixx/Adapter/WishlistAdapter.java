// WishlistAdapter.java
package com.example.profixx.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.profixx.Activity.DetailActivity;
import com.example.profixx.Domain.ItemsDomain;
import com.example.profixx.Domain.WishlistDomain;
import com.example.profixx.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder> {
    private ArrayList<WishlistDomain> items;
    private Context context;
    private DatabaseReference wishlistRef;
    private FirebaseAuth mAuth;

    public WishlistAdapter(ArrayList<WishlistDomain> items) {
        this.items = items;
        this.mAuth = FirebaseAuth.getInstance();
        this.wishlistRef = FirebaseDatabase.getInstance().getReference("users");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_wishlist, parent, false);
        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WishlistDomain item = items.get(position);

        holder.titleTxt.setText(item.getTitle());
        holder.priceTxt.setText("$" + item.getPrice());
        holder.ratingTxt.setText(String.valueOf(item.getRating()));

        if (item.getPicUrl() != null && !item.getPicUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getPicUrl().get(0))
                    .into(holder.pic);
        }

        // Set up delete button click listener
        final int adapterPosition = position;
        holder.deleteItem.setOnClickListener(v -> removeItem(adapterPosition));

        // Set up click listener for the entire item
        holder.itemLayout.setOnClickListener(v -> {
            // Convert WishlistDomain to ItemsDomain for DetailActivity
            ItemsDomain itemsDomain = new ItemsDomain();
            itemsDomain.setTitle(item.getTitle());
            itemsDomain.setPrice(item.getPrice());
            itemsDomain.setRating(item.getRating());
            itemsDomain.setPicUrl(item.getPicUrl());
            // Set any other necessary fields that DetailActivity expects

            // Create intent and pass data
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("object", itemsDomain);
            context.startActivity(intent);
        });
    }

    private String getCurrentUserId() {
        // Check for Google Sign In first
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(context);
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

    private void removeItem(final int position) {
        String userId = getCurrentUserId();
        if (userId != null && position >= 0 && position < items.size()) {
            WishlistDomain itemToRemove = items.get(position);
            String itemId = itemToRemove.getItemId(); // Assuming you have getId() method in WishlistDomain

            // Reference to the specific item in user's wishlist using the unique ID
            DatabaseReference itemRef = wishlistRef
                    .child(userId)
                    .child("wishlist")
                    .child(itemId); // Using the Firebase-generated unique ID

            // First remove from local list to prevent race conditions
            items.remove(position);
            notifyItemRemoved(position);

            // Remove from Firebase
            itemRef.removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Item removed from wishlist", Toast.LENGTH_SHORT).show();
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

    public void updateList(ArrayList<WishlistDomain> newList) {
        items = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTxt, priceTxt, ratingTxt, deleteItem;
        ImageView pic;
        ConstraintLayout itemLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.title);
            priceTxt = itemView.findViewById(R.id.priceTxt);
            ratingTxt = itemView.findViewById(R.id.ratingTxt);
            pic = itemView.findViewById(R.id.imageView15);
            deleteItem = itemView.findViewById(R.id.deleteItem);
            itemLayout = itemView.findViewById(R.id.constraintLayout4);
        }
    }
}

