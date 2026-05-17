package com.example.attendble.ui.prof.serve;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.attendble.R;
import com.example.attendble.ble.Advertiser;
import com.example.attendble.ble.BleConstants;
import com.example.attendble.ble.Scanner;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Session active : diffuse (beaconUUID, code) en BLE et scanne en parallèle les beacons
 * "réponse" des étudiants (RESPONSE_UUID + sessionShortId + numEtud) pour la liste live.
 */
public class ActiveSessionActivity extends AppCompatActivity {

    // Durée totale d'une session : 5 min. À l'expiration, la session se ferme
    // automatiquement (un seul code émis, pas de rotation).
    private static final long SESSION_TTL_MS = 5 * 60 * 1000L;
    private static final long TICK_MS = 1000L;

    private TextView tvCode;
    private TextView tvTimer;
    private TextView tvLiveCounter;
    private LinearLayout attendanceCardRoot;
    private CircularProgressIndicator progressTimer;

    private Advertiser advertiser;
    private Scanner responseScanner;
    private CountDownTimer countdown;
    private UUID beaconUUID;
    private int sessionShortId;
    private String currentCode;

    private final Set<Integer> detectedNumEtuds = new HashSet<>();
    private boolean liveListInitialized;
    private final Handler ui = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<String[]> blePermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (hasBlePermissions()) {
                    ensureBluetoothOnThenStart();
                } else {
                    Toast.makeText(this, R.string.as_perm_denied, Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    private final ActivityResultLauncher<Intent> enableBtLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    startSession();
                } else {
                    Toast.makeText(this, R.string.as_bt_required, Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_active_session);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        tvCode = findViewById(R.id.tv_code);
        tvTimer = findViewById(R.id.tv_timer);
        tvLiveCounter = findViewById(R.id.tv_live_counter);
        attendanceCardRoot = findViewById(R.id.attendance_card_root);
        progressTimer = findViewById(R.id.progress_timer);
        ((LinearProgressIndicator) findViewById(R.id.progress_live)).setProgressCompat(0, false);

        progressTimer.setMax(100);
        tvLiveCounter.setText(getString(R.string.as_live_counter_dynamic, 0));

        ((MaterialButton) findViewById(R.id.btn_end_session))
                .setOnClickListener(v -> endSession());

        beaconUUID = UUID.randomUUID();
        byte[] shortIdBytes = BleConstants.sessionShortId(beaconUUID);
        sessionShortId = ((shortIdBytes[0] & 0xFF) << 8) | (shortIdBytes[1] & 0xFF);

        if (hasBlePermissions()) {
            ensureBluetoothOnThenStart();
        } else {
            blePermLauncher.launch(requiredBlePermissions());
        }
    }

    private void ensureBluetoothOnThenStart() {
        advertiser = new Advertiser(this);
        if (!advertiser.isSupported()) {
            Toast.makeText(this, R.string.as_ble_unsupported, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (advertiser.isBluetoothEnabled()) {
            startSession();
        } else {
            enableBtLauncher.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
        }
    }

    private void startSession() {
        if (advertiser == null) {
            advertiser = new Advertiser(this);
        }
        startAdvertisingAndTimer();
        startResponseScan();
    }

    // Émet un code unique pour toute la session et démarre le compte à rebours global.
    // À l'expiration, la session se ferme automatiquement (pas de rotation de code).
    private void startAdvertisingAndTimer() {
        currentCode = generateCode();
        tvCode.setText(currentCode);

        advertiser.start(beaconUUID, currentCode, new Advertiser.Listener() {
            @Override
            public void onStarted() {
                // ok
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ActiveSessionActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });

        if (countdown != null) {
            countdown.cancel();
        }
        countdown = new CountDownTimer(SESSION_TTL_MS, TICK_MS) {
            @Override
            public void onTick(long msLeft) {
                int sec = (int) (msLeft / 1000L);
                tvTimer.setText(String.format(Locale.US, "%d:%02d", sec / 60, sec % 60));
                int pct = (int) (msLeft * 100L / SESSION_TTL_MS);
                progressTimer.setProgressCompat(pct, true);
            }

            @Override
            public void onFinish() {
                tvTimer.setText("0:00");
                progressTimer.setProgressCompat(0, true);
                endSession();
            }
        }.start();
    }

    private void startResponseScan() {
        responseScanner = new Scanner(this);
        if (!responseScanner.isSupported()) return;
        responseScanner.start(new Scanner.Listener() {
            @Override
            public void onBeaconDetected(UUID serviceUuid, byte[] data, int rssi) {
                if (!BleConstants.RESPONSE_UUID.equals(serviceUuid)) return;
                BleConstants.ResponsePayload p = BleConstants.decodeResponse(data);
                if (p == null) return;
                if (p.sessionShortId != sessionShortId) return;
                ui.post(() -> addDetected(p.numEtud));
            }

            @Override
            public void onError(String message) {
                ui.post(() -> Toast.makeText(ActiveSessionActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void addDetected(int numEtud) {
        if (!detectedNumEtuds.add(numEtud)) return;
        if (!liveListInitialized) {
            clearEmptyState();
            liveListInitialized = true;
        }
        appendStudentRow(numEtud);
        tvLiveCounter.setText(getString(R.string.as_live_counter_dynamic, detectedNumEtuds.size()));
    }

    /** Retire le placeholder "En attente…" avant d'insérer la première vraie ligne. */
    private void clearEmptyState() {
        View empty = findViewById(R.id.tv_attendance_empty);
        if (empty != null) {
            attendanceCardRoot.removeView(empty);
        }
    }

    private void appendStudentRow(int numEtud) {
        View divider = new View(this);
        LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (1 * getResources().getDisplayMetrics().density));
        divider.setLayoutParams(dp);
        divider.setBackgroundResource(R.color.brand_outline_variant);
        divider.setAlpha(0.2f);
        attendanceCardRoot.addView(divider);

        View row = LayoutInflater.from(this).inflate(R.layout.item_live_attendance, attendanceCardRoot, false);
        ((TextView) row.findViewById(R.id.tv_row_id)).setText(getString(R.string.as_live_id, numEtud));
        ((TextView) row.findViewById(R.id.tv_row_time))
                .setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        attendanceCardRoot.addView(row);
    }

    private void endSession() {
        stopBleAndTimer();
        Intent i = new Intent(this, SessionClosedActivity.class);
        i.putExtra(SessionClosedActivity.EXTRA_CLASSE_ID, getIntent().getStringExtra("classeId"));
        int[] arr = new int[detectedNumEtuds.size()];
        int k = 0;
        for (Integer n : detectedNumEtuds) arr[k++] = n;
        i.putExtra(SessionClosedActivity.EXTRA_DETECTED_NUM_ETUDS, arr);
        startActivity(i);
        finish();
    }

    private void stopBleAndTimer() {
        if (countdown != null) {
            countdown.cancel();
            countdown = null;
        }
        if (advertiser != null) {
            advertiser.stop();
        }
        if (responseScanner != null) {
            responseScanner.stop();
        }
    }

    @Override
    protected void onDestroy() {
        stopBleAndTimer();
        super.onDestroy();
    }

    private static String generateCode() {
        return String.format(Locale.US, "%04d", new Random().nextInt(10000));
    }

    private String[] requiredBlePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new String[]{
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            };
        }
        return new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
    }

    private boolean hasBlePermissions() {
        for (String p : requiredBlePermissions()) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
