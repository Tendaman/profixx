package com.example.profixx.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.profixx.R;
import com.example.profixx.Users.BusinessLoginActivity;
import com.example.profixx.Users.SigninActivity;
import com.example.profixx.databinding.ActivityDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class DashboardActivity extends BaseActivity {

    ActivityDashboardBinding binding;
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initVariables();
        initLoadData();
        initFab();
        initSignout();
    }

    private void initLoadData() {
        user = mAuth.getCurrentUser();
        if (user == null) {
            redirectToSignIn();
        } else {
            loadUserData(user.getUid(), null);
        }
    }

    private void loadUserData(String uid, Object o) {
        myRef.child("businesses").child(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    // Get the business data
                    String businessName = task.getResult().child("businessName").getValue(String.class);
                    String businessLogo = task.getResult().child("logo").getValue(String.class);

                    // Set data to the TextView and ImageView
                    binding.busName.setText(businessName);

                    // Load the logo using Picasso (assuming it's a URL)
                    if (businessLogo != null) {
                        Picasso.get().load(businessLogo).into(binding.logo);
                    } else {
                        // Default logo in case there's no logo URL
                        Picasso.get().load(R.drawable.btn_4).into(binding.logo);
                    }
                }
            } else {
                Toast.makeText(DashboardActivity.this, "Failed to load business data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void redirectToSignIn() {
        Intent intent = new Intent(getApplicationContext(), BusinessLoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void initFab() {
        binding.fab.setOnClickListener(v -> {
            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.bottomsheetlayout);
            dialog.setCanceledOnTouchOutside(true);

            // Find the views within the bottomsheetlayout
            View createProduct = dialog.findViewById(R.id.createProduct);
            View promoteStore = dialog.findViewById(R.id.promoteStore);
            View viewTrans = dialog.findViewById(R.id.viewTrans);
            View closeSlide = dialog.findViewById(R.id.closeSlide);

            // Set up click listeners for each option
            createProduct.setOnClickListener(v1 -> {
                Toast.makeText(DashboardActivity.this, "Create Product Selected", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });

            viewTrans.setOnClickListener(v1 -> {
                Toast.makeText(DashboardActivity.this, "View Transactions Selected", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });

            promoteStore.setOnClickListener(v1 -> {
                Toast.makeText(DashboardActivity.this, "Promote Store Selected", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });

            // GestureDetector to detect swipe-down gestures on closeSlide ImageView
            GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                    // Detect swipe down
                    if (e2.getY() - e1.getY() > 100 && Math.abs(velocityY) > 100) {
                        dialog.dismiss(); // Close dialog on downward swipe
                        return true;
                    }
                    return false;
                }

                @Override
                public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                    // Call performClick for accessibility
                    closeSlide.performClick();
                    return true;
                }
            });

            // Set the GestureDetector on the closeSlide ImageView
            closeSlide.setOnTouchListener((v1, event) -> {
                if (gestureDetector.onTouchEvent(event)) {
                    // Call performClick if the gesture detector handled the touch event
                    v1.performClick();
                    return true;
                }
                return false;
            });

            // Override performClick to log or handle additional click functionality if needed
            closeSlide.setOnClickListener(v1 -> {
                dialog.dismiss();
            });

            // Show the dialog
            dialog.show();
            Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            dialog.getWindow().setGravity(Gravity.BOTTOM);
        });
    }

    private void initVariables() {
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
    }

    private void initSignout() {
        binding.btnSignOut.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(DashboardActivity.this, BusinessLoginActivity.class));
            finish();
        });

    }
}