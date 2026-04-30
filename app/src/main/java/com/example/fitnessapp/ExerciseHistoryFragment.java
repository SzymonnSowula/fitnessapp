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

import java.util.ArrayList;
import java.util.List;

public class ExerciseHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private ExerciseSessionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise_history, container, false);
        recyclerView = view.findViewById(R.id.recycler_exercises);
        tvEmpty = view.findViewById(R.id.tv_empty);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ExerciseSessionAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        loadData();
        return view;
    }

    private void loadData() {
        new Thread(() -> {
            List<ExerciseSession> sessions = AppDatabase.getDatabase(requireContext()).exerciseSessionDao().getAllDesc();
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
