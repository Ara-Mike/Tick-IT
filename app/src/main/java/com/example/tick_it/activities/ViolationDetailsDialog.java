package com.example.tick_it.activities;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.example.tick_it.R;
import com.example.tick_it.models.Violation;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ViolationDetailsDialog {

    private Dialog dialog;
    private Context context;
    private Violation violation;

    public ViolationDetailsDialog(Context context, Violation violation) {
        this.context = context;
        this.violation = violation;
        initializeDialog();
    }

    private void initializeDialog() {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_violation_details);
        dialog.setCancelable(true);

        // Set dialog window properties
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        initializeViews();
        populateData();
        setupClickListeners();
    }

    private void initializeViews() {
        // Close button
        ImageButton btnClose = dialog.findViewById(R.id.btnClose);

        // Text views
        TextView tvViolationType = dialog.findViewById(R.id.tvViolationType);
        TextView tvStatus = dialog.findViewById(R.id.tvStatus);
        TextView tvFineAmount = dialog.findViewById(R.id.tvFineAmount);
        TextView tvDescription = dialog.findViewById(R.id.tvDescription);
        TextView tvLicensePlate = dialog.findViewById(R.id.tvLicensePlate);
        TextView tvLocation = dialog.findViewById(R.id.tvLocation);
        TextView tvIssueDate = dialog.findViewById(R.id.tvIssueDate);
        TextView tvDueDate = dialog.findViewById(R.id.tvDueDate);
        TextView tvEnforcerName = dialog.findViewById(R.id.tvEnforcerName);

        // Action buttons
        Button btnPayFine = dialog.findViewById(R.id.btnPayFine);
        Button btnDispute = dialog.findViewById(R.id.btnDispute);
    }

    private void populateData() {
        // Date formatter
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        // Set violation data
        setText(R.id.tvViolationType, violation.getViolationType());
        setText(R.id.tvFineAmount, String.format("$%.2f", violation.getFineAmount()));
        setText(R.id.tvDescription, violation.getDescription());
        setText(R.id.tvLicensePlate, violation.getLicensePlate());
        setText(R.id.tvLocation, violation.getLocation());
        setText(R.id.tvEnforcerName, violation.getEnforcerName());

        // Format and set dates
        if (violation.getIssueDate() != null) {
            setText(R.id.tvIssueDate, sdf.format(violation.getIssueDate()));
        } else {
            setText(R.id.tvIssueDate, "Not specified");
        }

        if (violation.getDueDate() != null) {
            setText(R.id.tvDueDate, sdf.format(violation.getDueDate()));
        } else {
            setText(R.id.tvDueDate, "Not specified");
        }

        // Set status with appropriate styling
        String status = violation.getStatus() != null ? violation.getStatus() : "pending";
        setText(R.id.tvStatus, status.toUpperCase());
        updateStatusStyle(status);

        // Show/hide action buttons based on status
        setupActionButtons(status);
    }

    private void setText(int viewId, String text) {
        TextView textView = dialog.findViewById(viewId);
        if (textView != null && text != null) {
            textView.setText(text);
        }
    }

    private void updateStatusStyle(String status) {
        TextView tvStatus = dialog.findViewById(R.id.tvStatus);
        if (tvStatus == null) return;

        int backgroundRes;
        switch (status.toLowerCase()) {
            case "paid":
                backgroundRes = R.drawable.status_paid_bg;
                break;
            case "dispute":
                backgroundRes = R.drawable.status_dispute_bg;
                break;
            case "pending":
            default:
                backgroundRes = R.drawable.status_pending_bg;
                break;
        }
        tvStatus.setBackgroundResource(backgroundRes);
    }

    private void setupActionButtons(String status) {
        Button btnPayFine = dialog.findViewById(R.id.btnPayFine);
        Button btnDispute = dialog.findViewById(R.id.btnDispute);
        View actionButtonsLayout = dialog.findViewById(R.id.actionButtonsLayout);

        if (actionButtonsLayout != null && btnPayFine != null && btnDispute != null) {
            switch (status.toLowerCase()) {
                case "pending":
                    actionButtonsLayout.setVisibility(View.VISIBLE);
                    btnPayFine.setVisibility(View.VISIBLE);
                    btnDispute.setVisibility(View.VISIBLE);
                    break;
                case "dispute":
                    actionButtonsLayout.setVisibility(View.VISIBLE);
                    btnPayFine.setVisibility(View.VISIBLE);
                    btnDispute.setVisibility(View.GONE);
                    break;
                case "paid":
                default:
                    actionButtonsLayout.setVisibility(View.GONE);
                    break;
            }
        }
    }

    private void setupClickListeners() {
        // Close button
        ImageButton btnClose = dialog.findViewById(R.id.btnClose);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dismiss());
        }

        // Pay Fine button
        Button btnPayFine = dialog.findViewById(R.id.btnPayFine);
        if (btnPayFine != null) {
            btnPayFine.setOnClickListener(v -> handlePayFine());
        }

        // Dispute button
        Button btnDispute = dialog.findViewById(R.id.btnDispute);
        if (btnDispute != null) {
            btnDispute.setOnClickListener(v -> handleDispute());
        }

        // Close on outside touch
        dialog.setOnCancelListener(dialogInterface -> dismiss());
    }

    private void handlePayFine() {
        // Implement pay fine functionality
        android.widget.Toast.makeText(context, "Pay fine functionality coming soon",
                android.widget.Toast.LENGTH_SHORT).show();
    }

    private void handleDispute() {
        // Implement dispute functionality
        android.widget.Toast.makeText(context, "Dispute functionality coming soon",
                android.widget.Toast.LENGTH_SHORT).show();
    }

    public void show() {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }
}