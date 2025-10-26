package com.example.tick_it.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.tick_it.adapters.ViolationsAdapter;
import com.example.tick_it.models.Violation;
import com.example.tick_it.R;
import com.example.tick_it.utils.FirebaseUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.example.tick_it.activities.ViolationDetailsDialog;

public class ViolationsListActivity extends AppCompatActivity {

    private RecyclerView rvViolations;
    private ProgressBar progressBar;
    private View emptyState;
    private TextView tvTotalViolations, tvPendingFines;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageButton btnBack;

    private ViolationsAdapter adapter;
    private List<Violation> violations = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration violationsListener;

    private static final String TAG = "ViolationsListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_violations_list);

        Log.d(TAG, "=== ViolationsListActivity Started ===");

        initializeViews();
        setupRecyclerView();
        setupSwipeRefresh();
        setupBackButton();

        // Initialize Firebase
        mAuth = FirebaseUtil.getAuth();
        db = FirebaseUtil.getFirestore();

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setupRealTimeListener();
    }

    private void initializeViews() {
        rvViolations = findViewById(R.id.rvViolations);
        progressBar = findViewById(R.id.progressBar);
        emptyState = findViewById(R.id.emptyState);
        tvTotalViolations = findViewById(R.id.tvTotalViolations);
        tvPendingFines = findViewById(R.id.tvPendingFines);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        btnBack = findViewById(R.id.btnBack);

        tvTotalViolations.setText("0");
        tvPendingFines.setText("$0.00");
    }

    private void setupRecyclerView() {
        adapter = new ViolationsAdapter(violations);
        adapter.setOnViolationClickListener(new ViolationsAdapter.OnViolationClickListener() {
            @Override
            public void onViolationClick(Violation violation) {
                showViolationDetails(violation);
            }
        });
        rvViolations.setLayoutManager(new LinearLayoutManager(this));
        rvViolations.setAdapter(adapter);
    }

    private void showViolationDetails(Violation violation) {
        ViolationDetailsDialog dialog = new ViolationDetailsDialog(this, violation);
        dialog.show();
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "Pull to refresh triggered");
                loadViolations();
            }
        });
    }

    private void setupBackButton() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setupRealTimeListener() {
        Log.d(TAG, "Setting up real-time Firestore listener");
        showLoading(true);

        // Get current user's driver license number from their profile
        String currentUserId = mAuth.getCurrentUser().getUid();

        // First, get the user's driver license number from their profile
        db.collection("users").document(currentUserId).get()
                .addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful() && userTask.getResult().exists()) {
                        String driverLicenseNumber = userTask.getResult().getString("driverLicenseNumber");

                        if (driverLicenseNumber != null && !driverLicenseNumber.isEmpty()) {
                            Log.d(TAG, "Searching violations for driver license: " + driverLicenseNumber);

                            // Query violations by driver's license number
                            violationsListener = db.collection("violations")
                                    .whereEqualTo("driverLicenseNumber", driverLicenseNumber)
                                    .orderBy("issueDate", Query.Direction.DESCENDING)
                                    .addSnapshotListener((querySnapshot, error) -> {
                                        showLoading(false);
                                        swipeRefreshLayout.setRefreshing(false);

                                        if (error != null) {
                                            Log.e(TAG, "Listen failed: " + error);
                                            Toast.makeText(this, "Error loading violations: " + error.getMessage(), Toast.LENGTH_LONG).show();
                                            showEmptyState();
                                            return;
                                        }

                                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                            List<DocumentSnapshot> documents = querySnapshot.getDocuments();
                                            processViolationsSnapshot(documents);
                                        } else {
                                            Log.d(TAG, "No violations found for driver license: " + driverLicenseNumber);
                                            violations.clear();
                                            updateUI(0, 0.0);
                                            adapter.updateData(violations);
                                        }
                                    });
                        } else {
                            Log.e(TAG, "User does not have a driver license number in profile");
                            showLoading(false);
                            Toast.makeText(this, "Driver license number not found in your profile", Toast.LENGTH_LONG).show();
                            showEmptyState();
                        }
                    } else {
                        Log.e(TAG, "Failed to read user profile");
                        showLoading(false);
                        Toast.makeText(this, "Error reading user profile", Toast.LENGTH_LONG).show();
                        showEmptyState();
                    }
                });
    }

    private void setupTimeout() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (progressBar.getVisibility() == View.VISIBLE) {
                Log.w(TAG, "Loading timeout - forcing UI update");
                showLoading(false);
                if (violations.isEmpty()) {
                    showEmptyState();
                }
            }
        }, 10000); // 10 second timeout
    }

    private void queryViolationsForUser(String userId) {
        // Get the user's driver license number first
        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        String driverLicenseNumber = task.getResult().getString("driverLicenseNumber");

                        if (driverLicenseNumber != null && !driverLicenseNumber.isEmpty()) {
                            Log.d(TAG, "Searching violations for user ID: " + userId + " and driver license: " + driverLicenseNumber);

                            // Query by userId (preferred) OR driverLicenseNumber as fallback
                            violationsListener = db.collection("violations")
                                    .whereIn("userId", Arrays.asList(userId, driverLicenseNumber))
                                    .orderBy("issueDate", Query.Direction.DESCENDING)
                                    .addSnapshotListener((querySnapshot, error) -> {
                                        // ... existing listener code
                                    });
                        } else {
                            // Fallback: query only by userId
                            Log.w(TAG, "No driver license in profile, querying by user ID only");
                            violationsListener = db.collection("violations")
                                    .whereEqualTo("userId", userId)
                                    .orderBy("issueDate", Query.Direction.DESCENDING)
                                    .addSnapshotListener((querySnapshot, error) -> {
                                        // ... existing listener code
                                    });
                        }
                    } else {
                        Log.e(TAG, "Failed to read user profile");
                        showLoading(false);
                        Toast.makeText(this, "Error reading user profile", Toast.LENGTH_LONG).show();
                        showEmptyState();
                    }
                });
    }

    private void processViolationsSnapshot(List<DocumentSnapshot> documents) {
        violations.clear();
        double totalPendingFines = 0;
        int pendingViolationsCount = 0;

        Log.d(TAG, "Processing " + documents.size() + " violations");

        for (DocumentSnapshot document : documents) {
            try {
                Violation violation = document.toObject(Violation.class);
                if (violation != null) {
                    violation.setId(document.getId());
                    violations.add(violation);

                    // Check if violation is pending and has a valid fine amount
                    if ("pending".equalsIgnoreCase(violation.getStatus())) {
                        double fineAmount = violation.getFineAmount();
                        if (fineAmount > 0) {
                            totalPendingFines += fineAmount;
                            pendingViolationsCount++;
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing violation: " + e.getMessage(), e);
            }
        }

        updateUI(violations.size(), totalPendingFines);
        adapter.updateData(violations);

        Log.d(TAG, "Updated UI - Total: " + violations.size() +
                ", Pending: " + pendingViolationsCount +
                ", Total Pending Amount: $" + totalPendingFines);
    }

    private void loadViolations() {
        // Manual refresh - real-time listener will handle updates
        showLoading(true);
        swipeRefreshLayout.setRefreshing(true);

        // Force a refresh by re-querying
        if (violationsListener != null) {
            violationsListener.remove();
        }
        setupRealTimeListener();
    }

    private void updateUI(int totalViolations, double totalPendingFines) {
        tvTotalViolations.setText(String.valueOf(totalViolations));
        tvPendingFines.setText(String.format("$%.2f", totalPendingFines));

        if (totalViolations == 0) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            rvViolations.setVisibility(View.GONE);
            emptyState.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        rvViolations.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        emptyState.setVisibility(View.GONE);
        rvViolations.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (violationsListener != null) {
            violationsListener.remove();
        }
    }
}