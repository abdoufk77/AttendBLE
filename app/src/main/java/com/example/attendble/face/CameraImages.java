package com.example.attendble.face;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageProxy;

/**
 * Utilitaire CameraX : convertit un ImageProxy en Bitmap "upright" (rotation appliquée),
 * prêt à être passé à ML Kit et à FaceEmbedder dans le même repère.
 */
public final class CameraImages {

    private CameraImages() {}

    public static @NonNull Bitmap toUprightBitmap(@NonNull ImageProxy proxy) {
        Bitmap raw = proxy.toBitmap();
        int rotation = proxy.getImageInfo().getRotationDegrees();
        if (rotation == 0) return raw;
        Matrix m = new Matrix();
        m.postRotate(rotation);
        Bitmap upright = Bitmap.createBitmap(raw, 0, 0, raw.getWidth(), raw.getHeight(), m, true);
        if (upright != raw) raw.recycle();
        return upright;
    }
}
