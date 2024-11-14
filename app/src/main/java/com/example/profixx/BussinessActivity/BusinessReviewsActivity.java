package com.example.profixx.BussinessActivity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.profixx.Activity.BaseActivity;
import com.example.profixx.Activity.DetailActivity;
import com.example.profixx.Adapter.ReviewedProductAdapter;
import com.example.profixx.Domain.ItemsDomain;
import com.example.profixx.Fragment.ProductReviewFragment;
import com.example.profixx.Fragment.ReviewedProductFragment;
import com.example.profixx.R;
import com.example.profixx.databinding.ActivityBusinessReviewsBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class BusinessReviewsActivity extends BaseActivity {

    ActivityBusinessReviewsBinding binding;
    private ItemsDomain object;
    private DatabaseReference wishlistRef;
    private FirebaseAuth mAuth;
    private Handler slideHandle = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBusinessReviewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupViewPager();
        initBackBtn();

        TabLayout.Tab secondTab = binding.tabLayout.getTabAt(1);
        if (secondTab != null) {
            secondTab.view.setClickable(false);
            secondTab.view.setBackgroundColor(getResources().getColor(R.color.color_dist, null));
            secondTab.view.getBackground().setAlpha(90); // Set semi-transparent opacity
            secondTab.view.setAlpha(0.5f);
        }
    }

    private void initBackBtn() {
        binding.backBtn.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
            finish();
        });
    }

    private void setupViewPager() {
        mAuth = FirebaseAuth.getInstance();

        String businessId = mAuth.getCurrentUser().getUid();
        String itemId = getIntent().getStringExtra("itemId");

        ViewPagerAdapter2 adapter = new ViewPagerAdapter2(getSupportFragmentManager());
        ReviewedProductFragment tab1 = new ReviewedProductFragment();
        ProductReviewFragment tab2 = new ProductReviewFragment();

        Bundle bundle1 = new Bundle();
        Bundle bundle2 = new Bundle();

        bundle1.putString("businessId", businessId);
        bundle1.putString("itemId", itemId);
        bundle2.putString("businessId", businessId);
        bundle2.putString("itemId", itemId);

        tab1.setArguments(bundle1);
        tab2.setArguments(bundle2);

        adapter.addFrag(tab1, "Reviewed Products");
        adapter.addFrag(tab2, "Product Reviews");

        binding.viewpager2.setAdapter(adapter);
        binding.tabLayout.setupWithViewPager(binding.viewpager2);

    }

    private static class ViewPagerAdapter2 extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();
        public ViewPagerAdapter2(@NonNull FragmentManager fm) {
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