package com.example.fitnessapp;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game2048Activity extends AppCompatActivity {

    private int[][] grid = new int[4][4];
    private TextView[][] cells = new TextView[4][4];
    private int score = 0;
    private TextView tvScore;
    private VoiceNavigator voiceNavigator;

    private final int[] cellColors = {
            0xFFEEE4DA, 0xFFEDE0C8, 0xFFF2B179, 0xFFF59563,
            0xFFF67C5F, 0xFFF65E3B, 0xFFEDCF72, 0xFFEDCC61,
            0xFFEDC850, 0xFFEDC53F, 0xFFEDC22E
    };

    private int targetScore = 2048;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game2048);

        int difficulty = getIntent().getIntExtra("EXTRA_DIFFICULTY", GameDifficultyActivity.DIFFICULTY_HARD);
        switch (difficulty) {
            case GameDifficultyActivity.DIFFICULTY_EASY:
                targetScore = 512;
                break;
            case GameDifficultyActivity.DIFFICULTY_MEDIUM:
                targetScore = 1024;
                break;
            case GameDifficultyActivity.DIFFICULTY_HARD:
                targetScore = 2048;
                break;
        }

        voiceNavigator = new VoiceNavigator(this, new VoiceNavigator.VoiceCallback() {
            @Override
            public void onVoiceCommand(String command) {
                runOnUiThread(() -> handleVoiceCommand(command));
            }
        });
        voiceNavigator.setup();

        tvScore = findViewById(R.id.tv_score);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_restart).setOnClickListener(v -> restartGame());

        findViewById(R.id.btn_left).setOnClickListener(v -> moveLeft());
        findViewById(R.id.btn_right).setOnClickListener(v -> moveRight());
        findViewById(R.id.btn_up).setOnClickListener(v -> moveUp());
        findViewById(R.id.btn_down).setOnClickListener(v -> moveDown());

        initCells();
        restartGame();

        voiceNavigator.speakDelayed("Gra 2048. Twój cel to " + targetScore + ". Przesuwaj kafelki aby łączyć te same liczby.", 500);
    }

    private void initCells() {
        int[] ids = {
                R.id.cell_00, R.id.cell_01, R.id.cell_02, R.id.cell_03,
                R.id.cell_10, R.id.cell_11, R.id.cell_12, R.id.cell_13,
                R.id.cell_20, R.id.cell_21, R.id.cell_22, R.id.cell_23,
                R.id.cell_30, R.id.cell_31, R.id.cell_32, R.id.cell_33
        };
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                cells[i][j] = findViewById(ids[i * 4 + j]);
            }
        }
    }

    private void handleVoiceCommand(String command) {
        switch (command) {
            case "restart":
            case "new_game":
            case "reset":
                restartGame();
                voiceNavigator.speak("Zaczynamy od nowa.");
                break;
            case "move_up":
                voiceNavigator.speak("Góra");
                moveUp();
                break;
            case "move_down":
                voiceNavigator.speak("Dół");
                moveDown();
                break;
            case "move_left":
                voiceNavigator.speak("Lewo");
                moveLeft();
                break;
            case "move_right":
                voiceNavigator.speak("Prawo");
                moveRight();
                break;
            case "help":
                voiceNavigator.speak("Mów góra, dół, lewo lub prawo aby przesuwać kafelki.");
                break;
            case "read":
            case "repeat":
                voiceNavigator.speak("Gra 2048. Twój wynik to " + score);
                break;
        }
    }

    private void restartGame() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                grid[i][j] = 0;
            }
        }
        score = 0;
        tvScore.setText("0");
        addRandomTile();
        addRandomTile();
        updateUI();
    }

    private void addRandomTile() {
        List<int[]> empty = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (grid[i][j] == 0) empty.add(new int[]{i, j});
            }
        }
        if (!empty.isEmpty()) {
            int[] pos = empty.get(new Random().nextInt(empty.size()));
            grid[pos[0]][pos[1]] = Math.random() < 0.9 ? 2 : 4;
        }
    }

    private void updateUI() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                TextView cell = cells[i][j];
                int val = grid[i][j];
                if (val == 0) {
                    cell.setText("");
                    cell.setBackgroundColor(0xFFFFFFFF);
                } else {
                    cell.setText(String.valueOf(val));
                    int colorIdx = (int) (Math.log(val) / Math.log(2)) - 1;
                    cell.setBackgroundColor(cellColors[Math.min(colorIdx, cellColors.length - 1)]);
                }
            }
        }
    }

    private void moveLeft() {
        if (mergeGrid(true)) {
            addRandomTile();
            updateUI();
            checkGameState();
        }
    }

    private void moveRight() {
        reverseGrid();
        if (mergeGrid(true)) {
            reverseGrid();
            addRandomTile();
            updateUI();
            checkGameState();
        } else {
            reverseGrid();
        }
    }

    private void moveUp() {
        transposeGrid();
        if (mergeGrid(true)) {
            transposeGrid();
            addRandomTile();
            updateUI();
            checkGameState();
        } else {
            transposeGrid();
        }
    }

    private void moveDown() {
        transposeGrid();
        reverseGrid();
        if (mergeGrid(true)) {
            reverseGrid();
            transposeGrid();
            addRandomTile();
            updateUI();
            checkGameState();
        } else {
            reverseGrid();
            transposeGrid();
        }
    }

    private void checkGameState() {
        boolean hasWon = false;
        boolean hasEmpty = false;

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (grid[i][j] == targetScore) hasWon = true;
                if (grid[i][j] == 0) hasEmpty = true;
            }
        }

        if (hasWon) {
            Toast.makeText(this, "Gratulacje! Cel osiągnięty!", Toast.LENGTH_LONG).show();
            voiceNavigator.speak("Gratulacje! Cel " + targetScore + " osiągnięty! Wygrałeś!");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceNavigator != null) {
            voiceNavigator.cleanup();
        }
    }

    private boolean mergeGrid(boolean left) {
        boolean moved = false;
        for (int i = 0; i < 4; i++) {
            int[] row = grid[i].clone();
            if (!left) {
                for (int j = 0; j < 2; j++) {
                    int tmp = grid[i][j];
                    grid[i][j] = grid[i][3 - j];
                    grid[i][3 - j] = tmp;
                }
            }
            List<Integer> tiles = new ArrayList<>();
            for (int j = 0; j < 4; j++) {
                if (grid[i][j] != 0) tiles.add(grid[i][j]);
            }
            for (int j = 0; j < tiles.size() - 1; j++) {
                if (tiles.get(j).equals(tiles.get(j + 1))) {
                    int merged = tiles.get(j) * 2;
                    tiles.set(j, merged);
                    tiles.remove(j + 1);
                    score += merged;
                    runOnUiThread(() -> tvScore.setText(String.valueOf(score)));
                }
            }
            while (tiles.size() < 4) tiles.add(0);

            if (!left) {
                for (int j = 0; j < 4; j++) {
                    grid[i][j] = tiles.get(3 - j);
                }
            } else {
                for (int j = 0; j < 4; j++) {
                    grid[i][j] = tiles.get(j);
                }
            }

            for (int j = 0; j < 4; j++) {
                if (grid[i][j] != row[j]) moved = true;
            }
        }
        return moved;
    }

    private void reverseGrid() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 2; j++) {
                int tmp = grid[i][j];
                grid[i][j] = grid[i][3 - j];
                grid[i][3 - j] = tmp;
            }
        }
    }

    private void transposeGrid() {
        for (int i = 0; i < 4; i++) {
            for (int j = i + 1; j < 4; j++) {
                int tmp = grid[i][j];
                grid[i][j] = grid[j][i];
                grid[j][i] = tmp;
            }
        }
    }
}