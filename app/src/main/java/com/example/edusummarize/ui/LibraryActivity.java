package com.example.edusummarize.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edusummarize.R;
import com.example.edusummarize.adapter.SummaryAdapter;
import com.example.edusummarize.firebase.FirebaseRepository;
import com.example.edusummarize.model.Summary;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class LibraryActivity extends AppCompatActivity {

    private EditText etSearch;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private ProgressBar progressBar;

    private SummaryAdapter adapter;
    private List<Summary> summaryList;
    private List<Summary> filteredList;
    private FirebaseRepository firebaseRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thư viện");
        }

        firebaseRepository = new FirebaseRepository();
        summaryList = new ArrayList<>();
        filteredList = new ArrayList<>();

        initViews();
        setupRecyclerView();
        setupSearch();
        loadSummaries();
    }

    private void initViews() {
        etSearch = findViewById(R.id.et_search);
        recyclerView = findViewById(R.id.recycler_view);
        tvEmpty = findViewById(R.id.tv_empty);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupRecyclerView() {
        adapter = new SummaryAdapter(this, filteredList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSummaries(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadSummaries() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firebaseRepository.getSummaries(userId, new FirebaseRepository.LoadCallback() {
            @Override
            public void onSuccess(List<Summary> summaries) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    summaryList.clear();
                    summaryList.addAll(summaries);
                    filteredList.clear();
                    filteredList.addAll(summaries);

                    if (summaries.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Lỗi tải dữ liệu: " + error);
                    Toast.makeText(LibraryActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void filterSummaries(String query) {
        filteredList.clear();

        if (query.isEmpty()) {
            filteredList.addAll(summaryList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Summary summary : summaryList) {
                if (summary.getTitle().toLowerCase().contains(lowerQuery) ||
                        summary.getSummaryText().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(summary);
                }
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredList.isEmpty() && !summaryList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Không tìm thấy kết quả");
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adapter != null) {
            adapter.releasePlayer();
        }
    }
}