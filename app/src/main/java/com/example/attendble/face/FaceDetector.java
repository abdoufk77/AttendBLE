package com.example.attendble.face;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;

/**
 * Wrapper ML Kit pour la détection de visage on-device. Expose le visage principal
 * (le plus grand) avec ses Euler angles (X/Y/Z), probabilités yeux/sourire et bounding box.
 * Mode ACCURATE + classifications ALL → optimal pour liveness, plus lent qu'un mode FAST
 * mais largement suffisant à la cadence d'analyse (10 fps).
 */
public class FaceDetector {

    public interface Listener {
        /**
         * Appelé pour chaque frame analysée.
         * @param face          visage principal détecté ({@code null} si aucun)
         * @param totalFaces    nombre total de visages détectés dans la frame
         */
        void onFace(Face face, int totalFaces);

        void onError(Exception e);
    }

    private final com.google.mlkit.vision.face.FaceDetector detector;

    public FaceDetector() {
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
                .setMinFaceSize(0.25f)
                .build();
        detector = FaceDetection.getClient(options);
    }

    /** Analyse un bitmap déjà orienté upright. Les bounds renvoyés sont dans le repère du bitmap. */
    public void analyze(@NonNull Bitmap upright, @NonNull Listener listener) {
        InputImage input = InputImage.fromBitmap(upright, 0);
        detector.process(input)
                .addOnSuccessListener(faces -> {
                    int n = faces == null ? 0 : faces.size();
                    listener.onFace(pickLargest(faces), n);
                })
                .addOnFailureListener(listener::onError);
    }

    private Face pickLargest(List<Face> faces) {
        if (faces == null || faces.isEmpty()) return null;
        Face best = faces.get(0);
        int bestArea = area(best);
        for (int i = 1; i < faces.size(); i++) {
            int a = area(faces.get(i));
            if (a > bestArea) {
                best = faces.get(i);
                bestArea = a;
            }
        }
        return best;
    }

    private int area(Face f) {
        return f.getBoundingBox().width() * f.getBoundingBox().height();
    }

    public void close() {
        detector.close();
    }
}
