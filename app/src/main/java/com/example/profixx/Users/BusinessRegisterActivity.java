package com.example.profixx.Users;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.profixx.Activity.BaseActivity;
import com.example.profixx.BussinessActivity.DashboardActivity;
import com.example.profixx.databinding.ActivityBusinessRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class BusinessRegisterActivity extends BaseActivity {
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference usersRef;
    ActivityBusinessRegisterBinding binding;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri imageUri;
    private StorageReference storageReference;

    @Override
    public void onStart() {
        super.onStart();
        // Ensure mAuth is initialized before calling getCurrentUser()
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBusinessRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initVariables();

        initUploadImage();

        binding.btnSignUp.setOnClickListener(v -> {
            binding.progressBar.setVisibility(View.VISIBLE);


            String usernameTxt = String.valueOf(binding.username.getText());
            String businessNameTxt = String.valueOf(binding.businessName.getText());
            String businessEmailTxt = String.valueOf(binding.businessEmail.getText());
            String businessPhoneTxt = String.valueOf(binding.phoneNum.getText());
            String businessDescTxt = String.valueOf(binding.businessDesc.getText());
            String businessEmailAddressTxt = String.valueOf(binding.businessEmail.getText());
            String businessPasswordTxt = String.valueOf(binding.passwordBusiness.getText());
            String businessConfirmPasswordTxt = String.valueOf(binding.conBusinessPassword.getText());

            if (validateInputs(usernameTxt, businessNameTxt, businessPhoneTxt, businessDescTxt, businessEmailTxt, businessPasswordTxt, businessConfirmPasswordTxt)) {
                mAuth.createUserWithEmailAndPassword(businessEmailTxt, businessPasswordTxt)
                        .addOnCompleteListener(task -> {
                            binding.progressBar.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    uploadLogoAndSaveData(user.getUid(), usernameTxt, businessNameTxt, businessEmailTxt, businessPhoneTxt, businessDescTxt);
                                }
                                Toast.makeText(getApplicationContext(), "Registration Successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }else {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
        binding.txtLogin.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), BusinessLoginActivity.class));
            finish();
        });

        binding.btnUser.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), SigninActivity.class));
            finish();
        });
    }

    private void initUploadImage() {

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        binding.businessLogo.setImageURI(imageUri); // Set the selected image
                    }
                }
        );

        binding.uploadLogoBtn.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            imagePickerLauncher.launch(Intent.createChooser(intent, "Select Image"));
        });


    }

    private void initVariables() {
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("businesses");
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    private void uploadLogoAndSaveData(String uid, String username, String businessName, String businessEmail,
                                       String phoneNumber, String businessDesc) {
        StorageReference logoRef = storageReference.child("seller_logos/" + uid + ".jpg");

        logoRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    logoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String logoUrl = uri.toString();
                        saveUserDataToDatabase(uid, username, businessName, businessEmail, phoneNumber,
                                businessDesc, logoUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(),
                            "Failed to upload logo: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void saveUserDataToDatabase(String uid, String username, String businessName, String businessEmail, String phoneNumber, String businessDesc, String logoUrl) {
        HashMap<String, Object> userData = new HashMap<>();
        userData.put("logo", logoUrl);
        userData.put("username", username);
        userData.put("businessName", businessName);
        userData.put("businessEmail", businessEmail);
        userData.put("businessPhone", phoneNumber);
        userData.put("businessDesc", businessDesc);

        usersRef.child(uid).setValue(userData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("SignupActivity", "User data saved to database.");
            } else {
                Log.e("SignupActivity", "Failed to save user data.", task.getException());
            }
        });
    }


    private boolean validateInputs(String username, String businessName,  String businessPhone, String businessDesc, String businessEmail, String businessPassword, String businessConfirmPassword) {
        if (TextUtils.isEmpty(username)) {
            binding.username.setError("Username is required");
            binding.username.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(businessName)) {
            binding.businessName.setError("Business Name is required");
            binding.businessName.requestFocus();
            return false;
        } else if (imageUri == null) {
            Toast.makeText(getApplicationContext(), "Please upload a logo", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (TextUtils.isEmpty(businessPhone)) {
            binding.phoneNum.setError("Phone Number is required");
            binding.phoneNum.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(businessDesc)) {
            binding.businessDesc.setError("Business Description is required");
            binding.businessDesc.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(businessEmail)) {
            binding.businessEmail.setError("Business Email is required");
            binding.businessEmail.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(businessPassword)) {
            binding.passwordBusiness.setError("Password is required");
            binding.passwordBusiness.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(businessConfirmPassword)) {
            binding.conBusinessPassword.setError("Confirm Password is required");
            binding.conBusinessPassword.requestFocus();
            return false;
        } else if (!businessPassword.equals(businessConfirmPassword)) {
            binding.conBusinessPassword.setError("Passwords do not match");
            binding.conBusinessPassword.requestFocus();
            return false;
        }

        return true;
    }
}