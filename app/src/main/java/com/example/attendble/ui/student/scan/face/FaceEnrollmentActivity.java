package com.example.attendble.ui.student.scan.face;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.attendble.domain.repository.AuthRepository;
import com.example.attendble.face.CameraImages;
import com.example.attendble.face.FaceDetector;
import com.example.attendble.face.FaceEmbedder;
import com.example.attendble.face.FaceMatcher;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.face.Face;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Étape 2/2 de l'inscription étudiant : enrôlement du visage en 5 captures guidées.
 * Pipeline : CameraX (caméra frontale) → ML Kit FaceDetector → vérification de la pose attendue
 *   → MobileFaceNet embedder → moyenne L2-normalisée → persistance via AuthRepository.
 */
public class FaceEnrollmentActivity extends AppCompatActivity {

    private static final String TAG = "FaceEnrollment";
    public static final String EXTRA_ETUDIANT_UID = "etudiant_uid";

    private static final int TOTAL_SAMPLES = 5;

    /** Bornes Euler (deg) par étape : Y = lacet (left/right), X = tangage (up/down). */
    private static final float[][] POSE_BOUNDS = {
            // {yMin, yMax, xMin, xMax}
            { -8f,   8f,  -8f,   8f },   // 1. front
            { -25f, -12f, -10f, 10f },   // 2. tourner à gauche
            {  12f,  25f, -10f, 10f },   // 3. tourner à droite
            { -10f,  10f,  10f, 25f },   // 4. lever la tête
            { -10f,  10f, -25f, -10f }   // 5. baisser la tête
    };
    private static final int[] INSTRUCTION_RES = {
            R.string.fe_instruction_look,
            R.string.fe_instruction_left,
            R.string.fe_instruction_right,
            R.string.fe_instruction_up,
            R.string.fe_instruction_down
    };
    /** Pourcentage minimum de la frame que doit occuper la bounding box pour être valide. */
    private static final float MIN_FACE_RATIO = 0.30f;
    /** Nombre de frames consécutives valides avant de capturer. */
    private static final int STABLE_FRAMES_REQUIRED = 2;

    private PreviewView previewView;
    private LinearLayout stepsContainer;
    private TextView tvInstruction;
    private TextView tvHint;
    private TextView tvProgressCount;
    private MaterialButton btnFinish;
    private View chipScanning;

    private FaceDetector faceDetector;
    private FaceEmbedder faceEmbedder;
    private ExecutorService cameraExecutor;
    private final List<float[]> capturedEmbeddings = new ArrayList<>();

    private int currentStep = 0;
    private int consecutiveValidFrames = 0;
    private volatile boolean processingFrame = false;
    private volatile boolean enrollmentDone = false;

    private String etudiantUid;
    private AuthRepository authRepository;

    private final androidx.activity.result.ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startCamera();
                else {
                    Toast.makeText(this, R.string.fe_permission_required, Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_face_enrollment);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        etudiantUid = getIntent().getStringExtra(EXTRA_ETUDIANT_UID);
        authRepository = ServiceLocator.getAuthRepository();

        MaterialToolbar topbar = findViewById(R.id.topbar);
        topbar.setNavigationOnClickListener(v -> {
            // Étape obligatoire — la flèche reste, mais n'autorise la sortie qu'à la fin.
            if (enrollmentDone) finish();
        });

        previewView = findViewById(R.id.camera_preview);
        stepsContainer = findViewById(R.id.steps_container);
        tvInstruction = findViewById(R.id.tv_instruction);
        tvHint = (TextView) ((ViewGroup) tvInstruction.getParent()).getChildAt(
                ((ViewGroup) tvInstruction.getParent()).indexOfChild(tvInstruction) + 1);
        tvProgressCount = findViewById(R.id.tv_progress_count);
        btnFinish = findViewById(R.id.btn_finish);
        chipScanning = findViewById(R.id.chip_scanning);

        btnFinish.setOnClickListener(v -> persistAndContinue());

        cameraExecutor = Executors.newSingleThreadExecutor();

        try {
            faceEmbedder = new FaceEmbedder(this);
        } catch (Exception e) {
            Log.e(TAG, "Failed to load MobileFaceNet model", e);
            goToError();
            return;
        }
        faceDetector = new FaceDetector();

        buildStepDots();
        refreshUi();

        if (hasCameraPermission()) startCamera();
        else permissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
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
        if (processingFrame || enrollmentDone) {
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
        } finally {
            // ImageProxy lu, on peut le libérer après extraction du bitmap.
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
            if (totalFaces > 1) {
                runOnUiThread(() -> tvHint.setText(R.string.fe_multiple_faces));
                consecutiveValidFrames = 0;
                return;
            }
            if (face == null) {
                runOnUiThread(() -> tvHint.setText(R.string.fe_no_face));
                consecutiveValidFrames = 0;
                return;
            }

            float[] bounds = POSE_BOUNDS[currentStep];
            float y = face.getHeadEulerAngleY();
            float x = face.getHeadEulerAngleX();
            boolean poseOk = y >= bounds[0] && y <= bounds[1] && x >= bounds[2] && x <= bounds[3];

            Rect box = face.getBoundingBox();
            float ratio = (float) box.width() / Math.max(1, frame.getWidth());
            boolean qualityOk = ratio >= MIN_FACE_RATIO;

            if (!qualityOk) {
                runOnUiThread(() -> tvHint.setText(R.string.fe_instruction_hint));
                consecutiveValidFrames = 0;
                return;
            }
            if (!poseOk) {
                runOnUiThread(() -> tvHint.setText(INSTRUCTION_RES[currentStep]));
                consecutiveValidFrames = 0;
                return;
            }

            consecutiveValidFrames++;
            if (consecutiveValidFrames < STABLE_FRAMES_REQUIRED) return;

            // Pose stable : on capture.
            float[] embedding;
            try {
                embedding = faceEmbedder.embed(frame, box);
            } catch (Exception e) {
                Log.e(TAG, "Embedding failed", e);
                consecutiveValidFrames = 0;
                return;
            }
            capturedEmbeddings.add(embedding);
            consecutiveValidFrames = 0;
            int captured = capturedEmbeddings.size();
            currentStep = captured;
            runOnUiThread(this::onCaptureSucceeded);
        } finally {
            if (!frame.isRecycled()) frame.recycle();
            processingFrame = false;
        }
    }

    private void onCaptureSucceeded() {
        vibrateShort();
        refreshUi();
        if (capturedEmbeddings.size() >= TOTAL_SAMPLES) {
            enrollmentDone = true;
            chipScanning.setVisibility(View.GONE);
            tvHint.setText("");
        }
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
            vibrator.vibrate(VibrationEffect.createOneShot(60L, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(60L);
        }
    }

    private void buildStepDots() {
        int activeSize = dp(12);
        int doneSize = dp(24);
        int pendingSize = dp(8);
        int spacing = dp(8);

        stepsContainer.removeAllViews();
        for (int i = 0; i < TOTAL_SAMPLES; i++) {
            View dot;
            int captured = capturedEmbeddings.size();
            if (i < captured) {
                ImageView iv = new ImageView(this);
                iv.setImageResource(R.drawable.ic_check);
                iv.setBackgroundResource(R.drawable.bg_step_done);
                iv.setPadding(dp(4), dp(4), dp(4), dp(4));
                dot = iv;
                setSize(dot, doneSize, doneSize);
            } else if (i == captured) {
                dot = new View(this);
                dot.setBackgroundResource(R.drawable.bg_step_dot_active);
                setSize(dot, activeSize, activeSize);
            } else {
                dot = new View(this);
                dot.setBackgroundResource(R.drawable.bg_step_dot_inactive);
                setSize(dot, pendingSize, pendingSize);
            }
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) dot.getLayoutParams();
            if (i > 0) lp.leftMargin = spacing;
            stepsContainer.addView(dot);
        }
    }

    private void setSize(View v, int w, int h) {
        v.setLayoutParams(new LinearLayout.LayoutParams(w, h));
    }

    private void refreshUi() {
        int captured = capturedEmbeddings.size();
        int instructionIndex = Math.min(captured, INSTRUCTION_RES.length - 1);
        tvInstruction.setText(INSTRUCTION_RES[instructionIndex]);
        tvHint.setText(R.string.fe_instruction_hint);
        tvProgressCount.setText(getString(R.string.fe_progress_format, captured, TOTAL_SAMPLES));
        btnFinish.setEnabled(captured >= TOTAL_SAMPLES);
        buildStepDots();
    }

    private void persistAndContinue() {
        if (capturedEmbeddings.size() < TOTAL_SAMPLES) return;
        btnFinish.setEnabled(false);
        tvHint.setText(R.string.fe_persisting);
        float[] template = FaceMatcher.averageL2(capturedEmbeddings);

        if (etudiantUid == null) {
            // En dev (lancement direct), pas d'uid → on ne persiste pas, on continue quand même.
            Log.w(TAG, "No etudiantUid in intent, skipping persistence");
            goToSuccess();
            return;
        }
        Log.i(TAG, "Persisting face template for uid=" + etudiantUid + " length=" + template.length);
        authRepository.updateFaceEmbedding(etudiantUid, template, new Callback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.i(TAG, "Face template persisted for uid=" + etudiantUid);
                goToSuccess();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Persist failed for uid=" + etudiantUid, e);
                btnFinish.setEnabled(true);
                tvHint.setText(R.string.fe_persist_failed);
                Toast.makeText(FaceEnrollmentActivity.this,
                        getString(R.string.fe_persist_failed) + " : " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void goToSuccess() {
        startActivity(new Intent(this, FaceEnrollmentSuccessActivity.class));
        finish();
    }

    private void goToError() {
        startActivity(new Intent(this, FaceEnrollmentErrorActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        if (enrollmentDone) super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (faceDetector != null) faceDetector.close();
        if (faceEmbedder != null) faceEmbedder.close();
        if (cameraExecutor != null) cameraExecutor.shutdown();
        super.onDestroy();
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}
