package com.example.attendble.ui.student.classes;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.attendble.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Écran "Join a Class" : l'étudiant saisit le code d'invitation transmis par le prof
 * (workflow Google Classroom). À brancher via JoinClasseUseCase.
 */
public class JoinClassActivity extends AppCompatActivity {

    private static final int MIN_CODE_LENGTH = 4;

    private TextInputLayout tilCode;
    private TextInputEditText etCode;
    private MaterialButton btnJoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_join_class);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        MaterialToolbar topbar = findViewById(R.id.topbar);
        topbar.setNavigationOnClickListener(v -> finish());

        tilCode = findViewById(R.id.til_code);
        etCode = findViewById(R.id.et_code);
        btnJoin = findViewById(R.id.btn_join);

        etCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilCode.setError(null);
                btnJoin.setEnabled(s.toString().trim().length() >= MIN_CODE_LENGTH);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        btnJoin.setOnClickListener(v -> attemptJoin());
        findViewById(R.id.btn_no_code).setOnClickListener(v ->
                Toast.makeText(this, R.string.jc_toast_no_code, Toast.LENGTH_LONG).show());
    }

    private void attemptJoin() {
        String code = etCode.getText() != null ? etCode.getText().toString().trim() : "";
        if (code.length() < MIN_CODE_LENGTH) {
            tilCode.setError(getString(R.string.jc_error_invalid));
            return;
        }
        Toast.makeText(this, R.string.jc_toast_join, Toast.LENGTH_SHORT).show();
    }
}
