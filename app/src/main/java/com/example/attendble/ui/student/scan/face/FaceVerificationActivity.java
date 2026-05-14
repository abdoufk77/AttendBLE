package com.example.attendble.ui.student.scan.face;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
import android.util.Size;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.core.resolutionselector.ResolutionStrategy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.attendble.R;
import com.example.attendble.data.ServiceLocator;
import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.Etudiant;
import com.example.attendble.domain.model.User;
import com.example.attendble.domain.repository.AuthRepository;
import com.example.attendble.face.CameraImages;
import com.example.attendble.face.FaceDetector;
import com.example.attendble.face.FaceEmbedder;
import com.example.attendble.face.FaceMatcher;
import com.example.attendble.ui.student.scan.AttendanceSuccessActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.face.Face;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 3e étape anti-fraude : vérification visuelle on-device après saisie du code.
 * Pipeline : CameraX → ML Kit (pose frontale stable) → MobileFaceNet (embedding)
 *   → comparaison cosinus avec le template stocké à l'enrôlement.
 */
public class FaceVerificationActivity extends AppCompatActivity {

    private static final String TAG = "FaceVerification";

    /** Pose frontale acceptable pour extraire l'embedding de comparaison. */
    private static final float FRONTAL_Y_MAX = 12f;
    private static final float FRONTAL_X_MAX = 12f;
    private static final float MIN_FACE_RATIO = 0.30f;
    private static final int STABLE_FRAMES_REQUIRED = 2;
    private static final long TIMEOUT_MS = 15_000L;
    private static final float MATCH_THRESHOLD = FaceMatcher.DEFAULT_THRESHOLD; // 0.70

    private PreviewView previewView;
    private TextView tvInstruction;
    private TextView tvProgressValue;
    private LinearProgressIndicator progress;

    private FaceDetector faceDetector;
    private FaceEmbedder faceEmbedder;
    private ExecutorService cameraExecutor;
    private final Handler ui = new Handler(Looper.getMainLooper());

    private float[] storedTemplate;
    private int consecutiveValidFrames = 0;
    private volatile boolean processingFrame = false;
    private volatile boolean finished = false;
    private long startTime;

    private final androidx.activity.result.ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) loadUserAndStart();
                else {
                    Toast.makeText(this, R.string.fv_permission_required, Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_face_verification);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        MaterialToolbar topbar = findViewById(R.id.topbar);
        topbar.setNavigationOnClickListener(v -> finishCancelled());

        previewView = findViewById(R.id.camera_preview);
        tvInstruction = findViewById(R.id.tv_instruction);
        tvProgressValue = findViewById(R.id.tv_progress_value);
        progress = findViewById(R.id.progress_analyzing);

        MaterialButton cancel = findViewById(R.id.btn_cancel);
        cancel.setOnClickListener(v -> finishCancelled());

        cameraExecutor = Executors.newSingleThreadExecutor();

        try {
            faceEmbedder = new FaceEmbedder(this);
        } catch (Exception e) {
            Log.e(TAG, "Failed to load MobileFaceNet model", e);
            goToError();
            return;
        }
        faceDetector = new FaceDetector();

        if (hasCameraPermission()) loadUserAndStart();
        else permissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void loadUserAndStart() {
        AuthRepository repo = ServiceLocator.getAuthRepository();
        repo.getCurrentUser(new Callback<User>() {
            @Override
            public void onSuccess(User user) {
                if (!(user instanceof Etudiant)) {
                    Log.e(TAG, "Current user is not an Etudiant");
                    goToError();
                    return;
                }
                float[] tpl = ((Etudiant) user).getFaceEmbedding();
                if (tpl == null || tpl.length == 0) {
                    tvInstruction.setText(R.string.fv_no_template);
                    Toast.makeText(FaceVerificationActivity.this,
                            R.string.fv_no_template, Toast.LENGTH_LONG).show();
                    ui.postDelayed(FaceVerificationActivity.this::goToError, 1500);
                    return;
                }
                storedTemplate = tpl;
                startTime = System.currentTimeMillis();
                startCamera();
                scheduleTimeout();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to load current user", e);
                goToError();
            }
        });
    }

    private void scheduleTimeout() {
        ui.postDelayed(() -> {
            if (finished) return;
            Toast.makeText(this, R.string.fv_timeout, Toast.LENGTH_SHORT).show();
            goToError();
        }, TIMEOUT_MS);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();
                bindUseCases(provider);
            } catch (Exception e) {
                Log.e(TAG, "Camera init failed", e);
                goToError();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindUseCases(@NonNull ProcessCameraProvider provider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ResolutionSelector resolution = new ResolutionSelector.Builder()
                .setResolutionStrategy(new ResolutionStrategy(
                        new Size(640, 480),
                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER))
                .build();

        ImageAnalysis analysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setResolutionSelector(resolution)
                .build();
        analysis.setAnalyzer(cameraExecutor, this::analyzeFrame);

        provider.unbindAll();
        provider.bindToLifecycle(this, CameraSelector.DEFAULT_FRONT_CAMERA, preview, analysis);
    }

    private void analyzeFrame(@NonNull ImageProxy proxy) {
        if (processingFrame || finished) {
            proxy.close();
            return;
        }
        processingFrame = true;

        Bitmap upright;
        try {
            upright = CameraImages.toUprightBitmap(proxy);
        } catch (Exception e) {
            Log.e(TAG, "Bitmap conversion failed", e);
            processingFrame = false;
            proxy.close();
            return;
        }
        proxy.close();

        faceDetector.analyze(upright, new FaceDetector.Listener() {
            @Override
            public void onFace(Face face, int totalFaces) {
                onDetectionResult(upright, face, totalFaces);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "ML Kit error", e);
                upright.recycle();
                processingFrame = false;
            }
        });
    }

    private void onDetectionResult(Bitmap frame, Face face, int totalFaces) {
        try {
            updateProgressFromElapsed();

            if (totalFaces > 1 || face == null) {
                consecutiveValidFrames = 0;
                return;
            }
            float y = face.getHeadEulerAngleY();
            float x = face.getHeadEulerAngleX();
            Rect box = face.getBoundingBox();
            float ratio = (float) box.width() / Math.max(1, frame.getWidth());

            boolean frontal = Math.abs(y) <= FRONTAL_Y_MAX && Math.abs(x) <= FRONTAL_X_MAX;
            boolean qualityOk = ratio >= MIN_FACE_RATIO;

            if (!frontal || !qualityOk) {
                consecutiveValidFrames = 0;
                return;
            }

            consecutiveValidFrames++;
            if (consecutiveValidFrames < STABLE_FRAMES_REQUIRED) return;

            float[] live;
            try {
                live = faceEmbedder.embed(frame, box);
            } catch (Exception e) {
                Log.e(TAG, "Embedding failed", e);
                consecutiveValidFrames = 0;
                return;
            }

            float similarity = FaceMatcher.cosineSimilarity(storedTemplate, live);
            Log.i(TAG, "Cosine similarity = " + similarity + " (threshold " + MATCH_THRESHOLD + ")");
            finished = true;
            ui.post(() -> {
                progress.setProgress(100);
                tvProgressValue.setText(getString(R.string.fv_progress_format, 100));
                if (similarity >= MATCH_THRESHOLD) {
                    vibrateShort();
                    goToSuccess();
                } else {
                    goToError();
                }
            });
        } finally {
            if (!frame.isRecycled()) frame.recycle();
            processingFrame = false;
        }
    }

    private void updateProgressFromElapsed() {
        long elapsed = System.currentTimeMillis() - startTime;
        int pct = (int) Math.min(95, (elapsed * 95) / TIMEOUT_MS);
        ui.post(() -> {
            progress.setProgress(pct);
            tvProgressValue.setText(getString(R.string.fv_progress_format, pct));
        });
    }

    private void goToSuccess() {
        if (isFinishing() || isDestroyed()) return;
        startActivity(new Intent(this, AttendanceSuccessActivity.class));
        finish();
    }

    private void goToError() {
        finished = true;
        if (isFinishing() || isDestroyed()) return;
        startActivity(new Intent(this, FaceVerificationErrorActivity.class));
        finish();
    }

    private void finishCancelled() {
        finished = true;
        finish();
    }

    @SuppressWarnings("deprecation")
    private void vibrateShort() {
        Vibrator vibrator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager mgr = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = mgr != null ? mgr.getDefaultVibrator() : null;
        } else {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }
        if (vibrator == null || !vibrator.hasVibrator()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(80L, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(80L);
        }
    }

    @Override
    protected void onDestroy() {
        ui.removeCallbacksAndMessages(null);
        if (faceDetector != null) faceDetector.close();
        if (faceEmbedder != null) faceEmbedder.close();
        if (cameraExecutor != null) cameraExecutor.shutdown();
        super.onDestroy();
    }
}
