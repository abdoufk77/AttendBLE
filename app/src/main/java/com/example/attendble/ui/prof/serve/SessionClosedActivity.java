package com.example.attendble.ui.prof.serve;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.attendble.R;
import com.example.attendble.ui.prof.classes.MyClassesActivity;
import com.google.android.material.button.MaterialButton;

/**
 * Récap d'une session fermée : stats (présent / absent / taux), liste des étudiants et leur statut.
 * Stubé — à brancher via SessionRepository.getReport(sessionId).
 */
public class SessionClosedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_session_closed);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        findViewById(R.id.btn_close).setOnClickListener(v -> finish());

        ((MaterialButton) findViewById(R.id.btn_export))
                .setOnClickListener(v -> Toast.makeText(this, R.string.sc_toast_export, Toast.LENGTH_SHORT).show());

        ((MaterialButton) findViewById(R.id.btn_back_classes))
                .setOnClickListener(v -> {
                    Intent intent = new Intent(this, MyClassesActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                });
    }
}
