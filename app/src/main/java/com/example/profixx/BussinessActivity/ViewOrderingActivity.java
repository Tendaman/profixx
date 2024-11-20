package com.example.profixx.BussinessActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.profixx.Activity.BaseActivity;
import com.example.profixx.Fragment.OrderDetailsFragment;
import com.example.profixx.Fragment.OrdersFragment;
import com.example.profixx.databinding.ActivityOrdersBinding;
import com.example.profixx.databinding.ActivityViewOrderingBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ViewOrderingActivity extends BaseActivity {

    ActivityViewOrderingBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewOrderingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupViewPager();
        initBackBtn();
    }

    private void initBackBtn() {
        binding.backBtn.setOnClickListener(v -> {
            onBackPressed();
            finish();
        });
    }

    private void setupViewPager() {
        mAuth = FirebaseAuth.getInstance();

        String businessId = mAuth.getCurrentUser().getUid();
        String orderId = getIntent().getStringExtra("orderId");

        ViewPagerAdapter3 adapter = new ViewPagerAdapter3(getSupportFragmentManager());

        OrdersFragment tab1 = new OrdersFragment();
        OrderDetailsFragment tab2 = new OrderDetailsFragment();

        // Bundle for OrdersFragment
        Bundle bundle1 = new Bundle();
        Bundle bundle2 = new Bundle();

        bundle1.putString("businessId", businessId);
        bundle1.putString("orderId", orderId);

        bundle2.putString("businessId", businessId);
        bundle2.putString("orderId", orderId);

        tab1.setArguments(bundle1);
        tab2.setArguments(bundle2);

        adapter.addFrag(tab1, "Ordered Products");
        adapter.addFrag(tab2, "Order Details");

        binding.viewpager3.setAdapter(adapter);
        binding.tabLayout.setupWithViewPager(binding.viewpager3);
    }

    private static class ViewPagerAdapter3 extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();
        public ViewPagerAdapter3(@NonNull FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title){
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}