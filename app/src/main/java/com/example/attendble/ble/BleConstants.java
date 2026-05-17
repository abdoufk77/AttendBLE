package com.example.attendble.ble;

import java.util.UUID;

/**
 * UUIDs et constantes partagées entre Advertiser/Scanner côté prof et étudiant.
 * RESPONSE_UUID est commun à tous les téléphones AttendBLE — c'est le canal sur lequel
 * les étudiants diffusent leur "pointage" après vérification.
 */
public final class BleConstants {

    /** UUID hardcodé pour le canal retour étudiant → prof. */
    public static final UUID RESPONSE_UUID =
            UUID.fromString("aabbccdd-1234-5678-9abc-def012345678");

    /** Durée pendant laquelle l'étudiant diffuse son pointage après succès.
     * Long pour maximiser les chances que le scanner du prof (qui partage le radio
     * avec son propre advertise) le capte. */
    public static final long RESPONSE_BROADCAST_MS = 45_000L;

    /** Taille du payload pointage : 2B sessionShortId + 4B numEtud + 1B status. */
    public static final int RESPONSE_PAYLOAD_LEN = 7;
    /** Anciennes versions du client : 6 octets sans le byte status (assumé CONFIRMED). */
    public static final int RESPONSE_PAYLOAD_LEN_LEGACY = 6;

    /** Étudiant a détecté la séance, broadcast initié mais code+face pas encore validés. */
    public static final int STATUS_PENDING = 0;
    /** Étudiant a validé code + face → présence confirmée. */
    public static final int STATUS_CONFIRMED = 1;

    private BleConstants() {
    }

    /** Réduit un beaconUUID à un identifiant court 2-octets (derniers bytes du least-sig). */
    public static byte[] sessionShortId(UUID beaconUUID) {
        long lsb = beaconUUID.getLeastSignificantBits();
        return new byte[]{(byte) ((lsb >> 8) & 0xFF), (byte) (lsb & 0xFF)};
    }

    /** Encode (sessionShortId 2B + numEtud 4B + status 1B). numEtud parsé en int, fallback 0. */
    public static byte[] encodeResponse(byte[] shortId, String numEtud, int status) {
        int n = 0;
        try {
            n = Integer.parseInt(numEtud);
        } catch (NumberFormatException ignored) {
        }
        byte[] out = new byte[RESPONSE_PAYLOAD_LEN];
        out[0] = shortId[0];
        out[1] = shortId[1];
        out[2] = (byte) ((n >> 24) & 0xFF);
        out[3] = (byte) ((n >> 16) & 0xFF);
        out[4] = (byte) ((n >> 8) & 0xFF);
        out[5] = (byte) (n & 0xFF);
        out[6] = (byte) (status & 0xFF);
        return out;
    }

    /** Décode un payload retour ; renvoie null si mauvaise taille.
     * Accepte aussi l'ancien format 6 octets (sans status), interprété comme CONFIRMED. */
    public static ResponsePayload decodeResponse(byte[] data) {
        if (data == null) return null;
        if (data.length != RESPONSE_PAYLOAD_LEN && data.length != RESPONSE_PAYLOAD_LEN_LEGACY) {
            return null;
        }
        int shortId = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
        int numEtud = ((data[2] & 0xFF) << 24)
                | ((data[3] & 0xFF) << 16)
                | ((data[4] & 0xFF) << 8)
                | (data[5] & 0xFF);
        int status = data.length == RESPONSE_PAYLOAD_LEN ? (data[6] & 0xFF) : STATUS_CONFIRMED;
        return new ResponsePayload(shortId, numEtud, status);
    }

    public static final class ResponsePayload {
        public final int sessionShortId;
        public final int numEtud;
        public final int status;

        public ResponsePayload(int sessionShortId, int numEtud, int status) {
            this.sessionShortId = sessionShortId;
            this.numEtud = numEtud;
            this.status = status;
        }
    }
}
