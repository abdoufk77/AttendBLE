package com.example.attendble.ui.student.scan.face;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.attendble.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

/**
 * Étape 2/2 de l'inscription étudiant : enrôlement du visage (liveness + capture multi-angles).
 * Capture 5 échantillons guidés (look / left / right / up / down) qui formeront le template
 * stocké dans Etudiant.faceEmbedding et réutilisé par FaceVerificationActivity.
 */
public class FaceEnrollmentActivity extends AppCompatActivity {

    public static final String EXTRA_ETUDIANT_UID = "etudiant_uid";

    private static final int TOTAL_SAMPLES = 5;
    private static final long CAPTURE_INTERVAL_MS = 1200L;

    private static final int[] INSTRUCTION_RES = {
            R.string.fe_instruction_look,
            R.string.fe_instruction_left,
            R.string.fe_instruction_right,
            R.string.fe_instruction_up,
            R.string.fe_instruction_down
    };

    private LinearLayout stepsContainer;
    private TextView tvInstruction;
    private TextView tvProgressCount;
    private MaterialButton btnFinish;

    private int capturedSamples = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onBackPressed() {
        // Enrôlement obligatoire : on bloque la sortie tant que les 5 captures ne sont pas faites.
        if (capturedSamples >= TOTAL_SAMPLES) {
            super.onBackPressed();
        }
    }

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

        // Étape obligatoire : la flèche retour est visible mais inactive tant que l'enrôlement
        // n'est pas terminé (idem onBackPressed).
        MaterialToolbar topbar = findViewById(R.id.topbar);
        topbar.setNavigationOnClickListener(v -> {
            if (capturedSamples >= TOTAL_SAMPLES) finish();
        });

        stepsContainer = findViewById(R.id.steps_container);
        tvInstruction = findViewById(R.id.tv_instruction);
        tvProgressCount = findViewById(R.id.tv_progress_count);
        btnFinish = findViewById(R.id.btn_finish);

        buildStepDots();
        refreshUi();

        btnFinish.setOnClickListener(v -> {
            // TODO: persister faceEmbedding via SignupUseCase.completeFaceEnrollment(uid, embedding)
            startActivity(new Intent(this, FaceEnrollmentSuccessActivity.class));
            finish();
        });

        // DEMO: simule la capture progressive — à remplacer par FaceDetector (ML Kit) +
        // FaceEmbedder (MobileFaceNet TFLite) déclenchés à chaque pose validée.
        scheduleNextCapture();
    }

    private void buildStepDots() {
        int activeSize = dp(12);
        int doneSize = dp(24);
        int pendingSize = dp(8);
        int spacing = dp(8);

        stepsContainer.removeAllViews();
        for (int i = 0; i < TOTAL_SAMPLES; i++) {
            View dot;
            if (i < capturedSamples) {
                ImageView iv = new ImageView(this);
                iv.setImageResource(R.drawable.ic_check);
                iv.setBackgroundResource(R.drawable.bg_step_done);
                iv.setPadding(dp(4), dp(4), dp(4), dp(4));
                dot = iv;
                setSize(dot, doneSize, doneSize);
            } else if (i == capturedSamples) {
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
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(w, h);
        v.setLayoutParams(lp);
    }

    private void refreshUi() {
        int instructionIndex = Math.min(capturedSamples, INSTRUCTION_RES.length - 1);
        tvInstruction.setText(INSTRUCTION_RES[instructionIndex]);
        tvProgressCount.setText(getString(R.string.fe_progress_format, capturedSamples, TOTAL_SAMPLES));
        btnFinish.setEnabled(capturedSamples >= TOTAL_SAMPLES);
        buildStepDots();
    }

    private void scheduleNextCapture() {
        if (capturedSamples >= TOTAL_SAMPLES) return;
        handler.postDelayed(() -> {
            capturedSamples++;
            refreshUi();
            scheduleNextCapture();
        }, CAPTURE_INTERVAL_MS);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}
