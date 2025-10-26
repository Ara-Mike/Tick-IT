package com.example.tick_it.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tick_it.R;
import com.example.tick_it.models.ViolationType;
import com.example.tick_it.utils.FirebaseUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IssueTicketActivity extends AppCompatActivity {

    private static final String TAG = "IssueTicketActivity";

    // UI Elements
    private EditText etLicensePlate, etViolatorName, etDriverLicense, etViolatorEmail, etViolatorPhone;
    private Spinner spinnerVehicleType, spinnerViolationType;
    private EditText etFineAmount, etLocation, etDescription;
    private Button btnCancel, btnIssueTicket;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Data
    private List<ViolationType> violationTypes = new ArrayList<>();
    private Map<String, Double> violationFines = new HashMap<>();
    private Map<String, String> violationIds = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_ticket);

        Log.d(TAG, "onCreate: Activity started");

        try {
            initializeViews();
            setupSpinners();
            setupFirebase();
            loadViolationTypes();
            setupClickListeners();
            Log.d(TAG, "onCreate: Setup completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "onCreate: Error during setup", e);
            Toast.makeText(this, "Error initializing activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initializeViews() {
        Log.d(TAG, "initializeViews: Starting view initialization");

        try {
            // Violator Information
            etLicensePlate = findViewById(R.id.etLicensePlate);
            spinnerVehicleType = findViewById(R.id.spinnerVehicleType);
            etViolatorName = findViewById(R.id.etViolatorName);
            etDriverLicense = findViewById(R.id.etDriverLicense);
            etViolatorEmail = findViewById(R.id.etViolatorEmail);
            etViolatorPhone = findViewById(R.id.etViolatorPhone);

            // Violation Details
            spinnerViolationType = findViewById(R.id.spinnerViolationType);
            etFineAmount = findViewById(R.id.etFineAmount);
            etLocation = findViewById(R.id.etLocation);
            etDescription = findViewById(R.id.etDescription);

            // Buttons
            btnCancel = findViewById(R.id.btnCancel);
            btnIssueTicket = findViewById(R.id.btnIssueTicket);

            // Progress Bar
            progressBar = findViewById(R.id.progressBar);

            Log.d(TAG, "initializeViews: All views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "initializeViews: Error finding views", e);
            throw new RuntimeException("Error initializing views", e);
        }
    }

    private void setupSpinners() {
        // Vehicle types
        String[] vehicleTypes = {"Select Vehicle Type", "Motorcycle", "Car", "SUV", "Truck", "Bus", "Van", "Other"};
        ArrayAdapter<String> vehicleAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, vehicleTypes);
        vehicleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVehicleType.setAdapter(vehicleAdapter);

        // Violation types (initially empty, will be populated from Firestore)
        ArrayAdapter<String> violationAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>());
        violationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerViolationType.setAdapter(violationAdapter);
    }

    private void setupFirebase() {
        mAuth = FirebaseUtil.getAuth();
        db = FirebaseUtil.getFirestore();
    }

    private void loadViolationTypes() {
        db.collection("violationTypes")
                .whereEqualTo("isActive", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        violationTypes.clear();
                        violationFines.clear();
                        violationIds.clear();
                        List<String> violationNames = new ArrayList<>();

                        // Add default option
                        violationNames.add("Select Violation Type");

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                ViolationType violationType = document.toObject(ViolationType.class);
                                violationType.setId(document.getId());
                                violationTypes.add(violationType);

                                // Create display text: "Violation Name - $FineAmount"
                                String displayText = violationType.getViolationName() + " - $" + violationType.getFineAmount();
                                violationNames.add(displayText);

                                // Store fine amounts and document IDs for auto-calculation
                                violationFines.put(displayText, violationType.getFineAmount());
                                violationIds.put(displayText, document.getId());

                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing violation type: " + e.getMessage());
                            }
                        }

                        // Update adapter
                        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerViolationType.getAdapter();
                        adapter.clear();
                        adapter.addAll(violationNames);
                        adapter.notifyDataSetChanged();

                        Log.d(TAG, "Loaded " + violationTypes.size() + " violation types");

                        if (violationTypes.isEmpty()) {
                            Toast.makeText(this, "No violation types found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Error loading violation types: ", task.getException());
                        Toast.makeText(this, "Error loading violation types", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setupClickListeners() {
        // Violation type selection listener
        spinnerViolationType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Skip the first "Select" item
                    String selectedViolationDisplay = (String) parent.getItemAtPosition(position);
                    Double fineAmount = violationFines.get(selectedViolationDisplay);

                    if (fineAmount != null) {
                        etFineAmount.setText(String.format("$%.2f", fineAmount));
                    }
                } else {
                    etFineAmount.setText("$0.00");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                etFineAmount.setText("$0.00");
            }
        });

        // Cancel button
        btnCancel.setOnClickListener(v -> {
            finish();
        });

        // Issue Ticket button
        btnIssueTicket.setOnClickListener(v -> {
            if (validateForm()) {
                issueTicket();
            }
        });
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Clear previous errors by resetting backgrounds
        etLicensePlate.setBackgroundResource(R.drawable.edit_text_background);
        etViolatorName.setBackgroundResource(R.drawable.edit_text_background);
        etDriverLicense.setBackgroundResource(R.drawable.edit_text_background);
        etLocation.setBackgroundResource(R.drawable.edit_text_background);

        // Basic validation
        if (etLicensePlate.getText().toString().trim().isEmpty()) {
            etLicensePlate.setBackgroundResource(R.drawable.edit_text_error_background);
            Toast.makeText(this, "License plate is required", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (spinnerVehicleType.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Vehicle type is required", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (etViolatorName.getText().toString().trim().isEmpty()) {
            etViolatorName.setBackgroundResource(R.drawable.edit_text_error_background);
            Toast.makeText(this, "Violator name is required", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (etDriverLicense.getText().toString().trim().isEmpty()) {
            etDriverLicense.setBackgroundResource(R.drawable.edit_text_error_background);
            Toast.makeText(this, "Driver's license number is required", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (spinnerViolationType.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Violation type is required", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (etLocation.getText().toString().trim().isEmpty()) {
            etLocation.setBackgroundResource(R.drawable.edit_text_error_background);
            Toast.makeText(this, "Location is required", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private void issueTicket() {
        showProgress(true);

        // Get current user (enforcer)
        String currentUserId = mAuth.getCurrentUser().getUid();

        // Calculate due date (30 days from now)
        Date issueDate = new Date();
        Date dueDate = new Date(issueDate.getTime() + (30L * 24 * 60 * 60 * 1000)); // 30 days

        // Get selected violation type and fine amount
        int violationPosition = spinnerViolationType.getSelectedItemPosition();
        if (violationPosition == 0) {
            Toast.makeText(this, "Please select a violation type", Toast.LENGTH_SHORT).show();
            showProgress(false);
            return;
        }

        String selectedViolationDisplay = (String) spinnerViolationType.getSelectedItem();
        Double fineAmount = violationFines.get(selectedViolationDisplay);
        String violationTypeId = violationIds.get(selectedViolationDisplay);

        // Extract just the violation name from the display text
        String violationName = selectedViolationDisplay.split(" - \\$")[0];

        if (violationTypeId == null || fineAmount == null) {
            Toast.makeText(this, "Invalid violation type selected", Toast.LENGTH_SHORT).show();
            showProgress(false);
            return;
        }

        // Get violator information
        String violatorName = etViolatorName.getText().toString().trim();
        String driverLicenseNumber = etDriverLicense.getText().toString().trim().toUpperCase();
        String licensePlate = etLicensePlate.getText().toString().trim().toUpperCase();
        String vehicleType = spinnerVehicleType.getSelectedItem().toString();

        // First, try to find the user by driver license number
        findUserByDriverLicense(driverLicenseNumber, new OnUserFoundListener() {
            @Override
            public void onUserFound(String userId, String actualUserName, String userEmail) {
                // User found - create violation with userId
                createViolationDocument(
                        currentUserId, userId, actualUserName, userEmail,
                        driverLicenseNumber, licensePlate, vehicleType,
                        violationTypeId, violationName, fineAmount,
                        issueDate, dueDate,
                        true // userExists = true
                );
            }

            @Override
            public void onUserNotFound() {
                // No user found - create violation without userId
                createViolationDocument(
                        currentUserId, null, violatorName, null,
                        driverLicenseNumber, licensePlate, vehicleType,
                        violationTypeId, violationName, fineAmount,
                        issueDate, dueDate,
                        false // userExists = false
                );
            }

            @Override
            public void onError(String errorMessage) {
                showProgress(false);
                Toast.makeText(IssueTicketActivity.this,
                        "Error searching for user: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    // Interface for the callback
    interface OnUserFoundListener {
        void onUserFound(String userId, String userName, String userEmail);
        void onUserNotFound();
        void onError(String errorMessage);
    }

    private void findUserByDriverLicense(String driverLicenseNumber, OnUserFoundListener listener) {
        Log.d(TAG, "Searching for user with driver license: " + driverLicenseNumber);

        // First, search in Firestore users collection
        db.collection("users")
                .whereEqualTo("driverLicenseNumber", driverLicenseNumber)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // User found in Firestore
                        DocumentSnapshot userDoc = task.getResult().getDocuments().get(0);
                        String userId = userDoc.getId();
                        String userName = userDoc.getString("name");
                        String userEmail = userDoc.getString("email");

                        Log.d(TAG, "Found user in Firestore: " + userName + " (ID: " + userId + ")");
                        listener.onUserFound(userId, userName, userEmail);
                    } else {
                        // No user found in Firestore, try to find by email in Firebase Auth
                        Log.d(TAG, "No user found in Firestore, searching Firebase Auth by email...");
                        findUserInAuthByEmail(driverLicenseNumber, listener);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error searching for user by driver license in Firestore", e);
                    listener.onError(e.getMessage());
                });
    }

    private void findUserInAuthByEmail(String driverLicenseNumber, OnUserFoundListener listener) {
        // Try to find if there's a user with an email that might match the driver license pattern
        // This is a fallback - you might need to adjust this logic based on your user registration flow

        // If the driver license was used as email during registration, try that
        String possibleEmail = driverLicenseNumber + "@example.com"; // Adjust this pattern

        // For now, we'll search in Firestore users collection by name or other fields
        // as a fallback approach
        searchUserByAnyField(driverLicenseNumber, listener);
    }

    private void searchUserByAnyField(String driverLicenseNumber, OnUserFoundListener listener) {
        // Try to find user by name (if driver license number was entered as name by mistake)
        db.collection("users")
                .whereEqualTo("name", driverLicenseNumber)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // User found by name
                        DocumentSnapshot userDoc = task.getResult().getDocuments().get(0);
                        String userId = userDoc.getId();
                        String userName = userDoc.getString("name");
                        String userEmail = userDoc.getString("email");

                        Log.d(TAG, "Found user by name match: " + userName + " (ID: " + userId + ")");
                        listener.onUserFound(userId, userName, userEmail);
                    } else {
                        // Last attempt: get all users and check if any have matching driver license
                        searchAllUsersForDriverLicense(driverLicenseNumber, listener);
                    }
                });
    }

    private void searchAllUsersForDriverLicense(String driverLicenseNumber, OnUserFoundListener listener) {
        // This is a broader search - get all users and check manually
        db.collection("users")
                .whereEqualTo("role", "motorist") // Only search motorists
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot userDoc : task.getResult()) {
                            String userDriverLicense = userDoc.getString("driverLicenseNumber");
                            if (driverLicenseNumber.equalsIgnoreCase(userDriverLicense)) {
                                // Found matching user
                                String userId = userDoc.getId();
                                String userName = userDoc.getString("name");
                                String userEmail = userDoc.getString("email");

                                Log.d(TAG, "Found user in broader search: " + userName + " (ID: " + userId + ")");
                                listener.onUserFound(userId, userName, userEmail);
                                return;
                            }
                        }
                        // No user found after all searches
                        Log.d(TAG, "No user found with driver license: " + driverLicenseNumber + " after all search attempts");
                        listener.onUserNotFound();
                    } else {
                        Log.e(TAG, "Error in broader user search", task.getException());
                        listener.onUserNotFound();
                    }
                });
    }

    private void createViolationDocument(
            String enforcerId, String violatorUserId, String violatorName, String violatorEmail,
            String driverLicenseNumber, String licensePlate, String vehicleType,
            String violationTypeId, String violationName, Double fineAmount,
            Date issueDate, Date dueDate, boolean userExists) {

        // Create violation object
        Map<String, Object> violation = new HashMap<>();

        // Violation Details
        violation.put("violationType", violationTypeId);
        violation.put("violationTypeName", violationName);
        violation.put("fineAmount", fineAmount);
        violation.put("description", etDescription.getText().toString().trim());

        // Violator Information
        violation.put("licensePlate", licensePlate);
        violation.put("vehicleType", vehicleType);
        violation.put("violatorName", violatorName);
        violation.put("driverLicenseNumber", driverLicenseNumber);

        // User Linking (NEW)
        if (userExists && violatorUserId != null) {
            violation.put("userId", violatorUserId); // Link to user account
            violation.put("userEmail", violatorEmail); // Store actual user email
            violation.put("userExists", true);
            Log.d(TAG, "Violation linked to user ID: " + violatorUserId);
        } else {
            violation.put("userId", null);
            violation.put("userExists", false);
            Log.d(TAG, "No user account found for this driver license");
        }

        // Optional Contact Information (from form)
        String formEmail = etViolatorEmail.getText().toString().trim();
        String formPhone = etViolatorPhone.getText().toString().trim();
        if (!formEmail.isEmpty()) violation.put("providedEmail", formEmail);
        if (!formPhone.isEmpty()) violation.put("providedPhone", formPhone);

        // Location & Incident
        violation.put("location", etLocation.getText().toString().trim());
        violation.put("incidentDate", new Date());

        // Enforcer Information
        violation.put("enforcerId", enforcerId);
        violation.put("enforcerName", getCurrentUserName());
        violation.put("enforcerBadgeNumber", "E-" + enforcerId.substring(0, 5).toUpperCase());

        // Status & Timestamps
        violation.put("status", "pending");
        violation.put("issueDate", issueDate);
        violation.put("dueDate", dueDate);
        violation.put("createdAt", new Date());
        violation.put("updatedAt", new Date());

        // Add to Firestore
        db.collection("violations")
                .add(violation)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Ticket issued successfully with ID: " + documentReference.getId());

                    String message = "Ticket issued successfully!";
                    if (userExists) {
                        message += " Linked to user account.";
                    } else {
                        message += " No user account found for this driver license.";
                    }

                    Toast.makeText(IssueTicketActivity.this, message, Toast.LENGTH_LONG).show();
                    showProgress(false);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error issuing ticket: ", e);
                    Toast.makeText(IssueTicketActivity.this,
                            "Error issuing ticket: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    showProgress(false);
                });
    }

    private String getCurrentUserName() {
        String email = mAuth.getCurrentUser().getEmail();
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf("@"));
        }
        return "Enforcer";
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnIssueTicket.setEnabled(!show);
        btnCancel.setEnabled(!show);
    }
}