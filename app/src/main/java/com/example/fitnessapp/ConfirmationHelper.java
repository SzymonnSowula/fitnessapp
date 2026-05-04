package com.example.fitnessapp;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

// Dodajemy prawidłowy import dla AlertDialog z AndroidX
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.button.MaterialButton;

public class ConfirmationHelper {

    public interface ConfirmationCallback {
        void onConfirm();
        void onCancel();
    }

    public static void showExitConfirmation(Activity activity, ConfirmationCallback callback) {
        if (activity == null || activity.isFinishing()) return;

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);

        View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_exit_confirmation, null);
        builder.setView(dialogView);

        // POPRAWKA: Używamy teraz AlertDialog z androidx.appcompat.app
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvTitle = dialogView.findViewById(R.id.tv_exit_title);
        TextView tvMessage = dialogView.findViewById(R.id.tv_exit_message);
        MaterialButton btnConfirm = dialogView.findViewById(R.id.btn_exit_confirm);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_exit_cancel);

        if (tvTitle != null) {
            tvTitle.setText(activity.getString(R.string.dialog_exit_title));
        }
        if (tvMessage != null) {
            tvMessage.setText(activity.getString(R.string.dialog_exit_message));
        }
        if (btnConfirm != null) {
            btnConfirm.setText(activity.getString(R.string.dialog_exit_confirm));
            btnConfirm.setOnClickListener(v -> {
                dialog.dismiss();
                if (callback != null) callback.onConfirm();
            });
        }
        if (btnCancel != null) {
            btnCancel.setText(activity.getString(R.string.dialog_exit_cancel));
            btnCancel.setOnClickListener(v -> {
                dialog.dismiss();
                if (callback != null) callback.onCancel();
            });
        }

        dialog.setCancelable(false);
        dialog.show();
    }
}