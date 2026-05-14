package com.example.attendble.ui.student.scan;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.attendble.R;
import com.example.attendble.ui.student.scan.face.FaceVerificationActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

/**
 * Saisie du code 4 chiffres. Le code attendu est passé en extra ({@link #EXTRA_EXPECTED_CODE})
 * depuis SearchingActivity (extrait du payload BLE). Match → FaceVerification, sinon toast.
 */
public class SessionCodeActivity extends AppCompatActivity {

    public static final String EXTRA_EXPECTED_CODE = "extra_expected_code";
    public static final String EXTRA_BEACON_UUID = "extra_beacon_uuid";

    private EditText[] digits;
    private MaterialButton btnConfirm;
    private String expectedCode;
    private String beaconUUID;

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

        expectedCode = getIntent().getStringExtra(EXTRA_EXPECTED_CODE);
        beaconUUID = getIntent().getStringExtra(EXTRA_BEACON_UUID);

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
        btnConfirm.setOnClickListener(v -> onConfirm());

        findViewById(R.id.btn_rescan).setOnClickListener(v -> finish());

        digits[0].requestFocus();
    }

    private void onConfirm() {
        String typed = readCode();
        if (expectedCode == null) {
            Toast.makeText(this, R.string.sc_no_session, Toast.LENGTH_LONG).show();
            return;
        }
        if (!expectedCode.equals(typed)) {
            Toast.makeText(this, R.string.sc_invalid_code, Toast.LENGTH_SHORT).show();
            clearDigits();
            return;
        }
        Intent intent = new Intent(this, FaceVerificationActivity.class);
        intent.putExtra(EXTRA_BEACON_UUID, beaconUUID);
        startActivity(intent);
        finish();
    }

    private String readCode() {
        StringBuilder sb = new StringBuilder(4);
        for (EditText d : digits) sb.append(d.getText());
        return sb.toString();
    }

    private void clearDigits() {
        for (EditText d : digits) d.setText("");
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
