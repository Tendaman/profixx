package com.example.profixx.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.profixx.Adapter.OrderProductsAdapter;
import com.example.profixx.Adapter.PopularAdapter;
import com.example.profixx.Domain.ItemsDomain;
import com.example.profixx.Domain.OrdersDomain;
import com.example.profixx.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class OrdersFragment extends Fragment {

    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    TextView noText;
    String businessId;
    String orderId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orderId = getArguments().getString("orderId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initOrderList(view);
    }

    private void initOrderList(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewOrders2);
        progressBar = view.findViewById(R.id.orderProgressbar2);
        noText = view.findViewById(R.id.noOrderText);

        progressBar.setVisibility(View.VISIBLE);

        businessId = FirebaseAuth.getInstance().getUid();

        Toast.makeText(getContext(), "OrderId: " + orderId, Toast.LENGTH_SHORT).show();
        Toast.makeText(getContext(), "BusinessId: " + businessId, Toast.LENGTH_SHORT).show();

        if (businessId != null && orderId != null) {
            DatabaseReference cartItemRef = FirebaseDatabase.getInstance()
                    .getReference("businesses")
                    .child(businessId)
                    .child("orders")
                    .child(orderId)
                    .child("products");

            ArrayList<OrdersDomain> productList = new ArrayList<>();

            cartItemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    productList.clear();
                    if (snapshot.exists()) {
                        for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                            OrdersDomain item = itemSnapshot.getValue(OrdersDomain.class);
                            if (item != null) {
                                item.setItemId(itemSnapshot.getKey());
                                productList.add(item);
                            }
                        }
                        if (!productList.isEmpty()) {
                            recyclerView.setLayoutManager(new GridLayoutManager(getContext(),
                                    1,
                                    RecyclerView.VERTICAL,
                                    false
                            ));
                            recyclerView.setAdapter(new OrderProductsAdapter(productList));
                            progressBar.setVisibility(View.GONE);
                        } else {
                            progressBar.setVisibility(View.GONE);
                            noText.setVisibility(View.VISIBLE);
                        }
                    }else{
                        progressBar.setVisibility(View.GONE);
                        noText.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}