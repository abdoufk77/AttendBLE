package com.example.attendble.ui.student.scan;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.attendble.R;
import com.example.attendble.ui.student.classes.MyClassesActivity;
import com.example.attendble.ui.student.home.HomeActivity;
import com.example.attendble.ui.student.profil.ProfilActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

/**
 * Écran de scan BLE étudiant (état idle/searching) : 3 anneaux pulsés + statut Bluetooth +
 * bouton Cancel. La détection réelle sera branchée via BleScanner + ListenSessionsUseCase.
 */
public class SearchingActivity extends AppCompatActivity {

    private static final long PULSE_DURATION_MS = 1800L;

    // DEMO: simule la détection d'un beacon après ~3s pour la démo PFA — à supprimer
    // une fois le vrai BleScanner branché.
    private static final long DEMO_DETECT_DELAY_MS = 3000L;
    private final Handler demoHandler = new Handler(Looper.getMainLooper());
    private final Runnable demoNavigate = () -> {
        startActivity(new Intent(this, SessionCodeActivity.class));
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_searching);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        MaterialButton btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(v -> {
            demoHandler.removeCallbacks(demoNavigate);
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        demoHandler.postDelayed(demoNavigate, DEMO_DETECT_DELAY_MS);

        startPulse(findViewById(R.id.ring_inner), 0L);
        startPulse(findViewById(R.id.ring_middle), 600L);
        startPulse(findViewById(R.id.ring_outer), 1200L);

        bindBottomNav();
    }

    private void startPulse(View ring, long startDelay) {
        ring.setScaleX(0.6f);
        ring.setScaleY(0.6f);
        ring.setAlpha(0f);
        ring.animate().cancel();
        ring.postDelayed(() -> loopPulse(ring), startDelay);
    }

    private void loopPulse(View ring) {
        if (isFinishing() || isDestroyed()) return;
        ring.setScaleX(0.6f);
        ring.setScaleY(0.6f);
        ring.setAlpha(0.4f);
        ring.animate()
                .scaleX(1.6f)
                .scaleY(1.6f)
                .alpha(0f)
                .setDuration(PULSE_DURATION_MS)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> loopPulse(ring))
                .start();
    }

    @Override
    protected void onDestroy() {
        demoHandler.removeCallbacks(demoNavigate);
        super.onDestroy();
    }

    private void bindBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(R.id.nav_scan);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_scan) {
                return true;
            }
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_classes) {
                startActivity(new Intent(this, MyClassesActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfilActivity.class));
                finish();
                return true;
            }
            Toast.makeText(this, R.string.student_toast_nav, Toast.LENGTH_SHORT).show();
            return true;
        });
    }
}
