package com.example.edusummarize.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edusummarize.R;
import com.example.edusummarize.utils.FirebaseAuthDebug;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> registerUser());
        tvLogin.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email không được để trống");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không đúng định dạng");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Mật khẩu không được để trống");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu không khớp");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this,
                                "Đăng ký thành công! Vui lòng đăng nhập.",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        FirebaseAuthDebug.logError("Registration failed", task.getException());
                        handleRegistrationError(task.getException());
                    }
                });
    }

    private void handleRegistrationError(Exception exception) {
        String errorMessage = "Đăng ký thất bại";

        if (exception != null) {
            String error = exception.getMessage();
            if (error != null) {
                if (error.contains("email-already-in-use")) {
                    errorMessage = "Email này đã được sử dụng";
                } else if (error.contains("invalid-email")) {
                    errorMessage = "Email không hợp lệ";
                } else if (error.contains("weak-password")) {
                    errorMessage = "Mật khẩu quá yếu";
                } else if (error.contains("network error") || error.contains("network")) {
                    errorMessage = "Lỗi kết nối mạng";
                } else {
                    errorMessage = "Đăng ký thất bại: " + error;
                }
            }
        }

        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
    }
}