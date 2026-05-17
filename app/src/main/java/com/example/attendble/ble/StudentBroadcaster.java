package com.example.attendble.ble;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.UUID;

/**
 * Singleton qui pilote le broadcast BLE de l'étudiant à travers plusieurs activités
 * (SessionCode → FaceVerif → AttendanceSuccess) sans interruption.
 *
 * Cycle de vie :
 *   1. {@link #startPending} dès que l'étudiant clique "Établir la connexion".
 *   2. {@link #promoteToConfirmed} après validation code + face.
 *   3. Auto-stop après {@link BleConstants#RESPONSE_BROADCAST_MS} depuis le dernier change
 *      (le prof a eu largement le temps de capter).
 */
public final class StudentBroadcaster {

    private static final String TAG = "AttendBLE/StudentBroadcaster";

    private static StudentBroadcaster INSTANCE;

    public static synchronized StudentBroadcaster get() {
        if (INSTANCE == null) INSTANCE = new StudentBroadcaster();
        return INSTANCE;
    }

    private Advertiser advertiser;
    private final Handler ui = new Handler(Looper.getMainLooper());
    private Runnable scheduledStop;

    private UUID beaconUUID;
    private byte[] shortId;
    private String numEtud;
    private int currentStatus = -1;

    private StudentBroadcaster() {
    }

    /** Démarre (ou redémarre) le broadcast en mode PENDING. Idempotent si déjà pending. */
    public synchronized void startPending(Context appContext, UUID beaconUUID, String numEtud) {
        if (this.beaconUUID != null && this.beaconUUID.equals(beaconUUID)
                && currentStatus == BleConstants.STATUS_PENDING) {
            Log.d(TAG, "startPending: déjà en pending, no-op");
            return;
        }
        this.beaconUUID = beaconUUID;
        this.shortId = BleConstants.sessionShortId(beaconUUID);
        this.numEtud = numEtud;
        Log.i(TAG, "startPending uuid=" + beaconUUID + " numEtud=" + numEtud);
        startInternal(appContext.getApplicationContext(), BleConstants.STATUS_PENDING);
    }

    /** Passe le broadcast en mode CONFIRMED (après code + face validés). */
    public synchronized void promoteToConfirmed(Context appContext) {
        if (beaconUUID == null || numEtud == null) {
            Log.w(TAG, "promoteToConfirmed: pas de broadcast en cours, on ignore");
            return;
        }
        Log.i(TAG, "promoteToConfirmed numEtud=" + numEtud);
        startInternal(appContext.getApplicationContext(), BleConstants.STATUS_CONFIRMED);
    }

    /** Arrête immédiatement le broadcast (timeout interne ou cancel programmatique).
     * Garde l'identité de la séance/étudiant pour qu'un éventuel promoteToConfirmed
     * arrivant tard puisse redémarrer le broadcast avec le bon payload. */
    public synchronized void stop() {
        if (scheduledStop != null) {
            ui.removeCallbacks(scheduledStop);
            scheduledStop = null;
        }
        if (advertiser != null) {
            advertiser.stop();
            advertiser = null;
        }
        currentStatus = -1;
        Log.d(TAG, "stop (identité conservée pour éventuel promote)");
    }

    /** Reset complet : à appeler quand l'étudiant abandonne (rescan, retour home, etc.). */
    public synchronized void cancel() {
        stop();
        beaconUUID = null;
        numEtud = null;
        Log.d(TAG, "cancel — identité effacée");
    }

    private void startInternal(Context appContext, int status) {
        // Stop l'advertise précédent s'il y en a un (Android n'autorise qu'un advertise actif par
        // BluetoothLeAdvertiser et il faut un setting fresh pour changer le payload).
        if (advertiser != null) {
            advertiser.stop();
            advertiser = null;
        }
        if (scheduledStop != null) {
            ui.removeCallbacks(scheduledStop);
        }

        advertiser = new Advertiser(appContext);
        if (!advertiser.isSupported() || !advertiser.isBluetoothEnabled()) {
            Log.w(TAG, "Advertise indisponible, abandon");
            advertiser = null;
            return;
        }

        byte[] payload = BleConstants.encodeResponse(shortId, numEtud, status);
        currentStatus = status;
        advertiser.start(BleConstants.RESPONSE_UUID, payload, new Advertiser.Listener() {
            @Override
            public void onStarted() {
                Log.d(TAG, "Advertise started status=" + status + " payloadLen=" + payload.length);
            }

            @Override
            public void onError(String message) {
                Log.w(TAG, "Advertise error: " + message);
            }
        });

        scheduledStop = this::stop;
        ui.postDelayed(scheduledStop, BleConstants.RESPONSE_BROADCAST_MS);
    }
}
