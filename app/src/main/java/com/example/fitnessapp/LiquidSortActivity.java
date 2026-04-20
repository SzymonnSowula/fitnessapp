package com.example.fitnessapp;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class LiquidSortActivity extends AppCompatActivity {

    private GridLayout glTubes;
    private TextView tvLevel;
    private Button btnReset, btnNext;
    private int currentLevel = 1;
    private List<TubeView> tubes = new ArrayList<>();
    private TubeView selectedTube = null;

    private static final int TUBE_CAPACITY = 4;
    private static final int[] COLORS = {
            0xFFEF4444, // Red
            0xFF3B82F6, // Blue
            0xFF10B981, // Green
            0xFFF59E0B, // Orange
            0xFF8B5CF6, // Purple
            0xFFEC4899, // Pink
            0xFF06B6D4  // Cyan
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liquid_sort);

        glTubes = findViewById(R.id.gl_tubes);
        tvLevel = findViewById(R.id.tv_level);
        btnReset = findViewById(R.id.btn_reset);
        btnNext = findViewById(R.id.btn_next);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        btnReset.setOnClickListener(v -> setupLevel(currentLevel));
        btnNext.setOnClickListener(v -> {
            currentLevel++;
            setupLevel(currentLevel);
            btnNext.setVisibility(View.GONE);
        });

        setupLevel(currentLevel);
    }

    private void setupLevel(int level) {
        tvLevel.setText("Poziom " + level);
        glTubes.removeAllViews();
        tubes.clear();
        selectedTube = null;

        int numColors = Math.min(2 + (level / 2), COLORS.length);
        int numEmptyTubes = 2;
        int totalTubes = numColors + numEmptyTubes;

        // Force a more spacious grid
        glTubes.setColumnCount(3);
        glTubes.setRowCount((int) Math.ceil(totalTubes / 3.0));

        List<Integer> allColors = new ArrayList<>();
        for (int i = 0; i < numColors; i++) {
            for (int j = 0; j < TUBE_CAPACITY; j++) {
                allColors.add(COLORS[i]);
            }
        }
        Collections.shuffle(allColors);

        for (int i = 0; i < numColors; i++) {
            TubeView tube = new TubeView(this);
            for (int j = 0; j < TUBE_CAPACITY; j++) {
                tube.colors.push(allColors.remove(0));
            }
            addTube(tube);
        }

        for (int i = 0; i < numEmptyTubes; i++) {
            addTube(new TubeView(this));
        }
    }

    private void addTube(TubeView tube) {
        tubes.add(tube);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = (int) (220 * getResources().getDisplayMetrics().density); // Slightly reduced height to fit better
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(8, 16, 8, 16); 
        tube.setLayoutParams(params);
        glTubes.addView(tube);

        tube.setOnClickListener(v -> onTubeClicked((TubeView) v));
    }

    private void onTubeClicked(TubeView clickedTube) {
        if (selectedTube == null) {
            if (!clickedTube.colors.isEmpty()) {
                selectedTube = clickedTube;
                clickedTube.animateSelection(true);
            }
        } else if (selectedTube == clickedTube) {
            selectedTube.animateSelection(false);
            selectedTube = null;
        } else {
            if (canPour(selectedTube, clickedTube)) {
                pourLiquid(selectedTube, clickedTube);
                selectedTube.animateSelection(false);
                selectedTube = null;
            } else {
                selectedTube.animateSelection(false);
                if (!clickedTube.colors.isEmpty()) {
                    selectedTube = clickedTube;
                    clickedTube.animateSelection(true);
                } else {
                    selectedTube = null;
                }
            }
        }
    }

    private boolean canPour(TubeView from, TubeView to) {
        if (from.colors.isEmpty() || to.colors.size() >= TUBE_CAPACITY) return false;
        int colorToPour = from.colors.peek();
        return to.colors.isEmpty() || to.colors.peek() == colorToPour;
    }

    private void pourLiquid(final TubeView from, final TubeView to) {
        final int colorToPour = from.colors.peek();
        
        int count = 0;
        List<Integer> temp = new ArrayList<>();
        while (!from.colors.isEmpty() && from.colors.peek() == colorToPour) {
            temp.add(from.colors.pop());
            count++;
        }

        int space = TUBE_CAPACITY - to.colors.size();
        final int pourCount = Math.min(count, space);

        // Put back any that didn't fit immediately
        for (int i = count - 1; i >= pourCount; i--) {
            from.colors.push(temp.get(i));
        }

        // Sequential pouring animation
        final int finalPourCount = pourCount;
        final android.os.Handler handler = new android.os.Handler();
        for (int i = 0; i < finalPourCount; i++) {
            final int index = i;
            handler.postDelayed(() -> {
                to.colors.push(colorToPour);
                from.invalidate();
                to.invalidate();
                if (index == finalPourCount - 1) {
                    checkWin();
                }
            }, i * 150); // 150ms delay between each block
        }
        
        if (finalPourCount == 0) {
            from.invalidate();
        }
    }

    private void checkWin() {
        boolean win = true;
        int completedTubesCount = 0;
        int numColors = Math.min(2 + (currentLevel / 2), COLORS.length);

        for (TubeView tube : tubes) {
            if (tube.colors.isEmpty()) {
                tube.setCompleted(false);
                continue;
            }
            if (tube.colors.size() == TUBE_CAPACITY) {
                int firstColor = tube.colors.get(0);
                boolean allSame = true;
                for (int color : tube.colors) {
                    if (color != firstColor) {
                        allSame = false;
                        break;
                    }
                }
                if (allSame) {
                    tube.setCompleted(true);
                    completedTubesCount++;
                } else {
                    tube.setCompleted(false);
                    win = false;
                }
            } else {
                tube.setCompleted(false);
                win = false;
            }
        }

        if (win && completedTubesCount == numColors) {
            Toast.makeText(this, "Brawo! Poziom ukończony!", Toast.LENGTH_SHORT).show();
            btnNext.setVisibility(View.VISIBLE);
        }
    }

    public static class TubeView extends View {
        Stack<Integer> colors = new Stack<>();
        private float selectionOffset = 0;
        private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private RectF rect = new RectF();
        private RectF bottomRect = new RectF();
        private android.graphics.Path tubePath = new android.graphics.Path();
        private boolean isCompleted = false;

        public TubeView(android.content.Context context) {
            super(context);
        }

        public void animateSelection(boolean selected) {
            float target = selected ? -60f : 0f;
            android.animation.ObjectAnimator animator = android.animation.ObjectAnimator.ofFloat(this, "selectionY", selectionOffset, target);
            animator.setDuration(250);
            animator.setInterpolator(new android.view.animation.DecelerateInterpolator());
            animator.start();
        }

        public void setSelectionY(float y) {
            this.selectionOffset = y;
            invalidate();
        }

        public void setCompleted(boolean completed) {
            this.isCompleted = completed;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth();
            float h = getHeight();
            
            // Constrain width to look more like a real test tube
            float tubeWidth = Math.min(w * 0.75f, 140f); 
            float left = (w - tubeWidth) / 2;
            float right = left + tubeWidth;
            float topBound = 70; 
            float bottomBound = h - 20;
            float tubeHeight = bottomBound - topBound;

            canvas.save();
            canvas.translate(0, selectionOffset);

            // 1. Draw Liquid (Inside)
            paint.setStyle(Paint.Style.FILL);
            float innerPadding = 12;
            float liquidWidth = tubeWidth - (innerPadding * 2);
            float liquidLeft = left + innerPadding;
            float liquidRight = right - innerPadding;
            
            // Calculate block height based on available internal space
            float availableLiquidHeight = tubeHeight - (tubeWidth / 2) - 10;
            float blockHeight = availableLiquidHeight / TUBE_CAPACITY;
            
            for (int i = 0; i < colors.size(); i++) {
                paint.setColor(colors.get(i));
                float blockBottom = bottomBound - (innerPadding + 4) - (i * blockHeight);
                float blockTop = blockBottom - blockHeight + 2;
                
                if (i == 0) {
                    // Rounded bottom for the first block
                    float radius = liquidWidth / 2;
                    // Draw the rectangular part of the bottom block
                    rect.set(liquidLeft, Math.min(blockTop, blockBottom - radius), liquidRight, blockBottom - radius);
                    canvas.drawRect(rect, paint);
                    // Draw the semi-circle part
                    bottomRect.set(liquidLeft, blockBottom - radius * 2, liquidRight, blockBottom);
                    canvas.drawArc(bottomRect, 0, 180, true, paint);
                } else {
                    rect.set(liquidLeft, blockTop, liquidRight, blockBottom);
                    canvas.drawRect(rect, paint);
                }
            }

            // 2. Draw Tube Glass (Outline)
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(14);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(isCompleted ? 0xFF059669 : 0xFFCBD5E1);
            
            tubePath.reset();
            tubePath.moveTo(left, topBound);
            tubePath.lineTo(left, bottomBound - tubeWidth / 2);
            rect.set(left, bottomBound - tubeWidth, right, bottomBound);
            tubePath.arcTo(rect, 180, -180, false);
            tubePath.lineTo(right, topBound);
            canvas.drawPath(tubePath, paint);

            // 3. Draw rim at the top
            paint.setStrokeWidth(12);
            canvas.drawLine(left - 10, topBound, right + 10, topBound, paint);

            canvas.restore();
        }
    }
}