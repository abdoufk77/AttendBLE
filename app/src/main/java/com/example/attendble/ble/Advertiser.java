package com.example.attendble.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.UUID;

/**
 * Wrapper {@link BluetoothLeAdvertiser} : diffuse (beaconUUID, code 4 chiffres) en BLE.
 * Le code est encodé sur 2 octets dans le ServiceData associé au beaconUUID.
 */
public class Advertiser {

    private static final String TAG = "AttendBLE/Advertiser";

    public interface Listener {
        void onStarted();

        void onError(String message);
    }

    private final BluetoothAdapter adapter;
    private BluetoothLeAdvertiser advertiser;
    private AdvertiseCallback currentCallback;
    private boolean advertising;

    public Advertiser(Context context) {
        BluetoothManager mgr = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        adapter = mgr != null ? mgr.getAdapter() : null;
    }

    /** true si l'appareil possède un module BLE Advertise (indépendant du on/off). */
    public boolean isSupported() {
        return adapter != null && adapter.getBluetoothLeAdvertiser() != null;
    }

    public boolean isBluetoothEnabled() {
        return adapter != null && adapter.isEnabled();
    }

    /** Variante "code prof" : 4 chiffres encodés sur 2 octets. */
    public void start(UUID beaconUUID, String code, Listener listener) {
        start(beaconUUID, encodeCode(code), listener);
    }

    @SuppressLint("MissingPermission")
    public void start(UUID beaconUUID, byte[] payload, Listener listener) {
        if (!isSupported()) {
            listener.onError("BLE Advertise non supporté sur cet appareil");
            return;
        }
        if (!isBluetoothEnabled()) {
            listener.onError("Bluetooth désactivé");
            return;
        }
        advertiser = adapter.getBluetoothLeAdvertiser();
        if (advertising) {
            stop();
        }

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(false)
                .build();

        ParcelUuid pUuid = new ParcelUuid(beaconUUID);
        // NB: pas de addServiceUuid (UUID 128-bit déjà inclus dans ServiceData)
        // sinon le payload dépasse les 31 octets autorisés (ADVERTISE_FAILED_DATA_TOO_LARGE).
        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .addServiceData(pUuid, payload)
                .build();

        currentCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                advertising = true;
                Log.d(TAG, "Advertising started, payloadLen=" + payload.length + " uuid=" + beaconUUID);
                listener.onStarted();
            }

            @Override
            public void onStartFailure(int errorCode) {
                advertising = false;
                Log.e(TAG, "Advertising failed, errorCode=" + errorCode);
                listener.onError("Échec advertise (code " + errorCode + ")");
            }
        };

        advertiser.startAdvertising(settings, data, currentCallback);
    }

    @SuppressLint("MissingPermission")
    public void stop() {
        if (advertiser != null && currentCallback != null) {
            advertiser.stopAdvertising(currentCallback);
            Log.d(TAG, "Advertising stopped");
        }
        currentCallback = null;
        advertising = false;
    }

    public boolean isAdvertising() {
        return advertising;
    }

    /** Code "0000".."9999" → 2 octets big-endian. */
    static byte[] encodeCode(String code) {
        int n;
        try {
            n = Integer.parseInt(code) & 0xFFFF;
        } catch (NumberFormatException e) {
            n = 0;
        }
        return new byte[]{(byte) ((n >> 8) & 0xFF), (byte) (n & 0xFF)};
    }
}