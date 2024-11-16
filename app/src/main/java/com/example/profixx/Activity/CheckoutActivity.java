package com.example.profixx.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.profixx.Domain.ItemsDomain;
import com.example.profixx.Helper.ManagmentCart;
import com.example.profixx.R;
import com.example.profixx.databinding.ActivityCheckoutBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.paypal.checkout.PayPalCheckout;
import com.paypal.checkout.approve.Approval;
import com.paypal.checkout.approve.OnApprove;
import com.paypal.checkout.config.CheckoutConfig;
import com.paypal.checkout.config.Environment;
import com.paypal.checkout.config.PaymentButtonIntent;
import com.paypal.checkout.config.SettingsConfig;
import com.paypal.checkout.config.UIConfig;
import com.paypal.checkout.createorder.CreateOrder;
import com.paypal.checkout.createorder.CreateOrderActions;
import com.paypal.checkout.createorder.CurrencyCode;
import com.paypal.checkout.createorder.OrderIntent;
import com.paypal.checkout.createorder.UserAction;
import com.paypal.checkout.order.Amount;
import com.paypal.checkout.order.AppContext;
import com.paypal.checkout.order.CaptureOrderResult;
import com.paypal.checkout.order.OnCaptureComplete;
import com.paypal.checkout.order.OrderRequest;
import com.paypal.checkout.order.PurchaseUnit;
import com.paypal.checkout.paymentbutton.PaymentButtonContainer;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CheckoutActivity extends BaseActivity {
    private final String PublishableKey = "pk_test_51PyfvSERQuHKD8YnPpoqR6V67Jn2IsKbVsOokqQFOESCm1fsiAjbj6omPJd1LQjqTaNtP1XIVeUSO35yNaz03a2B0047JcEHrs";
    private final String SecretKey = "sk_test_51PyfvSERQuHKD8Yn0oVZqd1JhRr86N2jYNTlQHH7XF25JL4wI9shWQ2yKGrHZYvLyTy9FgaHL1odBYuhwiBa8DT900rMGixuYU";
    private DatabaseReference databaseReference;
    private String CustomerId;
    private String EphericalKey;
    private String ClientSecret;
    private PaymentSheet paymentSheet;
    private ActivityCheckoutBinding binding;
    PaymentButtonContainer paymentButtonContainer;
    public static final String TAG = "Checkout Activity";
    private double totalPrice;
    private double productTotal;
    private ManagmentCart managerCart;
    private final String PayPalClientId = "AVvtG0G1Ai2K1cEGnfYdfYLZHaC_FBTIKZlW8Cit-E_vWvmMWw50NqMH69bigw1TyDZonWp4F_dZjCmO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Configure PayPal
        PayPalCheckout.setConfig(new CheckoutConfig(
                getApplication(),
                PayPalClientId,
                Environment.SANDBOX,
                CurrencyCode.USD,
                UserAction.PAY_NOW,
                PaymentButtonIntent.CAPTURE,
                new SettingsConfig(true, false),
                new UIConfig(true),
                "com.example.profixx://paypalpay"
        ));

        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        managerCart = new ManagmentCart(this);
        calculateCartTotals();
        setVariable();
        initPayPal();
        initCardPay();
        initChangeAddress();
    }

    private void initChangeAddress() {
        binding.changeAddress.setOnClickListener(v -> {
            Intent intent = new Intent(CheckoutActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    private void initPayPal() {
        paymentButtonContainer = findViewById(R.id.payment_button_container);

        paymentButtonContainer.setup(
                new CreateOrder() {
                    @Override
                    public void create(@NonNull CreateOrderActions createOrderActions) {
                        Log.d(TAG, "Initializing PayPal Order");
                        ArrayList<PurchaseUnit> purchaseUnits = new ArrayList<>();
                        purchaseUnits.add(
                                new PurchaseUnit.Builder()
                                        .amount(
                                                new Amount.Builder()
                                                        .currencyCode(CurrencyCode.USD)
                                                        .value(String.valueOf(totalPrice))
                                                        .build()
                                        ).build()
                        );
                        OrderRequest order = new OrderRequest(
                                OrderIntent.CAPTURE,
                                new AppContext.Builder()
                                        .userAction(UserAction.PAY_NOW)
                                        .build(),
                                purchaseUnits
                        );
                        createOrderActions.create(order, (CreateOrderActions.OnOrderCreated) null);
                    }
                },
                new OnApprove(){
                    @Override
                    public void onApprove(@NonNull Approval approval) {
                        approval.getOrderActions().capture(new OnCaptureComplete() {
                            @Override
                            public void onCaptureComplete(@NonNull CaptureOrderResult result) {
                                Log.d(TAG, String.format("CaptureOrderResult: %s", result));
                                Toast.makeText(CheckoutActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                                saveOrderData();
                                Intent intent = new Intent(CheckoutActivity.this, InvoiceActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                }
        );
    }

    private void initCardPay() {
        // Initialize Stripe PaymentConfiguration
        PaymentConfiguration.init(this, PublishableKey);
        paymentSheet = new PaymentSheet(this, this::onPaymentResult);

        // Create a Stripe Customer
        createCustomer();

        binding.cardPaymentButton.setOnClickListener(v -> paymentFlow());
    }

    private void createCustomer() {
        StringRequest request = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/customers",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            CustomerId = object.getString("id");
                            Toast.makeText(CheckoutActivity.this, "Customer ID: " + CustomerId, Toast.LENGTH_SHORT).show();
                            getEmphericalKey();
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(CheckoutActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer " + SecretKey);
                return header;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void getEmphericalKey() {
        StringRequest request = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/ephemeral_keys",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            EphericalKey = object.getString("id");
                            Toast.makeText(CheckoutActivity.this, "Ephemeral Key: " + EphericalKey, Toast.LENGTH_SHORT).show();
                            getClientSecret(CustomerId, EphericalKey);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(CheckoutActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + SecretKey);
                headers.put("Stripe-Version", "2024-06-20");
                return headers;
            }

            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("customer", CustomerId);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void getClientSecret(String customerId, String ephericalKey) {
        StringRequest request = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/payment_intents",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            ClientSecret = jsonObject.getString("client_secret");
                            Toast.makeText(CheckoutActivity.this, "Client Secret: " + ClientSecret, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(CheckoutActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + SecretKey);
                return headers;
            }

            @NonNull
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer", CustomerId);
                long amountInCents = Math.round(totalPrice * 100);
                params.put("amount", String.valueOf(amountInCents));
                params.put("currency", "usd");
                params.put("automatic_payment_methods[enabled]", "true");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void paymentFlow() {
        if (ClientSecret != null) {
            paymentSheet.presentWithPaymentIntent(
                    ClientSecret,
                    new PaymentSheet.Configuration(
                            "Profixx",
                            new PaymentSheet.CustomerConfiguration(
                                    CustomerId,
                                    EphericalKey
                            )
                    )
            );
        } else {
            Toast.makeText(this, "Client secret is not initialized", Toast.LENGTH_SHORT).show();
        }
    }

    private void onPaymentResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show();
            saveOrderData();
            Intent intent = new Intent(CheckoutActivity.this, InvoiceActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void setVariable() {
        binding.backBtn.setOnClickListener(v -> finish());
    }

    private void calculateCartTotals() {
        // Calculate total number of items
        int totalQuantity = 0;
        for (int i = 0; i < managerCart.getListCart().size(); i++) {
            totalQuantity += managerCart.getListCart().get(i).getNumberInCart();
        }
        binding.totalQuantity.setText(String.valueOf(totalQuantity));

        // Calculate other totals
        double percentTax = 0.02;
        double delivery = 10.00;
        double tax = Math.round((managerCart.getTotalFee() * percentTax * 100.0)) / 100.0;

        double total = Math.round((managerCart.getTotalFee() + tax + delivery) * 100.0) / 100.0;
        double itemTotal = Math.round(managerCart.getTotalFee() * 100.0) / 100.0;

        binding.totalFeeTxt.setText("$" + itemTotal);
        binding.deliveryTxt.setText("$" + delivery);
        binding.taxTxt.setText("$" + tax);
        binding.totalTxt.setText("$" + total);

        productTotal = itemTotal;

        totalPrice = total;
    }

    private void saveOrderData() {
        // Replace with user data retrieval method
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databasesReference = FirebaseDatabase.getInstance().getReference("users");
        databasesReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String username = snapshot.child("username").getValue(String.class);
                    String address = snapshot.child("address").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String city = snapshot.child("city").getValue(String.class);
                    String province = snapshot.child("province").getValue(String.class);
                    String postalCode = snapshot.child("postalCode").getValue(String.class);
                    String suburb = snapshot.child("suburb").getValue(String.class);
                    String country = snapshot.child("country").getValue(String.class);
                    String phoneNumber = snapshot.child("phoneNumber").getValue(String.class);
                    String photoUrl = snapshot.child("photoUrl").getValue(String.class);

                    uploadData(username, address, email, city, province, postalCode, suburb, country, phoneNumber, photoUrl);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void uploadData(String username, String address, String email, String city, String province, String postalCode, String suburb, String country, String phoneNumber, String photoUrl) {
        DatabaseReference myref = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        myref.child("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                    for (ItemsDomain item : managerCart.getListCart()) {
                        String businessId = item.getBusinessId();
                        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("businesses").child(businessId).child("orders");

                        String orderId = orderRef.push().getKey();
                        if (orderId != null) {
                            Map<String, Object> orderData = new HashMap<>();
                            orderData.put("orderId", orderId);
                            orderData.put("userId", userId);
                            orderData.put("userName", username); // Set the fetched username
                            orderData.put("itemName", item.getTitle());
                            orderData.put("quantity", item.getNumberInCart());
                            orderData.put("totalAmount",item.getNumberInCart() * item.getPrice());
                            orderData.put("status", "pending");
                            orderData.put("phoneNumber", phoneNumber);
                            orderData.put("address", address);
                            orderData.put("email", email);
                            orderData.put("city", city);
                            orderData.put("province", province);
                            orderData.put("postalCode", postalCode);
                            orderData.put("suburb", suburb);
                            orderData.put("country", country);
                            orderData.put("photoUrl", photoUrl);
                            orderData.put("cartItems", managerCart.getListCart());

                            orderRef.child(orderId).setValue(orderData)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Log.d(TAG, "Order data saved successfully!");
                                        } else {
                                            Log.e(TAG, "Failed to save order data", task1.getException());
                                        }
                                    });
                        }
                    }
            } else {
                Log.e(TAG, "Failed to fetch username", task.getException());
            }
        });
    }
}
