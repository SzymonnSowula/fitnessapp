package com.example.fitnessapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GameSessionAdapter extends RecyclerView.Adapter<GameSessionAdapter.ViewHolder> {

    private List<GameSession> sessions;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public GameSessionAdapter(List<GameSession> sessions) {
        this.sessions = sessions;
    }

    public void updateData(List<GameSession> sessions) {
        this.sessions = sessions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game_session, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GameSession s = sessions.get(position);
        String typeName;
        switch (s.gameType) {
            case "memory": typeName = "MEMORY"; break;
            case "colors": typeName = "KOLORY"; break;
            case "liquid": typeName = "PŁYNY"; break;
            default: typeName = s.gameType.toUpperCase();
        }
        holder.tvGameType.setText(typeName);
        holder.tvScore.setText("Wynik: " + s.score + " | Poziom: " + s.level);
        holder.tvDate.setText(dateFormat.format(new Date(s.completedAt)));
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvGameType, tvScore, tvDate;

        ViewHolder(View itemView) {
            super(itemView);
            tvGameType = itemView.findViewById(R.id.tv_game_type);
            tvScore = itemView.findViewById(R.id.tv_score);
            tvDate = itemView.findViewById(R.id.tv_date);
        }
    }
}
