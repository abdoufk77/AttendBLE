package com.example.attendble.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Wrapper {@link BluetoothLeScanner} : détecte les beacons AttendBLE et extrait (UUID, code).
 * Un beacon est reconnu si son ScanRecord contient un ServiceData de 2 octets — le code émis
 * par {@link Advertiser} (cf. encodage 2 octets big-endian).
 */
public class Scanner {

    private static final String TAG = "AttendBLE/Scanner";

    public interface Listener {
        /** Renvoie l'UUID du ServiceData et son contenu brut (à parser selon le canal). */
        void onBeaconDetected(UUID serviceUuid, byte[] data, int rssi);

        void onError(String message);
    }

    private final BluetoothAdapter adapter;
    private BluetoothLeScanner scanner;
    private ScanCallback currentCallback;
    private boolean scanning;

    public Scanner(Context context) {
        BluetoothManager mgr = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        adapter = mgr != null ? mgr.getAdapter() : null;
    }

    public boolean isSupported() {
        return adapter != null && adapter.getBluetoothLeScanner() != null;
    }

    public boolean isBluetoothEnabled() {
        return adapter != null && adapter.isEnabled();
    }

    @SuppressLint("MissingPermission")
    public void start(Listener listener) {
        start(null, listener);
    }

    /** Variante filtrée : ne reporte que les beacons portant le ServiceData {@code filterUuid}.
     * Un filtre côté radio est BEAUCOUP plus fiable que de filtrer dans le callback
     * (Android peut dédupliquer/throttler agressivement les résultats non filtrés). */
    @SuppressLint("MissingPermission")
    public void start(UUID filterUuid, Listener listener) {
        if (!isSupported()) {
            listener.onError("BLE Scan non supporté sur cet appareil");
            return;
        }
        if (!isBluetoothEnabled()) {
            listener.onError("Bluetooth désactivé");
            return;
        }
        if (scanning) {
            stop();
        }
        scanner = adapter.getBluetoothLeScanner();

        ScanSettings.Builder settingsBuilder = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setReportDelay(0L);
        // Mode "agressif" : remonter même un match faible (utile quand le prof
        // advertise et scanne en même temps, le radio est partagé).
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            settingsBuilder
                    .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                    .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT);
        }
        ScanSettings settings = settingsBuilder.build();

        List<ScanFilter> filters = null;
        if (filterUuid != null) {
            ScanFilter f = new ScanFilter.Builder()
                    .setServiceData(new ParcelUuid(filterUuid), new byte[0], new byte[0])
                    .build();
            filters = Collections.singletonList(f);
        }

        currentCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                if (result == null || result.getScanRecord() == null) return;
                Map<ParcelUuid, byte[]> serviceData = result.getScanRecord().getServiceData();
                if (serviceData == null) return;
                for (Map.Entry<ParcelUuid, byte[]> e : serviceData.entrySet()) {
                    byte[] data = e.getValue();
                    if (data != null && data.length > 0) {
                        UUID uuid = e.getKey().getUuid();
                        Log.d(TAG, "AttendBLE beacon: uuid=" + uuid + " dataLen=" + data.length + " rssi=" + result.getRssi());
                        listener.onBeaconDetected(uuid, data, result.getRssi());
                    }
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                scanning = false;
                Log.e(TAG, "Scan failed, errorCode=" + errorCode);
                listener.onError("Échec scan (code " + errorCode + ")");
            }
        };

        scanner.startScan(filters, settings, currentCallback);
        scanning = true;
        Log.d(TAG, "Scanning started, filterUuid=" + filterUuid);
    }

    @SuppressLint("MissingPermission")
    public void stop() {
        if (scanner != null && currentCallback != null && scanning) {
            try {
                scanner.stopScan(currentCallback);
            } catch (IllegalStateException ignored) {
            }
            Log.d(TAG, "Scanning stopped");
        }
        currentCallback = null;
        scanning = false;
    }

    public boolean isScanning() {
        return scanning;
    }
}
