package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MindGamesActivity extends AppCompatActivity {

 private VoiceNavigator voiceNavigator;

 @Override
 protected void onCreate(Bundle savedInstanceState) {
 super.onCreate(savedInstanceState);
 setContentView(R.layout.activity_mind_games);

 voiceNavigator = new VoiceNavigator(this, new VoiceNavigator.VoiceCallback() {
 @Override
 public void onVoiceCommand(String command) {
 runOnUiThread(() -> handleVoiceCommand(command));
 }
 });

 voiceNavigator.setup();

NavbarHelper.initNavbar(this);

        // Help button listener
        ImageButton btnHelp = findViewById(R.id.btn_help);
        if (btnHelp != null) {
            btnHelp.setOnClickListener(v -> VoiceHelpDialog.show(this));
        }

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

 CardView cardMemory = findViewById(R.id.card_memory_selection);
 cardMemory.setOnClickListener(v -> {
 Intent intent = new Intent(MindGamesActivity.this, GameInstructionActivity.class);
 intent.putExtra(GameInstructionActivity.EXTRA_GAME_TYPE, GameInstructionActivity.GAME_MEMORY);
 startActivity(intent);
 });

 CardView cardColors = findViewById(R.id.card_colors_selection);
 cardColors.setOnClickListener(v -> {
 Intent intent = new Intent(MindGamesActivity.this, GameInstructionActivity.class);
 intent.putExtra(GameInstructionActivity.EXTRA_GAME_TYPE, GameInstructionActivity.GAME_COLORS);
 startActivity(intent);
 });

 CardView cardLiquid = findViewById(R.id.card_liquid_selection);
 cardLiquid.setOnClickListener(v -> {
 Intent intent = new Intent(MindGamesActivity.this, GameInstructionActivity.class);
 intent.putExtra(GameInstructionActivity.EXTRA_GAME_TYPE, GameInstructionActivity.GAME_LIQUID);
 startActivity(intent);
 });

 voiceNavigator.speakDelayed("Gry umysłowe. Wybierz grę.", 500);
 }

 private void handleVoiceCommand(String command) {
 switch (command) {
 case "back":
 onBackPressed();
 break;
 case "exit":
 finish();
 break;
 case "home":
 navigateTo(ChoiceActivity.class);
 break;
 case "games":
 case "read":
 case "repeat":
 voiceNavigator.speak("Dostępne gry: Memory - ćwiczenie pamięci. Kolory - powtarzanie sekwencji. Płyny - sortowanie kolorów.");
 break;
 case "game_memory":
 voiceNavigator.speak("Uruchamiam grę memory.");
 {
 Intent intent = new Intent(this, GameInstructionActivity.class);
 intent.putExtra(GameInstructionActivity.EXTRA_GAME_TYPE, GameInstructionActivity.GAME_MEMORY);
 startActivity(intent);
 }
 break;
 case "game_colors":
 voiceNavigator.speak("Uruchamiam grę kolory.");
 {
 Intent intent = new Intent(this, GameInstructionActivity.class);
 intent.putExtra(GameInstructionActivity.EXTRA_GAME_TYPE, GameInstructionActivity.GAME_COLORS);
 startActivity(intent);
 }
 break;
 case "game_liquid":
 voiceNavigator.speak("Uruchamiam sortowanie płynów.");
 {
 Intent intent = new Intent(this, GameInstructionActivity.class);
 intent.putExtra(GameInstructionActivity.EXTRA_GAME_TYPE, GameInstructionActivity.GAME_LIQUID);
 startActivity(intent);
 }
 break;
 case "stop":
 voiceNavigator.stopSpeaking();
 break;
 case "help":
 voiceNavigator.speak(VoiceCommands.getGameHelpText());
 break;
 }
 }

 private void navigateTo(Class<?> activityClass) {
 Intent intent = new Intent(this, activityClass);
 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
 startActivity(intent);
 finish();
 }

 @Override
 protected void onDestroy() {
 super.onDestroy();
 if (voiceNavigator != null) {
 voiceNavigator.cleanup();
 }
 }
}