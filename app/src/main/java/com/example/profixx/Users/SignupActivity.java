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

public class SignupActivity extends BaseActivity {
    TextInputEditText email, password, conPassword, username, phone;
    Button register,googleBtn;
    FirebaseAuth mAuth;
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
        if(currentUser != null){
            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }else if(account != null){
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
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
        email = findViewById(R.id.emailtxt);
        password = findViewById(R.id.password_txt);
        conPassword = findViewById(R.id.confirm_password);
        username = findViewById(R.id.username_txt);
        phone = findViewById(R.id.phone_num);
        register = findViewById(R.id.btnSignUp);
        progressBar = findViewById(R.id.progressBar);
        signin = findViewById(R.id.txtLogin);
        googleBtn = findViewById(R.id.btnGoogle);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
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

        register.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String usernametxt = String.valueOf(username.getText());
                String phonetxt = String.valueOf(phone.getText());
                String emailtxt = String.valueOf(email.getText());
                String passwordtxt = String.valueOf(password.getText());
                String conpasswordtxt = String.valueOf(conPassword.getText());

                if (TextUtils.isEmpty(usernametxt)) {
                    Toast.makeText(SignupActivity.this, "Enter Username", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
                else if (TextUtils.isEmpty(phonetxt)) {
                    Toast.makeText(SignupActivity.this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
                else if (TextUtils.isEmpty(emailtxt)) {
                    Toast.makeText(SignupActivity.this, "Enter Email", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
                else if (TextUtils.isEmpty(passwordtxt)) {
                    Toast.makeText(SignupActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
                else if (TextUtils.isEmpty(conpasswordtxt)) {
                    Toast.makeText(SignupActivity.this, "Confirm your Password", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
                else if (TextUtils.isEmpty(usernametxt) && TextUtils.isEmpty(emailtxt) && TextUtils.isEmpty(phonetxt) && TextUtils.isEmpty(passwordtxt) && TextUtils.isEmpty(conpasswordtxt)) {
                    Toast.makeText(SignupActivity.this, "Fill in your details", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
                else if (!passwordtxt.equals(conpasswordtxt)) {
                    Toast.makeText(SignupActivity.this, "Password does not match", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }else {

                    mAuth.createUserWithEmailAndPassword(emailtxt, passwordtxt)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SignupActivity.this, "Registration Successful",
                                                Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(SignupActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, SigninActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void updateUI(GoogleSignInAccount account) {
        Intent intent = new Intent(SignupActivity.this, MainActivity.class);
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

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            Toast.makeText(this, "Signed in successfully", Toast.LENGTH_SHORT).show();
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            updateUI(null);
        }
    }

}