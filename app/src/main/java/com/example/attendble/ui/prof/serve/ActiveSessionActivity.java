package com.example.attendble.ui.prof.serve;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.attendble.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;

/**
 * Écran de session active : code 4-chiffres, timer 2 min, liste live des pointages.
 * Stubé — sera branché sur SessionRepository + BleAdvertiserManager + countdown timer.
 */
public class ActiveSessionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_active_session);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        ((CircularProgressIndicator) findViewById(R.id.progress_timer)).setProgressCompat(53, false);
        ((LinearProgressIndicator) findViewById(R.id.progress_live)).setProgressCompat(28, false);

        ((MaterialButton) findViewById(R.id.btn_end_session))
                .setOnClickListener(v -> {
                    Toast.makeText(this, R.string.as_toast_end, Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}
