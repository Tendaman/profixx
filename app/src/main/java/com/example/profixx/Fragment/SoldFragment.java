package com.example.profixx.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.profixx.Activity.ProfileActivity;
import com.example.profixx.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Locale;

public class SoldFragment extends Fragment {

    private String businessId;
    private String itemId;
    private EditText reviewEditText;
    private ImageView sendBtn;
    private RatingBar ratingBar;
    private TextView ratingTxt;
    private ConstraintLayout ratingView;
    private TextView usernameText;
    private TextView emailText;
    private ImageView profileImage;
    private RatingBar displayRatingBar;
    private TextView displayRatingText;
    private TextInputEditText reviewDisplayText;

    private TextView deleteItem;
    private String currentReviewId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            businessId = getArguments().getString("businessId");
            itemId = getArguments().getString("itemId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sold, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        checkExistingReview();
    }

    private void initializeViews(View view) {
        // Rating input views
        ratingView = view.findViewById(R.id.ratingView);
        ratingBar = view.findViewById(R.id.ratingBar);
        ratingTxt = view.findViewById(R.id.ratingTxt);
        reviewEditText = view.findViewById(R.id.reviewTxt);
        sendBtn = view.findViewById(R.id.sendBtn);

        // Review display views
        usernameText = view.findViewById(R.id.username);
        emailText = view.findViewById(R.id.email);
        profileImage = view.findViewById(R.id.profileImage);
        displayRatingBar = view.findViewById(R.id.ratingBar2);
        displayRatingText = view.findViewById(R.id.ratingTxt2);
        reviewDisplayText = view.findViewById(R.id.reviewDisplayText);

        // Set rating bar listener
        ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) ->
                ratingTxt.setText(String.format(Locale.getDefault(), "%.0f", rating))
        );

        // Set click listener for the send button
        sendBtn.setOnClickListener(v -> submitReview());

        deleteItem = view.findViewById(R.id.deleteItem);
        deleteItem.setOnClickListener(v -> deleteReview());
    }



    private void checkExistingReview() {
        String userId = getCurrentUserId();
        if (userId == null) {
            showRatingView();
            return;
        }

        DatabaseReference reviewsRef = FirebaseDatabase.getInstance()
                .getReference("businesses")
                .child(businessId)
                .child("products")
                .child(itemId)
                .child("reviews");

        Query query = reviewsRef.orderByChild("userId").equalTo(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Review exists, show the review display view
                    for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                        displayExistingReview(reviewSnapshot);
                        break; // We only need the first review
                    }
                } else {
                    // No review exists, show the rating input view
                    showRatingView();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to check existing review", Toast.LENGTH_SHORT).show();
                showRatingView();
            }
        });
    }

    private void showRatingView() {
        ratingView.setVisibility(View.VISIBLE);
    }


    private void displayExistingReview(DataSnapshot reviewSnapshot) {
        currentReviewId = reviewSnapshot.getKey();
        String username = reviewSnapshot.child("username").getValue(String.class);
        String email = reviewSnapshot.child("email").getValue(String.class);
        String reviewText = reviewSnapshot.child("reviewText").getValue(String.class);
        Float rating = reviewSnapshot.child("rating").getValue(Float.class);
        String reviewUserId = reviewSnapshot.child("userId").getValue(String.class);
        String photoUrl = reviewSnapshot.child("photoUrl").getValue(String.class);

        ratingView.setVisibility(View.GONE);

        usernameText.setText(username != null ? username : "Anonymous");
        emailText.setText(email != null ? email : "No email");
        reviewDisplayText.setText(reviewText);

        // Display the photo URL if available, or fallback to default image
        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(getContext())
                    .load(photoUrl)
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.baseline_perm_identity_24);  // Default image if no photo
        }

        if (rating != null) {
            displayRatingBar.setRating(rating);
            displayRatingText.setText(String.format(Locale.getDefault(), "%.0f", rating));
        }

        String currentUserId = getCurrentUserId();
        if (currentUserId != null && currentUserId.equals(reviewUserId)) {
            deleteItem.setVisibility(View.VISIBLE);
        } else {
            deleteItem.setVisibility(View.GONE);
        }
    }

    private void deleteReview() {
        if (currentReviewId == null) {
            Toast.makeText(getContext(), "Cannot delete review: Review ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference reviewRef = FirebaseDatabase.getInstance()
                .getReference("businesses")
                .child(businessId)
                .child("products")
                .child(itemId)
                .child("reviews")
                .child(currentReviewId);

        reviewRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Review deleted successfully", Toast.LENGTH_SHORT).show();
                    // Reset the view to show the rating input
                    ratingView.setVisibility(View.VISIBLE);
                    // Clear the current review ID
                    currentReviewId = null;
                    // Reset the rating input
                    ratingBar.setRating(0);
                    reviewEditText.setText("");
                    checkExistingReview();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to delete review: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    private String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        }

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getContext());
        if (acct != null) {
            return acct.getId();
        }

        return null;
    }

    private void submitReview() {
        String reviewText = reviewEditText.getText().toString().trim();
        float rating = ratingBar.getRating();

        if (reviewText.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a review", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get Firebase user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            retrieveUserDataAndSubmitReview(currentUser.getUid(), reviewText, rating);
        } else {
            // Check for signed-in Google account if Firebase user is unavailable
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getContext());
            if (acct != null) {
                retrieveUserDataAndSubmitReview(acct.getId(), reviewText, rating);
            } else {
                Toast.makeText(getContext(), "Please sign in to submit a review", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void retrieveUserDataAndSubmitReview(String userId, String reviewText, float rating) {
        // Firebase reference for user data
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.child("username").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    String photoUrl = snapshot.child("photoUrl").getValue(String.class);

                    saveReviewToDatabase(userId, username, email, phone, reviewText, rating, photoUrl);
                } else {
                    Toast.makeText(getContext(), "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveReviewToDatabase(String userId, String username, String email, String phone, String reviewText, float rating, String photoUrl) {
        // Reference for storing reviews in Firebase
        DatabaseReference reviewRef = FirebaseDatabase.getInstance()
                .getReference("businesses")
                .child(businessId)
                .child("products")
                .child(itemId)
                .child("reviews");

        // Generate unique key for review
        String reviewId = reviewRef.push().getKey();

        if (reviewId != null) {
            // Review data
            HashMap<String, Object> reviewData = new HashMap<>();
            reviewData.put("reviewText", reviewText);
            reviewData.put("rating", rating);
            reviewData.put("userId", userId);
            reviewData.put("username", username != null ? username : "Anonymous");
            reviewData.put("email", email != null ? email : "No email");
            reviewData.put("phone", phone != null ? phone : "No phone");
            reviewData.put("photoUrl", photoUrl != null ? photoUrl : "");

            // Save review to Firebase
            reviewRef.child(reviewId).setValue(reviewData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Review submitted", Toast.LENGTH_SHORT).show();
                        reviewEditText.setText("");  // Clear the review text
                        ratingBar.setRating(0);
                        checkExistingReview();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to submit review", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(getContext(), "Error generating review ID", Toast.LENGTH_SHORT).show();
        }
    }
}
