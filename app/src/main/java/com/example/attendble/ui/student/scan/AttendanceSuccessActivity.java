package com.example.attendble.ui.student.scan;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.attendble.R;
import com.example.attendble.ble.Advertiser;
import com.example.attendble.ble.BleConstants;
import com.example.attendble.data.ServiceLocator;
import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.Etudiant;
import com.example.attendble.domain.model.User;
import com.example.attendble.ui.student.classes.MyClassesActivity;
import com.example.attendble.ui.student.home.HomeActivity;

import java.util.UUID;

/**
 * Confirmation pointage. Diffuse en parallèle un beacon "réponse" pendant ~8s
 * (RESPONSE_UUID + sessionShortId + numEtud) pour que le prof voie l'étudiant en live.
 */
public class AttendanceSuccessActivity extends AppCompatActivity {

    public static final String EXTRA_BEACON_UUID = "extra_beacon_uuid";
    private static final String TAG = "AttendBLE/Response";

    private Advertiser advertiser;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_attendance_success);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        findViewById(R.id.btn_home).setOnClickListener(v -> goTo(HomeActivity.class));
        findViewById(R.id.btn_classes).setOnClickListener(v -> goTo(MyClassesActivity.class));

        broadcastResponseIfPossible();
    }

    private void broadcastResponseIfPossible() {
        String beaconUUIDStr = getIntent().getStringExtra(EXTRA_BEACON_UUID);
        if (beaconUUIDStr == null) {
            Log.w(TAG, "Pas de beaconUUID en extra, pas d'émission retour");
            return;
        }
        UUID beaconUUID;
        try {
            beaconUUID = UUID.fromString(beaconUUIDStr);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "beaconUUID invalide: " + beaconUUIDStr);
            return;
        }
        byte[] shortId = BleConstants.sessionShortId(beaconUUID);
        int shortIdInt = ((shortId[0] & 0xFF) << 8) | (shortId[1] & 0xFF);
        Log.i(TAG, "Préparation broadcast: beaconUUID=" + beaconUUID + " shortId=" + shortIdInt);

        ServiceLocator.getAuthRepository().getCurrentUser(new Callback<User>() {
            @Override
            public void onSuccess(User user) {
                if (!(user instanceof Etudiant)) {
                    Log.w(TAG, "User courant n'est pas un Etudiant, abandon broadcast");
                    return;
                }
                String numEtud = ((Etudiant) user).getNumEtud();
                Log.i(TAG, "Broadcast pour numEtud=" + numEtud + " pendant "
                        + BleConstants.RESPONSE_BROADCAST_MS + "ms");
                byte[] payload = BleConstants.encodeResponse(shortId, numEtud);
                startBroadcast(payload);
            }

            @Override
            public void onError(Exception e) {
                Log.w(TAG, "Impossible de récupérer l'étudiant: " + e.getMessage());
            }
        });
    }

    private void startBroadcast(byte[] payload) {
        advertiser = new Advertiser(this);
        if (!advertiser.isSupported() || !advertiser.isBluetoothEnabled()) {
            Log.w(TAG, "Advertise non disponible, retour BLE annulé");
            return;
        }
        advertiser.start(BleConstants.RESPONSE_UUID, payload, new Advertiser.Listener() {
            @Override
            public void onStarted() {
                Log.d(TAG, "Réponse étudiant: payloadLen=" + payload.length);
            }

            @Override
            public void onError(String message) {
                Log.w(TAG, "Réponse échouée: " + message);
            }
        });
        handler.postDelayed(this::stopBroadcast, BleConstants.RESPONSE_BROADCAST_MS);
    }

    private void stopBroadcast() {
        if (advertiser != null) {
            advertiser.stop();
            advertiser = null;
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        stopBroadcast();
        super.onDestroy();
    }

    private void goTo(Class<?> target) {
        Intent intent = new Intent(this, target);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
