package com.example.attendble.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.attendble.R;
import com.example.attendble.data.ServiceLocator;
import com.example.attendble.domain.Callback;
import com.example.attendble.domain.enums.UserRole;
import com.example.attendble.domain.model.User;
import com.example.attendble.domain.usecase.LoginUseCase;
import com.example.attendble.ui.prof.home.HomeActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;

    private LoginUseCase loginUseCase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);

        btnLogin = findViewById(R.id.btn_login);
        MaterialButton btnSignup = findViewById(R.id.btn_signup);
        MaterialButton btnForgot = findViewById(R.id.btn_forgot);

        loginUseCase = ServiceLocator.provideLoginUseCase();

        btnLogin.setOnClickListener(v -> attemptLogin());
        btnSignup.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
        btnForgot.setOnClickListener(v -> toast(R.string.toast_forgot));
    }

    private void attemptLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        tilEmail.setError(null);
        tilPassword.setError(null);

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.toast_email_required));
            toast(R.string.toast_email_required);
            return;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.toast_password_required));
            toast(R.string.toast_password_required);
            return;
        }

        btnLogin.setEnabled(false);
        loginUseCase.execute(email, password, new Callback<User>() {
            @Override
            public void onSuccess(User user) {
                btnLogin.setEnabled(true);
                if (user.getRole() == UserRole.PROFESSEUR) {
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                } else {
                    startActivity(new Intent(LoginActivity.this,
                            com.example.attendble.ui.student.home.HomeActivity.class));
                }
                finish();
            }

            @Override
            public void onError(Exception e) {
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void toast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }
}