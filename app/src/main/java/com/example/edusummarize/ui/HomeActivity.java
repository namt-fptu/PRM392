package com.example.edusummarize.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.edusummarize.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    private TextView tvGreeting;
    private CardView cardScan, cardLibrary;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupGreeting();
        setupListeners();
    }

    private void initViews() {
        tvGreeting = findViewById(R.id.tv_greeting);
        cardScan = findViewById(R.id.card_scan);
        cardLibrary = findViewById(R.id.card_library);
    }

    private void setupGreeting() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            String name = email != null ? email.split("@")[0] : "Bạn";
            tvGreeting.setText("Xin chào, " + name + "!");
        }
    }

    private void setupListeners() {
        cardScan.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SummarizeActivity.class);
            startActivity(intent);
        });

        cardLibrary.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, LibraryActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            showLogoutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    mAuth.signOut();
                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}