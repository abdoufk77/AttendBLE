package com.example.attendble.ui.student.scan.face;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.attendble.R;
import com.example.attendble.ui.student.scan.AttendanceSuccessActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

/**
 * 3e étape anti-fraude : vérification visuelle on-device (ML Kit + MobileFaceNet TFLite).
 * Inséré entre la saisie du code 4 chiffres et l'écran de succès. Pour l'instant le flow
 * est simulé via Handler — à brancher sur FaceDetector + FaceEmbedder + FaceMatcher.
 */
public class FaceVerificationActivity extends AppCompatActivity {

    private static final long SIMULATED_DURATION_MS = 2500L;

    private LinearProgressIndicator progress;
    private TextView progressValue;

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
        topbar.setNavigationOnClickListener(v -> finish());

        progress = findViewById(R.id.progress_analyzing);
        progressValue = findViewById(R.id.tv_progress_value);

        MaterialButton cancel = findViewById(R.id.btn_cancel);
        cancel.setOnClickListener(v -> finish());

        // DEMO: simule la progression et redirige vers AttendanceSuccess.
        // À remplacer par VerifierVisageUseCase.execute(etudiantId, frameEmbedding, callback).
        simulateAnalysis();
    }

    private void simulateAnalysis() {
        Handler handler = new Handler(Looper.getMainLooper());
        long start = System.currentTimeMillis();
        Runnable tick = new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - start;
                int pct = (int) Math.min(100, (elapsed * 100) / SIMULATED_DURATION_MS);
                progress.setProgress(pct);
                progressValue.setText(getString(R.string.fv_progress_format, pct));
                if (pct < 100) {
                    handler.postDelayed(this, 80L);
                } else {
                    startActivity(new Intent(FaceVerificationActivity.this, AttendanceSuccessActivity.class));
                    finish();
                }
            }
        };
        handler.post(tick);
    }
}
