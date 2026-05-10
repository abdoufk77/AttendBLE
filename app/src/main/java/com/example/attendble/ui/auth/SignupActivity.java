package com.example.attendble.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.attendble.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SignupActivity extends AppCompatActivity {

    private TextInputLayout tilFullName;
    private TextInputLayout tilRoleField;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;

    private TextInputEditText etFullName;
    private TextInputEditText etRoleField;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;

    private MaterialButton btnSignup;

    private boolean isStudent = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        tilFullName = findViewById(R.id.til_fullname);
        tilRoleField = findViewById(R.id.til_role_field);
        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);

        etFullName = findViewById(R.id.et_fullname);
        etRoleField = findViewById(R.id.et_role_field);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);

        MaterialButtonToggleGroup toggleRole = findViewById(R.id.toggle_role);
        btnSignup = findViewById(R.id.btn_signup);
        MaterialButton btnLogin = findViewById(R.id.btn_login);

        toggleRole.check(R.id.btn_role_student);
        applyRole(true);

        toggleRole.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            applyRole(checkedId == R.id.btn_role_student);
        });

        btnSignup.setOnClickListener(v -> attemptSignup());
        btnLogin.setOnClickListener(v -> finish());
    }

    private void applyRole(boolean student) {
        isStudent = student;
        if (student) {
            tilRoleField.setHint(getString(R.string.signup_student_id_hint));
            btnSignup.setText(R.string.signup_button_student);
        } else {
            tilRoleField.setHint(getString(R.string.signup_department_hint));
            btnSignup.setText(R.string.signup_button_professor);
        }
        etRoleField.setText("");
        tilRoleField.setError(null);
    }

    private void attemptSignup() {
        String fullName = textOf(etFullName);
        String roleField = textOf(etRoleField);
        String email = textOf(etEmail);
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String confirm = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

        tilFullName.setError(null);
        tilRoleField.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError(getString(R.string.toast_fullname_required));
            toast(R.string.toast_fullname_required);
            return;
        }
        if (TextUtils.isEmpty(roleField)) {
            int msg = isStudent ? R.string.toast_student_id_required : R.string.toast_department_required;
            tilRoleField.setError(getString(msg));
            toast(msg);
            return;
        }
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
        if (TextUtils.isEmpty(confirm)) {
            tilConfirmPassword.setError(getString(R.string.toast_confirm_password_required));
            toast(R.string.toast_confirm_password_required);
            return;
        }
        if (!password.equals(confirm)) {
            tilConfirmPassword.setError(getString(R.string.toast_password_mismatch));
            toast(R.string.toast_password_mismatch);
            return;
        }

        toast(isStudent ? R.string.toast_signup_student : R.string.toast_signup_professor);
    }

    private String textOf(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void toast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }
}
