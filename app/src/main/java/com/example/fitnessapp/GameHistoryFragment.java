package com.example.fitnessapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.utils.AppExecutors;

import java.util.ArrayList;
import java.util.List;

public class GameHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private GameSessionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_history, container, false);
        recyclerView = view.findViewById(R.id.recycler_games);
        tvEmpty = view.findViewById(R.id.tv_empty);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GameSessionAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        loadData();
        return view;
    }

    private void loadData() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<GameSession> sessions = AppDatabase.getDatabase(requireContext()).gameSessionDao().getAllDesc();
            requireActivity().runOnUiThread(() -> {
                if (sessions.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.updateData(sessions);
                }
            });
        }).start();
    }
}
