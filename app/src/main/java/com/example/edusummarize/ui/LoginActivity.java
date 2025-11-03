package com.example.edusummarize.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edusummarize.R;
import com.example.edusummarize.ui.components.CustomToast;
import com.example.edusummarize.utils.FirebaseAuthDebug;
import com.example.edusummarize.utils.HapticFeedbackUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Debug Firebase Auth status
        FirebaseAuthDebug.logAuthStatus();

        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToHome();
            return;
        }

        initViews();
        setupListeners();

        // UI/UX IMPROVEMENT: Add fade-in animation
        findViewById(R.id.card_login).startAnimation(
            android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_in)
        );
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> {
            // UI/UX IMPROVEMENT: Haptic feedback on button press
            HapticFeedbackUtil.lightVibrate(this);
            v.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.bounce));
            loginUser();
        });

        tvRegister.setOnClickListener(v -> {
            HapticFeedbackUtil.lightVibrate(this);
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            // UI/UX IMPROVEMENT: Smooth activity transition
            overridePendingTransition(R.anim.slide_up, R.anim.fade_in);
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email không được để trống");
            etEmail.requestFocus();
            HapticFeedbackUtil.errorVibrate(this);
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không đúng định dạng");
            etEmail.requestFocus();
            HapticFeedbackUtil.errorVibrate(this);
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Mật khẩu không được để trống");
            etPassword.requestFocus();
            HapticFeedbackUtil.errorVibrate(this);
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            etPassword.requestFocus();
            HapticFeedbackUtil.errorVibrate(this);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        Log.d("LoginActivity", "Attempting login with email: " + email);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);

                    if (task.isSuccessful()) {
                        Log.d("LoginActivity", "Login successful");
                        // UI/UX IMPROVEMENT: Success feedback
                        HapticFeedbackUtil.successVibrate(this);
                        CustomToast.success(this, "Đăng nhập thành công!");
                        navigateToHome();
                    } else {
                        FirebaseAuthDebug.logError("Login failed", task.getException());
                        // UI/UX IMPROVEMENT: Error feedback
                        HapticFeedbackUtil.errorVibrate(this);
                        handleAuthError(task.getException());
                    }
                });
    }

    private void handleAuthError(Exception exception) {
        String errorMessage = "Đăng nhập thất bại";

        if (exception != null) {
            Log.e("LoginActivity", "Authentication failed", exception);
            String error = exception.getMessage();
            if (error != null) {
                if (error.contains("password is invalid") || error.contains("wrong-password")) {
                    errorMessage = "Mật khẩu không đúng";
                } else if (error.contains("user-not-found") || error.contains("no user record")) {
                    errorMessage = "Email chưa được đăng ký";
                } else if (error.contains("invalid-email")) {
                    errorMessage = "Email không hợp lệ";
                } else if (error.contains("user-disabled")) {
                    errorMessage = "Tài khoản đã bị vô hiệu hóa";
                } else if (error.contains("too-many-requests")) {
                    errorMessage = "Quá nhiều lần thử, vui lòng thử lại sau";
                } else if (error.contains("network error") || error.contains("network")) {
                    errorMessage = "Lỗi kết nối mạng";
                } else {
                    errorMessage = "Đăng nhập thất bại: " + error;
                }
            }
        }

        // UI/UX IMPROVEMENT: Use custom toast instead of default
        CustomToast.error(this, errorMessage);
    }

    private void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        // UI/UX IMPROVEMENT: Smooth transition
        overridePendingTransition(R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
