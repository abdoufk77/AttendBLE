package com.example.attendble.ui.student.scan;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.attendble.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

/**
 * Écran de saisie du code de session (étape 2 du flow scan étudiant) : beacon détecté +
 * 4 chiffres à saisir. À brancher via ValiderPresenceUseCase(sessionId, etudiantId, code).
 */
public class SessionCodeActivity extends AppCompatActivity {

    private EditText[] digits;
    private MaterialButton btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_session_code);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        MaterialToolbar topbar = findViewById(R.id.topbar);
        topbar.setNavigationOnClickListener(v -> finish());

        digits = new EditText[]{
                findViewById(R.id.et_digit_1),
                findViewById(R.id.et_digit_2),
                findViewById(R.id.et_digit_3),
                findViewById(R.id.et_digit_4)
        };
        wireDigitFields();

        btnConfirm = findViewById(R.id.btn_confirm);
        // DEMO: ouvre directement l'écran de succès — à remplacer par
        // ValiderPresenceUseCase.execute(...) une fois la couche métier branchée.
        btnConfirm.setOnClickListener(v -> {
            startActivity(new Intent(this, AttendanceSuccessActivity.class));
            finish();
        });

        findViewById(R.id.btn_rescan).setOnClickListener(v -> finish());

        digits[0].requestFocus();
    }

    private void wireDigitFields() {
        for (int i = 0; i < digits.length; i++) {
            final int index = i;
            digits[i].addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < digits.length - 1) {
                        digits[index + 1].requestFocus();
                    }
                    btnConfirm.setEnabled(isCodeComplete());
                }

                @Override public void afterTextChanged(Editable s) { }
            });

            digits[i].setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && keyCode == KeyEvent.KEYCODE_DEL
                        && digits[index].getText().length() == 0
                        && index > 0) {
                    digits[index - 1].requestFocus();
                    digits[index - 1].setText("");
                    return true;
                }
                return false;
            });
        }
    }

    private boolean isCodeComplete() {
        for (EditText d : digits) {
            if (d.getText().length() != 1) return false;
        }
        return true;
    }
}
