package com.example.tick_it.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.tick_it.R;
import com.example.tick_it.utils.FirebaseUtil;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupClickListeners();

        mAuth = FirebaseUtil.getAuth();
        db = FirebaseUtil.getFirestore();

        // Check if user is already logged in
        if (mAuth.getCurrentUser() != null) {
            checkUserRoleAndRedirect(mAuth.getCurrentUser().getUid());
        }
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());
        tvRegister.setOnClickListener(v -> navigateToRegister());
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

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

        showProgress(true);
        loginUser(email, password);
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login successful
                        String userId = mAuth.getCurrentUser().getUid();

                        // Check user role and redirect accordingly
                        checkUserRoleAndRedirect(userId);
                    } else {
                        // Login failed
                        Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserRoleAndRedirect(String userId) {
        FirebaseFirestore db = FirebaseUtil.getFirestore();

        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String userRole = document.getString("role");
                            redirectToAppropriateActivity(userRole);
                        } else {
                            // Default to motorist if role not found
                            redirectToAppropriateActivity("motorist");
                        }
                    } else {
                        // Default to motorist if error
                        redirectToAppropriateActivity("motorist");
                    }
                });
    }

    private void redirectToAppropriateActivity(String userRole) {
        Intent intent;

        if ("enforcer".equalsIgnoreCase(userRole)) {
            intent = new Intent(LoginActivity.this, EnforcerMainActivity.class);
            Toast.makeText(this, "Welcome Enforcer!", Toast.LENGTH_SHORT).show();
        } else {
            // Default to motorist
            intent = new Intent(LoginActivity.this, MotoristMainActivity.class);
            Toast.makeText(this, "Welcome Motorist!", Toast.LENGTH_SHORT).show();
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void redirectToDashboard(String role) {
        Intent intent;
        if ("enforcer".equals(role)) {
            intent = new Intent(LoginActivity.this, EnforcerMainActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, MotoristMainActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToRegister() {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? android.view.View.VISIBLE : android.view.View.GONE);
        btnLogin.setEnabled(!show);
    }
}