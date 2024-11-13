package com.example.profixx.Users;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.profixx.Activity.BaseActivity;
import com.example.profixx.Activity.MainActivity;
import com.example.profixx.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignupActivity extends BaseActivity {
    TextInputEditText email, password, conPassword, username, phone;
    Button register,googleBtn, businessLogin;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference usersRef;
    ProgressBar progressBar;
    TextView signin;

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    GoogleSignInAccount account;
    private static final int RC_SIGN_IN = 1000;


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(currentUser != null){
            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }else if(account != null){
            updateUI(account);
        }else{
            Toast.makeText(this, "Please Login", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mAuth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");

        email = findViewById(R.id.emailtxt);
        password = findViewById(R.id.password_txt);
        conPassword = findViewById(R.id.confirm_password);
        username = findViewById(R.id.username_txt);
        phone = findViewById(R.id.phone_num);
        register = findViewById(R.id.btnSignUp);
        progressBar = findViewById(R.id.progressBar);
        signin = findViewById(R.id.txtLogin);
        googleBtn = findViewById(R.id.btnGoogle);
        businessLogin = findViewById(R.id.btnBusiness);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        gsc = GoogleSignIn.getClient(this, gso);

        googleBtn.setOnClickListener(v -> {
            if (v.getId() == R.id.btnGoogle) {
                signIn();
            }
        });

        register.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            String usernametxt = String.valueOf(username.getText());
            String phonetxt = String.valueOf(phone.getText());
            String emailtxt = String.valueOf(email.getText());
            String passwordtxt = String.valueOf(password.getText());
            String conpasswordtxt = String.valueOf(conPassword.getText());

            if (validateInputs(usernametxt, phonetxt, emailtxt, passwordtxt, conpasswordtxt)) {
                mAuth.createUserWithEmailAndPassword(emailtxt, passwordtxt)
                        .addOnCompleteListener(task -> {
                            progressBar.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    saveUserDataToDatabase(user.getUid(), usernametxt, phonetxt, emailtxt);
                                }
                                Toast.makeText(SignupActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(SignupActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                progressBar.setVisibility(View.GONE);
            }
        });

        signin.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, SigninActivity.class);
            startActivity(intent);
            finish();
        });

        businessLogin.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, BusinessLoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void saveUserDataToDatabase(String uid, String username, String phone, String email) {
        HashMap<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("phone", phone);
        userData.put("email", email);
        usersRef.child(uid).setValue(userData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("SignupActivity", "User data saved to database.");
            } else {
                Log.e("SignupActivity", "Failed to save user data.", task.getException());
            }
        });
    }

    private boolean validateInputs(String username, String phone, String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Enter Username", Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Password does not match", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void updateUI(GoogleSignInAccount account) {
        progressBar.setVisibility(View.GONE);
        if (account != null) {
            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void signIn() {
        Intent signInIntent = gsc.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            if (account != null) {
                // Get user details from Google Account
                String email = account.getEmail();
                String username = account.getDisplayName();
                String userId = account.getId();
                String photoUrl = String.valueOf(account.getPhotoUrl());


                // Save user data to Firebase
                saveGoogleUserDataToDatabase(userId, username, email, photoUrl);

                Toast.makeText(this, "Signed in successfully", Toast.LENGTH_SHORT).show();
                updateUI(account);
            }
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            updateUI(null);
        }
    }

    private void saveGoogleUserDataToDatabase(String userId, String username, String email, String photoUrl) {
        DatabaseReference userRef = usersRef.child(userId);

        // Check if user already exists
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    // User exists, check for missing data and update if necessary
                    HashMap<String, Object> updatedData = new HashMap<>();

                    if (!task.getResult().hasChild("username") || TextUtils.isEmpty(task.getResult().child("username").getValue(String.class))) {
                        updatedData.put("username", username);
                    }
                    if (!task.getResult().hasChild("email") || TextUtils.isEmpty(task.getResult().child("email").getValue(String.class))) {
                        updatedData.put("email", email);
                    }
                    if (!task.getResult().hasChild("photoUrl") || TextUtils.isEmpty(task.getResult().child("photoUrl").getValue(String.class))) {
                        updatedData.put("photoUrl", photoUrl);
                    }

                    if (!updatedData.isEmpty()) {
                        userRef.updateChildren(updatedData).addOnCompleteListener(updateTask -> {
                            if (updateTask.isSuccessful()) {
                                Log.d("SigninActivity", "User data updated in database.");
                            } else {
                                Log.e("SigninActivity", "Failed to update user data", updateTask.getException());
                            }
                        });
                    }

                } else {
                    // User doesn't exist, save new data
                    HashMap<String, Object> userData = new HashMap<>();
                    userData.put("username", username);
                    userData.put("email", email);
                    userData.put("photoUrl", photoUrl);
                    userData.put("phone", ""); // Default or empty if phone data is not available

                    userRef.setValue(userData).addOnCompleteListener(saveTask -> {
                        if (saveTask.isSuccessful()) {
                            Log.d("SigninActivity", "Google user data saved to database");
                        } else {
                            Log.e("SigninActivity", "Failed to save Google user data", saveTask.getException());
                            Toast.makeText(getApplicationContext(),
                                    "Failed to save user data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                Log.e("SignupActivity", "Error checking user existence", task.getException());
                Toast.makeText(SignupActivity.this,
                        "Error checking user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

}