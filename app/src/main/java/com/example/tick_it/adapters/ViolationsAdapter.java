package com.example.tick_it.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tick_it.R;
import com.example.tick_it.models.Violation;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ViolationsAdapter extends RecyclerView.Adapter<ViolationsAdapter.ViolationViewHolder> {

    private List<Violation> violations;
    private Context context;
    private OnViolationClickListener onViolationClickListener;

    public interface OnViolationClickListener {
        void onViolationClick(Violation violation);
    }

    public ViolationsAdapter(List<Violation> violations) {
        this.violations = violations;
    }

    public void setOnViolationClickListener(OnViolationClickListener listener) {
        this.onViolationClickListener = listener;
    }

    public void updateData(List<Violation> newViolations) {
        this.violations = newViolations;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViolationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_violation, parent, false);
        return new ViolationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViolationViewHolder holder, int position) {
        Violation violation = violations.get(position);
        holder.bind(violation);

        holder.itemView.setOnClickListener(v -> {
            if (onViolationClickListener != null) {
                onViolationClickListener.onViolationClick(violation);
            }
        });
    }

    @Override
    public int getItemCount() {
        return violations.size();
    }

    static class ViolationViewHolder extends RecyclerView.ViewHolder {
        private TextView tvViolationType, tvFineAmount, tvDescription;
        private TextView tvLicensePlate, tvLocation, tvIssueDate, tvStatus;

        public ViolationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvViolationType = itemView.findViewById(R.id.tvViolationType);
            tvFineAmount = itemView.findViewById(R.id.tvFineAmount);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvLicensePlate = itemView.findViewById(R.id.tvLicensePlate);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvIssueDate = itemView.findViewById(R.id.tvIssueDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        public void bind(Violation violation) {
            // Set basic violation info
            tvViolationType.setText(violation.getViolationType());
            tvFineAmount.setText(String.format("$%.2f", violation.getFineAmount()));
            tvDescription.setText(violation.getDescription());
            tvLicensePlate.setText(violation.getLicensePlate());
            tvLocation.setText(violation.getLocation());

            // Format dates
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            if (violation.getIssueDate() != null) {
                tvIssueDate.setText(sdf.format(violation.getIssueDate()));
            }

            // Set status with appropriate background
            String status = violation.getStatus() != null ? violation.getStatus() : "pending";
            tvStatus.setText(status.toUpperCase());
            updateStatusBackground(status);
        }

        private void updateStatusBackground(String status) {
            int backgroundRes = R.drawable.status_pending_bg;
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
    }
}