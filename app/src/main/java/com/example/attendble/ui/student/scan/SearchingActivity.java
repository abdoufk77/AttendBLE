package com.example.attendble.ui.student.scan;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
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
import com.example.attendble.ble.Scanner;
import com.example.attendble.ui.student.classes.MyClassesActivity;
import com.example.attendble.ui.student.home.HomeActivity;
import com.example.attendble.ui.student.profil.ProfilActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;
import java.util.UUID;

/**
 * Écran de scan BLE étudiant : 3 anneaux pulsés pendant que le {@link Scanner} cherche un
 * beacon AttendBLE. Sur première détection → SessionCodeActivity avec (uuid, expectedCode).
 */
public class SearchingActivity extends AppCompatActivity {

    private static final long PULSE_DURATION_MS = 1800L;

    private Scanner scanner;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean beaconHandled;

    private final ActivityResultLauncher<String[]> blePermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (hasBlePermissions()) {
                    ensureBluetoothOnThenScan();
                } else {
                    Toast.makeText(this, R.string.searching_perm_denied, Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    private final ActivityResultLauncher<Intent> enableBtLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    startScan();
                } else {
                    Toast.makeText(this, R.string.searching_bt_required, Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_searching);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        MaterialButton btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        startPulse(findViewById(R.id.ring_inner), 0L);
        startPulse(findViewById(R.id.ring_middle), 600L);
        startPulse(findViewById(R.id.ring_outer), 1200L);

        bindBottomNav();

        if (hasBlePermissions()) {
            ensureBluetoothOnThenScan();
        } else {
            blePermLauncher.launch(requiredBlePermissions());
        }
    }

    private void ensureBluetoothOnThenScan() {
        scanner = new Scanner(this);
        if (!scanner.isSupported()) {
            Toast.makeText(this, R.string.searching_ble_unsupported, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (scanner.isBluetoothEnabled()) {
            startScan();
        } else {
            enableBtLauncher.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
        }
    }

    private void startScan() {
        if (scanner == null) {
            scanner = new Scanner(this);
        }
        scanner.start(new Scanner.Listener() {
            @Override
            public void onBeaconDetected(UUID serviceUuid, byte[] data, int rssi) {
                // Canal "session prof" : payload 2 octets = code 4 chiffres
                if (data.length != 2) return;
                int code = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
                String codeStr = String.format(Locale.US, "%04d", code);
                handler.post(() -> handleBeacon(serviceUuid, codeStr));
            }

            @Override
            public void onError(String message) {
                handler.post(() ->
                        Toast.makeText(SearchingActivity.this, message, Toast.LENGTH_LONG).show());
            }
        });
    }

    private void handleBeacon(UUID beaconUUID, String code) {
        if (beaconHandled || isFinishing() || isDestroyed()) return;
        beaconHandled = true;
        scanner.stop();

        Intent intent = new Intent(this, SessionCodeActivity.class);
        intent.putExtra(SessionCodeActivity.EXTRA_BEACON_UUID, beaconUUID.toString());
        intent.putExtra(SessionCodeActivity.EXTRA_EXPECTED_CODE, code);
        startActivity(intent);
        finish();
    }

    private void startPulse(View ring, long startDelay) {
        ring.setScaleX(0.6f);
        ring.setScaleY(0.6f);
        ring.setAlpha(0f);
        ring.animate().cancel();
        ring.postDelayed(() -> loopPulse(ring), startDelay);
    }

    private void loopPulse(View ring) {
        if (isFinishing() || isDestroyed()) return;
        ring.setScaleX(0.6f);
        ring.setScaleY(0.6f);
        ring.setAlpha(0.4f);
        ring.animate()
                .scaleX(1.6f)
                .scaleY(1.6f)
                .alpha(0f)
                .setDuration(PULSE_DURATION_MS)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> loopPulse(ring))
                .start();
    }

    @Override
    protected void onDestroy() {
        if (scanner != null) {
            scanner.stop();
        }
        super.onDestroy();
    }

    private String[] requiredBlePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADVERTISE
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

    private void bindBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(R.id.nav_scan);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_scan) {
                return true;
            }
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_classes) {
                startActivity(new Intent(this, MyClassesActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfilActivity.class));
                finish();
                return true;
            }
            Toast.makeText(this, R.string.student_toast_nav, Toast.LENGTH_SHORT).show();
            return true;
        });
    }
}
