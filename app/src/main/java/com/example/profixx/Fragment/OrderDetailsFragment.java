package com.example.profixx.Fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.profixx.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.paypal.pyplcheckout.data.model.pojo.FirebaseAuth;

public class OrderDetailsFragment extends Fragment {

    private TextView quantityTextView, totalQuantityTextView;
    private TextView totalFeeTextView, deliveryFeeTextView, taxTextView, totalTextView;
    private TextView usernameTextView, emailTextView, phoneNumberTextView;
    private TextView addressTextView, suburbTextView, cityTextView, provinceTextView, countryTextView, postalCodeTextView;

    private String orderId;
    private String businessId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_order_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initOrderView(view);

        if (getArguments() != null) {
            orderId = getArguments().getString("orderId");
            businessId = getArguments().getString("businessId");
        }

        if (orderId != null) {
            fetchOrderDetails();
        } else {
            Toast.makeText(getContext(), "No Order ID provided", Toast.LENGTH_SHORT).show();
        }
    }

    private void initOrderView(View view) {
        // Initialize the views
        quantityTextView = view.findViewById(R.id.quantity);
        totalQuantityTextView = view.findViewById(R.id.total_quantity);
        totalFeeTextView = view.findViewById(R.id.totalFeeTxt);
        deliveryFeeTextView = view.findViewById(R.id.deliveryTxt);
        taxTextView = view.findViewById(R.id.taxTxt);
        totalTextView = view.findViewById(R.id.totalTxt);

        usernameTextView = view.findViewById(R.id.username);
        emailTextView = view.findViewById(R.id.email);
        phoneNumberTextView = view.findViewById(R.id.phoneNumber);

        addressTextView = view.findViewById(R.id.address);
        suburbTextView = view.findViewById(R.id.suburb);
        cityTextView = view.findViewById(R.id.city);
        provinceTextView = view.findViewById(R.id.province);
        countryTextView = view.findViewById(R.id.country);
        postalCodeTextView = view.findViewById(R.id.postalCode);
    }

    private void fetchOrderDetails() {

        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference()
                .child("businesses") // Assuming your data is under businesses
                .child(businessId) // You can dynamically replace "businessId" if needed
                .child("orders")
                .child(orderId); // Use the orderId to fetch the specific order

        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Fetch user data
                    DataSnapshot userDataSnapshot = snapshot.child("userData");
                    String username = userDataSnapshot.child("userName").getValue(String.class);
                    String email = userDataSnapshot.child("email").getValue(String.class);
                    String phone = userDataSnapshot.child("phone").getValue(String.class);
                    String address = userDataSnapshot.child("address").getValue(String.class);
                    String city = userDataSnapshot.child("city").getValue(String.class);
                    String province = userDataSnapshot.child("province").getValue(String.class);
                    String postalCode = userDataSnapshot.child("postalCode").getValue(String.class);
                    String suburb = userDataSnapshot.child("suburb").getValue(String.class);
                    String country = userDataSnapshot.child("country").getValue(String.class);

                    // Populate user data into the TextViews
                    usernameTextView.setText(username);
                    emailTextView.setText(email);
                    phoneNumberTextView.setText(phone);

                    addressTextView.setText(address);
                    suburbTextView.setText(suburb);
                    cityTextView.setText(city);
                    provinceTextView.setText(province);
                    countryTextView.setText(country);
                    postalCodeTextView.setText(postalCode);

                    // Fetch product details
                    DataSnapshot productsSnapshot = snapshot.child("products");
                    double totalAmount = 0;
                    int totalQuantity = 0;

                    for (DataSnapshot productSnapshot : productsSnapshot.getChildren()) {
                        int quantity = productSnapshot.child("quantity").getValue(Integer.class);
                        double price = productSnapshot.child("price").getValue(Double.class);
                        totalAmount += price * quantity;
                        totalQuantity += quantity;
                    }

                    // Update quantity and amount
                    totalQuantityTextView.setText(String.valueOf(totalQuantity));
                    totalFeeTextView.setText(String.format("$%.2f", totalAmount));

                    // Calculate and display delivery fee, tax, and total
                    deliveryFeeTextView.setText(String.format("$%.2f", calculateDeliveryFee()));
                    taxTextView.setText(String.format("$%.2f", calculateTax(totalAmount)));
                    totalTextView.setText(String.format("$%.2f", calculateTotal(totalAmount)));
                } else {
                    Toast.makeText(getContext(), "Order not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("OrderDetailsFragment", "Error fetching order details", error.toException());
                Toast.makeText(getContext(), "Failed to fetch order details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private double calculateDeliveryFee() {
        return 10.00; // Example: Flat delivery fee
    }

    private double calculateTax(double totalAmount) {
        return totalAmount * 0.1; // Example: 10% tax
    }

    private double calculateTotal(double totalAmount) {
        return totalAmount + calculateDeliveryFee() + calculateTax(totalAmount);
    }
}
