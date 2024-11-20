package com.example.profixx.Users;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.profixx.Activity.BaseActivity;
import com.example.profixx.Activity.MainActivity;
import com.example.profixx.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SigninActivity extends BaseActivity {
    TextInputEditText email, password;
    Button login, googleBtn, businessLogin;
    private TextInputLayout emailLayout;
    private TextView forgotPassword;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference usersRef;
    ProgressBar progressBar;
    TextView signup;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    GoogleSignInAccount account;
    private static final int RC_SIGN_IN = 1000;

    private ProgressDialog progressDialog;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(SigninActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }else if(account != null){
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            updateUI(account);
        }else{
            Toast.makeText(this, "Welcome to Profixx", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        mAuth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");

        email = findViewById(R.id.emailtxt);
        password = findViewById(R.id.password_txt);
        login = findViewById(R.id.btnSignIn);
        progressBar = findViewById(R.id.progressBar);
        signup = findViewById(R.id.txtSignup);
        googleBtn = findViewById(R.id.btnGoogle);
        businessLogin = findViewById(R.id.btnBusiness);
        emailLayout = findViewById(R.id.emailLayout);
        forgotPassword = findViewById(R.id.forgotPassword);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        gsc = GoogleSignIn.getClient(this, gso);

        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.btnGoogle) {
                    signIn();
                }
            }
        });

        login.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String emailtxt = String.valueOf(email.getText());
            String passwordtxt = String.valueOf(password.getText());
            if (TextUtils.isEmpty(emailtxt)) {
                Toast.makeText(SigninActivity.this, "Enter Email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(passwordtxt)) {
                Toast.makeText(SigninActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(emailtxt) && TextUtils.isEmpty(passwordtxt)) {
                Toast.makeText(SigninActivity.this, "Fill in your details", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(emailtxt, passwordtxt)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(SigninActivity.this, "Authentication successful.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SigninActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(SigninActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        signup.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
            startActivity(intent);
            finish();
        });

        businessLogin.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), BusinessLoginActivity.class);
            startActivity(intent);
            finish();
        });

        forgotPassword.setOnClickListener(v -> {

            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Logging in...");
            progressDialog.setCancelable(false);
            String remail = email.getText().toString().trim();

            if (remail.isEmpty()) {
                emailLayout.setError("Enter your email first");
                email.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(remail).matches()) {
                emailLayout.setError("Please enter a valid email");
                emailLayout.requestFocus();
                return;
            }

            progressDialog.setMessage("Sending reset email...");
            progressDialog.show();

            mAuth.sendPasswordResetEmail(remail)
                    .addOnCompleteListener(task -> {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(SigninActivity.this,
                                    "Password reset email sent",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(SigninActivity.this,
                                    "Failed to send reset email. Please try again.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    private void updateUI(GoogleSignInAccount account) {
        Intent intent = new Intent(SigninActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
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
                String photoUrl = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : "";

                // Check if the user already exists in the database
                checkAndSaveUserData(userId, username, email, photoUrl);

                Toast.makeText(this, "Signed in successfully", Toast.LENGTH_SHORT).show();
                updateUI(account);
            }
        } catch (ApiException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            updateUI(null);
        }
    }

    private void checkAndSaveUserData(String userId, String username, String email, String photoUrl) {
        DatabaseReference userRef = usersRef.child(userId);

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
                Log.e("SigninActivity", "Error checking user existence", task.getException());
                Toast.makeText(getApplicationContext(),
                        "Error checking user data", Toast.LENGTH_SHORT).show();
            }
        });
    }


}