package com.example.attendble.ui.student.scan;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.attendble.R;
import com.example.attendble.ble.StudentBroadcaster;
import com.example.attendble.data.ServiceLocator;
import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.Etudiant;
import com.example.attendble.domain.model.User;
import com.example.attendble.ui.student.scan.face.FaceVerificationActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.UUID;

/**
 * Étape 2 du pointage : confirmation visuelle de la séance (vraies infos classe préchargées
 * dans l'intent) → bouton "Établir la connexion" qui démarre le broadcast BLE PENDING (le prof
 * voit "En cours de pointage") et déverrouille la saisie du code. Le broadcast continue
 * automatiquement à travers FaceVerification puis passe en CONFIRMED dans AttendanceSuccess.
 */
public class SessionCodeActivity extends AppCompatActivity {

    private static final String TAG = "SessionCode";

    public static final String EXTRA_EXPECTED_CODE = "extra_expected_code";
    public static final String EXTRA_BEACON_UUID = "extra_beacon_uuid";
    public static final String EXTRA_CLASSE_NAME = "extra_classe_name";
    public static final String EXTRA_CLASSE_META = "extra_classe_meta";

    private EditText[] digits;
    private MaterialButton btnConfirm;
    private MaterialButton btnEstablish;
    private View tvHeading;
    private View tvSubtitle;
    private View codeRow;
    private String expectedCode;
    private String beaconUUID;
    private boolean connectionEstablished;

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

        // Card session : vraies infos passées par SearchingActivity (préchargées online).
        String classeName = getIntent().getStringExtra(EXTRA_CLASSE_NAME);
        String classeMeta = getIntent().getStringExtra(EXTRA_CLASSE_META);
        if (classeName != null && !classeName.isEmpty()) {
            ((TextView) findViewById(R.id.tv_session_name)).setText(classeName);
        }
        if (classeMeta != null && !classeMeta.isEmpty()) {
            ((TextView) findViewById(R.id.tv_session_meta)).setText(classeMeta);
        }

        digits = new EditText[]{
                findViewById(R.id.et_digit_1),
                findViewById(R.id.et_digit_2),
                findViewById(R.id.et_digit_3),
                findViewById(R.id.et_digit_4)
        };
        wireDigitFields();

        btnConfirm = findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(v -> onConfirm());

        btnEstablish = findViewById(R.id.btn_establish);
        tvHeading = findViewById(R.id.tv_code_heading);
        tvSubtitle = findViewById(R.id.tv_code_subtitle);
        codeRow = findViewById(R.id.code_cells_row);

        lockCodeInput();
        btnEstablish.setOnClickListener(v -> establishConnection());

        findViewById(R.id.btn_rescan).setOnClickListener(v -> {
            StudentBroadcaster.get().cancel();
            finish();
        });
    }

    /** Tant que la "connexion" n'est pas établie, on grise et on désactive la zone code. */
    private void lockCodeInput() {
        for (EditText d : digits) {
            d.setEnabled(false);
            d.setFocusable(false);
        }
        btnConfirm.setEnabled(false);
    }

    private void unlockCodeInput() {
        for (EditText d : digits) {
            d.setEnabled(true);
            d.setFocusableInTouchMode(true);
        }
        tvHeading.setAlpha(1f);
        tvSubtitle.setAlpha(1f);
        codeRow.setAlpha(1f);
        digits[0].requestFocus();
    }

    /** Lance le broadcast PENDING et simule un délai de "connexion" pour stabiliser
     * le radio BLE (Android settle son scanner pendant ces 2 secondes). */
    private void establishConnection() {
        if (connectionEstablished) return;
        if (beaconUUID == null) {
            Toast.makeText(this, R.string.sc_no_session, Toast.LENGTH_LONG).show();
            return;
        }
        UUID uuid;
        try {
            uuid = UUID.fromString(beaconUUID);
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, R.string.sc_no_session, Toast.LENGTH_LONG).show();
            return;
        }

        btnEstablish.setEnabled(false);
        btnEstablish.setText(R.string.ssc_establishing);

        // Démarre le broadcast PENDING dès maintenant : le prof voit l'étudiant en gris
        // ("En cours de pointage") avant même qu'il ait saisi le code. Donne 30-60s au radio
        // du prof pour capter, au lieu d'attendre la fin du flux.
        ServiceLocator.getAuthRepository().getCurrentUser(new Callback<User>() {
            @Override
            public void onSuccess(User user) {
                if (!(user instanceof Etudiant)) {
                    Log.w(TAG, "User courant n'est pas un Etudiant");
                    finishEstablishing();
                    return;
                }
                String numEtud = ((Etudiant) user).getNumEtud();
                StudentBroadcaster.get().startPending(getApplicationContext(), uuid, numEtud);
                Log.i(TAG, "Pending broadcast initié pour numEtud=" + numEtud);

                // Petit délai UX pour que l'utilisateur voie le passage "Connecting…" → "Connected"
                // et pour laisser le radio BLE se stabiliser.
                new Handler(Looper.getMainLooper()).postDelayed(
                        SessionCodeActivity.this::finishEstablishing, 2_000L);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "getCurrentUser failed", e);
                finishEstablishing();
            }
        });
    }

    private void finishEstablishing() {
        connectionEstablished = true;
        btnEstablish.setText(R.string.ssc_established);
        unlockCodeInput();
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
                    btnConfirm.setEnabled(connectionEstablished && isCodeComplete());
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
