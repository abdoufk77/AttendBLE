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
import android.util.Log;
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
import com.example.attendble.data.ServiceLocator;
import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.EtudiantAttendance;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Session active : diffuse (beaconUUID, code) en BLE et scanne en parallèle les beacons
 * "réponse" des étudiants (RESPONSE_UUID + sessionShortId + numEtud) pour la liste live.
 */
public class ActiveSessionActivity extends AppCompatActivity {

    private static final String TAG = "AttendBLE/ActiveSession";

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
    /** numEtud (int) → nom complet. Préchargé au démarrage de la session pour permettre
     * de résoudre les beacons reçus en noms sans appel réseau pendant le pointage. */
    private final Map<Integer, String> numEtudToName = new HashMap<>();
    /** numEtud → row View pour pouvoir basculer "pending → confirmed" (gris → vert)
     * quand l'étudiant termine sa face verif. */
    private final Map<Integer, View> rowByNumEtud = new HashMap<>();
    /** numEtud déjà passés en CONFIRMED — évite de re-basculer (et n'incrémente le compteur qu'une fois). */
    private final Set<Integer> confirmedNumEtuds = new HashSet<>();
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

        preloadClasseMembers();

        if (hasBlePermissions()) {
            ensureBluetoothOnThenStart();
        } else {
            blePermLauncher.launch(requiredBlePermissions());
        }
    }

    /** Précharge la table {numEtud → nom} via le backend pour qu'on puisse afficher
     * un vrai nom dans la liste live (offline pendant le pointage : tout est en mémoire). */
    private void preloadClasseMembers() {
        String classeId = getIntent().getStringExtra("classeId");
        if (classeId == null) {
            Log.w(TAG, "Pas de classeId : impossible de précharger les membres");
            return;
        }
        ServiceLocator.provideListEtudiantsByClasseUseCase().execute(classeId,
                new Callback<List<EtudiantAttendance>>() {
                    @Override
                    public void onSuccess(List<EtudiantAttendance> list) {
                        for (EtudiantAttendance ea : list) {
                            String numStr = ea.getEtudiant().getNumEtud();
                            if (numStr == null) continue;
                            try {
                                int num = Integer.parseInt(numStr.trim());
                                numEtudToName.put(num, ea.getEtudiant().getNom());
                            } catch (NumberFormatException ignored) {
                            }
                        }
                        Log.i(TAG, "Préchargé " + numEtudToName.size() + " membre(s) pour classeId=" + classeId);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.w(TAG, "Échec préchargement membres: " + e.getMessage());
                    }
                });
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
        Log.i(TAG, "Starting response scan: filter=" + BleConstants.RESPONSE_UUID
                + " mySessionShortId=" + sessionShortId);
        // Filtre radio sur RESPONSE_UUID : beaucoup plus fiable que filtrer après coup
        // (Android dédup/throttle quand on scanne sans filtre).
        responseScanner.start(BleConstants.RESPONSE_UUID, new Scanner.Listener() {
            @Override
            public void onBeaconDetected(UUID serviceUuid, byte[] data, int rssi) {
                if (!BleConstants.RESPONSE_UUID.equals(serviceUuid)) return;
                BleConstants.ResponsePayload p = BleConstants.decodeResponse(data);
                if (p == null) {
                    Log.w(TAG, "Response payload mal formé, len=" + (data == null ? 0 : data.length));
                    return;
                }
                if (p.sessionShortId != sessionShortId) {
                    Log.d(TAG, "Response ignorée (autre session) shortId=" + p.sessionShortId
                            + " numEtud=" + p.numEtud);
                    return;
                }
                Log.i(TAG, "Response OK numEtud=" + p.numEtud + " status=" + p.status + " rssi=" + rssi);
                ui.post(() -> handleResponse(p.numEtud, p.status));
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Scanner error: " + message);
                ui.post(() -> Toast.makeText(ActiveSessionActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    /** Gère un beacon réponse : nouveau pending → on ajoute une ligne grisée ; confirmation
     * d'une ligne existante → on la passe en vert ; pending déjà vu → no-op. */
    private void handleResponse(int numEtud, int status) {
        boolean newlyDetected = detectedNumEtuds.add(numEtud);
        if (newlyDetected) {
            if (!liveListInitialized) {
                clearEmptyState();
                liveListInitialized = true;
            }
            appendStudentRow(numEtud, status);
        }
        if (status == BleConstants.STATUS_CONFIRMED && confirmedNumEtuds.add(numEtud)) {
            promoteRowToConfirmed(numEtud);
        }
        // Compteur live : on ne compte que les confirmés (pending ≠ encore "présent").
        tvLiveCounter.setText(getString(R.string.as_live_counter_dynamic, confirmedNumEtuds.size()));
    }

    /** Retire le placeholder "En attente…" avant d'insérer la première vraie ligne. */
    private void clearEmptyState() {
        View empty = findViewById(R.id.tv_attendance_empty);
        if (empty != null) {
            attendanceCardRoot.removeView(empty);
        }
    }

    private void appendStudentRow(int numEtud, int status) {
        View divider = new View(this);
        LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (1 * getResources().getDisplayMetrics().density));
        divider.setLayoutParams(dp);
        divider.setBackgroundResource(R.color.brand_outline_variant);
        divider.setAlpha(0.2f);
        attendanceCardRoot.addView(divider);

        View row = LayoutInflater.from(this).inflate(R.layout.item_live_attendance, attendanceCardRoot, false);
        String name = numEtudToName.get(numEtud);
        TextView tvName = row.findViewById(R.id.tv_row_name);
        if (name != null && !name.isEmpty()) {
            tvName.setText(name);
        }
        ((TextView) row.findViewById(R.id.tv_row_id)).setText(getString(R.string.as_live_id, numEtud));
        ((TextView) row.findViewById(R.id.tv_row_time))
                .setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        // En pending : ligne grisée + texte "En cours de pointage…"
        if (status != BleConstants.STATUS_CONFIRMED) {
            row.setAlpha(0.5f);
            tvName.setText(getString(R.string.as_live_pending,
                    name != null && !name.isEmpty() ? name : ""));
        }
        attendanceCardRoot.addView(row);
        rowByNumEtud.put(numEtud, row);
    }

    /** Passe la ligne pending → confirmée (opacité 1 + nom plein + icône verte déjà dans le layout). */
    private void promoteRowToConfirmed(int numEtud) {
        View row = rowByNumEtud.get(numEtud);
        if (row == null) return;
        row.setAlpha(1f);
        TextView tvName = row.findViewById(R.id.tv_row_name);
        String name = numEtudToName.get(numEtud);
        if (name != null && !name.isEmpty()) {
            tvName.setText(name);
        } else {
            tvName.setText(R.string.as_live_present);
        }
        ((TextView) row.findViewById(R.id.tv_row_time))
                .setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    private void endSession() {
        stopBleAndTimer();
        Intent i = new Intent(this, SessionClosedActivity.class);
        i.putExtra(SessionClosedActivity.EXTRA_CLASSE_ID, getIntent().getStringExtra("classeId"));
        // Seuls les CONFIRMED (code + face validés) comptent comme présents. Les pending
        // qui n'ont jamais reçu de confirmation sont considérés comme absents.
        int[] arr = new int[confirmedNumEtuds.size()];
        int k = 0;
        for (Integer n : confirmedNumEtuds) arr[k++] = n;
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
