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
import com.example.profixx.databinding.ActivityDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class DashboardActivity extends BaseActivity {

    ActivityDashboardBinding binding;
    FirebaseAuth mAuth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initVariables();
        initFab();
        initSignout();
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
    }

    private void initSignout() {
        binding.btnSignOut.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(DashboardActivity.this, BusinessLoginActivity.class));
            finish();
        });

    }
}