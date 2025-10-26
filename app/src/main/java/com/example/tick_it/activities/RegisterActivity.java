package com.example.tick_it.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.tick_it.R;
import com.example.tick_it.utils.FirebaseUtil;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPassword, etDriverLicense, etLicensePlate;
    private AutoCompleteTextView actVehicleType;
    private TextInputLayout tilDriverLicense;
    private RadioGroup radioGroupRole;
    private Button btnRegister;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeViews();
        setupClickListeners();
        setupRoleChangeListener();

        mAuth = FirebaseUtil.getAuth();
        db = FirebaseUtil.getFirestore();
    }

    private void initializeViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etDriverLicense = findViewById(R.id.etDriverLicense);
        etLicensePlate = findViewById(R.id.etLicensePlate);
        actVehicleType = findViewById(R.id.actVehicleType);
        tilDriverLicense = findViewById(R.id.tilDriverLicense);
        radioGroupRole = findViewById(R.id.radioGroupRole);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> attemptRegistration());
    }

    private void setupRoleChangeListener() {
        radioGroupRole.setOnCheckedChangeListener((group, checkedId) -> {
            updateDriverLicenseFieldRequirement();
        });
        // Set initial state
        updateDriverLicenseFieldRequirement();
    }

    private void updateDriverLicenseFieldRequirement() {
        String role = getSelectedRole();
        if ("motorist".equals(role)) {
            // Required for motorists
            tilDriverLicense.setHint("Driver's License Number *");
            etDriverLicense.setError(null);
        } else {
            // Optional for enforcers
            tilDriverLicense.setHint("Driver's License Number (Optional)");
            etDriverLicense.setError(null);
        }
    }

    private void attemptRegistration() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String driverLicense = etDriverLicense.getText().toString().trim().toUpperCase();
        String licensePlate = etLicensePlate.getText().toString().trim().toUpperCase();
        String vehicleType = actVehicleType.getText().toString().trim();
        String role = getSelectedRole();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Full name is required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        // Driver's license validation based on role
        if ("motorist".equals(role)) {
            if (TextUtils.isEmpty(driverLicense)) {
                etDriverLicense.setError("Driver's license number is required for motorists");
                return;
            }
            if (driverLicense.length() < 5) {
                etDriverLicense.setError("Driver's license number must be at least 5 characters");
                return;
            }
        }

        // Vehicle information validation for motorists
        if ("motorist".equals(role)) {
            if (TextUtils.isEmpty(licensePlate)) {
                etLicensePlate.setError("License plate is required for motorists");
                return;
            }
            if (TextUtils.isEmpty(vehicleType)) {
                actVehicleType.setError("Vehicle type is required for motorists");
                return;
            }
        }

        showProgress(true);
        registerUser(name, email, password, driverLicense, licensePlate, vehicleType, role);
    }

    private String getSelectedRole() {
        int selectedId = radioGroupRole.getCheckedRadioButtonId();
        RadioButton radioButton = findViewById(selectedId);
        return radioButton.getText().toString().toLowerCase();
    }

    private void registerUser(String name, String email, String password, String driverLicense,
                              String licensePlate, String vehicleType, String role) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // User created successfully in Authentication
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            // Save user data to Firestore with the UID as document ID
                            saveUserDataToFirestore(userId, name, email, driverLicense, licensePlate, vehicleType, role);
                        } else {
                            showProgress(false);
                            Toast.makeText(RegisterActivity.this,
                                    "Error: User authentication failed",
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        showProgress(false);
                        Toast.makeText(RegisterActivity.this,
                                "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserDataToFirestore(String userId, String name, String email, String driverLicense,
                                         String licensePlate, String vehicleType, String role) {
        Map<String, Object> user = new HashMap<>();
        user.put("uid", userId); // Store the UID as a field in the document
        user.put("name", name);
        user.put("email", email);
        user.put("role", role);
        user.put("createdAt", System.currentTimeMillis());

        // Only store driver license if provided (required for motorists, optional for enforcers)
        if (!TextUtils.isEmpty(driverLicense)) {
            user.put("driverLicenseNumber", driverLicense);
        } else {
            // For motorists without driver license, set a placeholder (shouldn't happen due to validation)
            if ("motorist".equals(role)) {
                user.put("driverLicenseNumber", "PENDING_SETUP");
            }
        }

        // Store vehicle information for motorists
        if ("motorist".equals(role)) {
            user.put("licensePlate", licensePlate);
            user.put("vehicleType", vehicleType);
        }

        // Save to Firestore using the UID as the document ID
        db.collection("users").document(userId)
                .set(user)
                .addOnCompleteListener(task -> {
                    showProgress(false);

                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this,
                                "Registration successful!", Toast.LENGTH_SHORT).show();

                        // Show warning if motorist registered without driver license
                        if ("motorist".equals(role) && TextUtils.isEmpty(driverLicense)) {
                            Toast.makeText(RegisterActivity.this,
                                    "Warning: You need to add your driver's license number to view violations",
                                    Toast.LENGTH_LONG).show();
                        }

                        // Redirect to appropriate dashboard
                        Intent intent;
                        if ("enforcer".equals(role)) {
                            intent = new Intent(RegisterActivity.this, EnforcerMainActivity.class);
                        } else {
                            intent = new Intent(RegisterActivity.this, MotoristMainActivity.class);
                        }
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                "Failed to save user data: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();

                        // Optional: Delete the user from Authentication if Firestore save fails
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            firebaseUser.delete();
                        }
                    }
                });
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? android.view.View.VISIBLE : android.view.View.GONE);
        btnRegister.setEnabled(!show);
    }
}