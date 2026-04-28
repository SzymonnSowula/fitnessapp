package com.example.fitnessapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.button.MaterialButton;

public class ConfirmationHelper {

    public interface ConfirmationCallback {
        void onConfirm();
        void onCancel();
    }

    public static void showExitConfirmation(Activity activity, ConfirmationCallback callback) {
        if (activity == null || activity.isFinishing()) return;

        com.google.android.material.dialog.MaterialAlertDialogBuilder builder =
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(activity);

        View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_exit_confirmation, null);
        builder.setView(dialogView);

        android.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvTitle = dialogView.findViewById(R.id.tv_exit_title);
        TextView tvMessage = dialogView.findViewById(R.id.tv_exit_message);
        MaterialButton btnConfirm = dialogView.findViewById(R.id.btn_exit_confirm);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_exit_cancel);

        if (tvTitle != null) {
            tvTitle.setText("Wyjście z aplikacji");
        }
        if (tvMessage != null) {
            tvMessage.setText("Czy na pewno chcesz wyjść z aplikacji?");
        }
        if (btnConfirm != null) {
            btnConfirm.setText("Tak, wyjdź");
            btnConfirm.setOnClickListener(v -> {
                dialog.dismiss();
                if (callback != null) callback.onConfirm();
            });
        }
        if (btnCancel != null) {
            btnCancel.setText("Nie, zostaję");
            btnCancel.setOnClickListener(v -> {
                dialog.dismiss();
                if (callback != null) callback.onCancel();
            });
        }

        dialog.setCancelable(false);
        dialog.show();
    }
}
