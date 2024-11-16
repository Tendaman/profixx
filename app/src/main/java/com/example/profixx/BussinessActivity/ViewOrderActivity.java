package com.example.profixx.BussinessActivity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.profixx.Activity.BaseActivity;
import com.example.profixx.R;
import com.example.profixx.databinding.ActivityViewOrderBinding;

public class ViewOrderActivity extends BaseActivity {

    ActivityViewOrderBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewOrderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

    }
}