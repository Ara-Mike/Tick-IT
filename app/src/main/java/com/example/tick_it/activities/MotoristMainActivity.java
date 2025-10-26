package com.example.tick_it.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.tick_it.R;
import com.example.tick_it.utils.FirebaseUtil;

public class MotoristMainActivity extends AppCompatActivity {

    private Button btnViewViolations, btnPayFines, btnDisputeViolations, btnNotifications;
    private ImageView btnNotificationsBell;
    private TextView txtWelcome, txtPendingFines, txtPendingDisputes;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motorist_main);

        initializeViews();
        setupClickListeners();

        mAuth = FirebaseUtil.getAuth();
        db = FirebaseUtil.getFirestore();

        loadUserData();
        loadViolationsStats();
    }

    private void initializeViews() {
        // Buttons
        btnViewViolations = findViewById(R.id.btnViewViolations);
        btnPayFines = findViewById(R.id.btnPayFines);
        btnDisputeViolations = findViewById(R.id.btnDisputeViolations);
        btnNotifications = findViewById(R.id.btnNotifications);

        // Header elements
        btnNotificationsBell = findViewById(R.id.btnNotificationsBell);
        txtWelcome = findViewById(R.id.txtWelcome);
        txtPendingFines = findViewById(R.id.txtPendingFines);
        txtPendingDisputes = findViewById(R.id.txtPendingDisputes);
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();

            db.collection("users").document(userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Get username - adjust field name based on your database structure
                                String userName = document.getString("name");
                                if (userName == null || userName.isEmpty()) {
                                    userName = document.getString("username");
                                }
                                if (userName == null || userName.isEmpty()) {
                                    userName = "Motorist"; // Fallback
                                }

                                txtWelcome.setText("Welcome back, " + userName + "!");
                            } else {
                                txtWelcome.setText("Welcome back, Motorist!");
                            }
                        } else {
                            txtWelcome.setText("Welcome back, Motorist!");
                        }
                    });
        } else {
            txtWelcome.setText("Welcome back, Motorist!");
        }
    }

    private void loadViolationsStats() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();

            // Get pending fines count and amount
            db.collection("violations")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("status", "pending")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            int pendingFinesCount = task.getResult().size();
                            double totalPendingAmount = 0;

                            for (DocumentSnapshot document : task.getResult().getDocuments()) {
                                Double fineAmount = document.getDouble("fineAmount");
                                if (fineAmount != null) {
                                    totalPendingAmount += fineAmount;
                                }
                            }

                            // Update pending fines display
                            if (pendingFinesCount > 0) {
                                txtPendingFines.setText(pendingFinesCount + " pending fines");
                            } else {
                                txtPendingFines.setText("No pending fines");
                            }

                            // Get pending disputes count
                            loadPendingDisputes(userId);
                        } else {
                            txtPendingFines.setText("Error loading fines");
                            txtPendingDisputes.setText("Error loading disputes");
                        }
                    });
        }
    }

    private void loadPendingDisputes(String userId) {
        db.collection("disputes") // Adjust collection name based on your structure
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int pendingDisputesCount = task.getResult().size();

                        if (pendingDisputesCount > 0) {
                            txtPendingDisputes.setText(pendingDisputesCount + " pending disputes");
                        } else {
                            txtPendingDisputes.setText("No pending disputes");
                        }
                    } else {
                        txtPendingDisputes.setText("Error loading disputes");
                    }
                });
    }

    private void setupClickListeners() {
        btnViewViolations.setOnClickListener(v -> {
            startActivity(new Intent(MotoristMainActivity.this, ViolationsListActivity.class));
        });

        btnPayFines.setOnClickListener(v ->
                Toast.makeText(this, "Pay Fines - Feature coming soon", Toast.LENGTH_SHORT).show());

        btnDisputeViolations.setOnClickListener(v ->
                Toast.makeText(this, "Dispute Violations - Feature coming soon", Toast.LENGTH_SHORT).show());

        btnNotifications.setOnClickListener(v ->
                Toast.makeText(this, "Notifications - Feature coming soon", Toast.LENGTH_SHORT).show());

        // Notification bell click listener
        btnNotificationsBell.setOnClickListener(v ->
                Toast.makeText(this, "Notifications Bell Clicked", Toast.LENGTH_SHORT).show());
    }

    public void logout(android.view.View view) {
        mAuth.signOut();
        Intent intent = new Intent(MotoristMainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}