package com.example.tick_it.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
// import com.example.tick_it.R;  // Keep this commented for now
import com.example.tick_it.utils.FirebaseUtil;

public class EnforcerMainActivity extends AppCompatActivity {

    private Button btnIssueTicket, btnTicketHistory, btnSyncData;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Try to set content view, if R is available
        try {
            // This will only work if R class is generated
            Class<?> rClass = Class.forName("com.example.tick_it.R$layout");
            Object layoutField = rClass.getDeclaredField("activity_enforcer_main").get(null);
            int layoutId = (Integer) layoutField;
            setContentView(layoutId);
        } catch (Exception e) {
            // Create layout programmatically if R isn't available
            setContentView(createSimpleLayout());
        }

        initializeViews();
        setupClickListeners();

        mAuth = FirebaseUtil.getAuth();
    }

    private android.view.View createSimpleLayout() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        TextView title = new TextView(this);
        title.setText("Enforcer Dashboard");
        title.setTextSize(20);
        title.setPadding(0, 0, 0, 30);

        btnIssueTicket = new Button(this);
        btnIssueTicket.setText("Issue Ticket");
        btnIssueTicket.setPadding(0, 20, 0, 20);

        btnTicketHistory = new Button(this);
        btnTicketHistory.setText("Ticket History");
        btnTicketHistory.setPadding(0, 20, 0, 20);

        btnSyncData = new Button(this);
        btnSyncData.setText("Sync Data");
        btnSyncData.setPadding(0, 20, 0, 20);

        Button logoutBtn = new Button(this);
        logoutBtn.setText("Logout");
        logoutBtn.setPadding(0, 20, 0, 20);
        logoutBtn.setOnClickListener(this::logout);

        layout.addView(title);
        layout.addView(btnIssueTicket);
        layout.addView(btnTicketHistory);
        layout.addView(btnSyncData);
        layout.addView(logoutBtn);

        return layout;
    }

    private void initializeViews() {
        // If we used the simple layout, buttons are already initialized
        // Otherwise try to find them by ID
        if (btnIssueTicket == null) {
            try {
                Class<?> rClass = Class.forName("com.example.tick_it.R$id");
                btnIssueTicket = findViewById((Integer) rClass.getDeclaredField("btnIssueTicket").get(null));
                btnTicketHistory = findViewById((Integer) rClass.getDeclaredField("btnTicketHistory").get(null));
                btnSyncData = findViewById((Integer) rClass.getDeclaredField("btnSyncData").get(null));
            } catch (Exception e) {
                // If R.id isn't available, we're using the programmatic layout
            }
        }
    }

    private void setupClickListeners() {
        btnIssueTicket.setOnClickListener(v -> {
            // Start Issue Ticket Activity
            Intent intent = new Intent(EnforcerMainActivity.this, IssueTicketActivity.class);
            startActivity(intent);
        });

        btnTicketHistory.setOnClickListener(v ->
                Toast.makeText(this, "Ticket History - Feature coming soon", Toast.LENGTH_SHORT).show());

        btnSyncData.setOnClickListener(v ->
                Toast.makeText(this, "Sync Data - Feature coming soon", Toast.LENGTH_SHORT).show());
    }

    public void logout(android.view.View view) {
        mAuth.signOut();
        Intent intent = new Intent(EnforcerMainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}