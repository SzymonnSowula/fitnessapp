package com.example.fitnessapp;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.NonNull;

public class VoiceHelpDialog extends Dialog {

    private String helpTitle;
    private String helpContent;

    public VoiceHelpDialog(@NonNull Context context) {
        super(context);
    }

    public VoiceHelpDialog(@NonNull Context context, String title, String content) {
        super(context);
        this.helpTitle = title;
        this.helpContent = content;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_voice_help);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Dialog automatically uses the layout's title and content
        setCanceledOnTouchOutside(true);
    }

    public static void show(Context context) {
        VoiceHelpDialog dialog = new VoiceHelpDialog(context);
        dialog.show();
    }

    public static void show(Context context, String title, String content) {
        VoiceHelpDialog dialog = new VoiceHelpDialog(context, title, content);
        dialog.show();
    }

    public static void showVoiceHelp(Context context) {
        VoiceHelpDialog dialog = new VoiceHelpDialog(context);
        dialog.show();
    }
}