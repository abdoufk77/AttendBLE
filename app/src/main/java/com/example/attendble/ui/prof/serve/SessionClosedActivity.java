package com.example.attendble.ui.prof.serve;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.attendble.R;
import com.example.attendble.data.ServiceLocator;
import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.Etudiant;
import com.example.attendble.domain.model.EtudiantAttendance;
import com.example.attendble.ui.prof.classes.MyClassesActivity;
import com.google.android.material.button.MaterialButton;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Récap d'une session fermée : croise la liste des étudiants inscrits avec ceux
 * détectés en BLE pendant la session pour afficher stats + liste PRESENT/ABSENT.
 */
public class SessionClosedActivity extends AppCompatActivity {

    public static final String EXTRA_CLASSE_ID = "classeId";
    public static final String EXTRA_DETECTED_NUM_ETUDS = "detectedNumEtuds";

    private TextView tvPresent;
    private TextView tvAbsent;
    private TextView tvRate;
    private LinearLayout studentsContainer;

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

        tvPresent = findViewById(R.id.tv_stat_present_value);
        tvAbsent = findViewById(R.id.tv_stat_absent_value);
        tvRate = findViewById(R.id.tv_stat_rate_value);
        studentsContainer = findViewById(R.id.students_container);

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

        loadSummary();
    }

    private void loadSummary() {
        String classeId = getIntent().getStringExtra(EXTRA_CLASSE_ID);
        int[] detectedArr = getIntent().getIntArrayExtra(EXTRA_DETECTED_NUM_ETUDS);
        Set<Integer> detected = new HashSet<>();
        if (detectedArr != null) for (int n : detectedArr) detected.add(n);

        if (classeId == null) {
            renderEmpty(detected);
            return;
        }

        ServiceLocator.provideListEtudiantsByClasseUseCase().execute(classeId,
                new Callback<List<EtudiantAttendance>>() {
                    @Override
                    public void onSuccess(List<EtudiantAttendance> rows) {
                        render(rows, detected);
                    }

                    @Override
                    public void onError(Exception e) {
                        renderEmpty(detected);
                    }
                });
    }

    private void renderEmpty(Set<Integer> detected) {
        tvPresent.setText(String.valueOf(detected.size()));
        tvAbsent.setText("—");
        tvRate.setText("—");
    }

    private void render(List<EtudiantAttendance> rows, Set<Integer> detected) {
        int present = 0;
        int total = rows.size();
        studentsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (EtudiantAttendance row : rows) {
            Etudiant e = row.getEtudiant();
            boolean isPresent = isPresent(detected, e.getNumEtud());
            if (isPresent) present++;
            addStudentRow(inflater, e.getNom(), isPresent);
        }
        int absent = total - present;
        int rate = total == 0 ? 0 : Math.round(100f * present / total);
        tvPresent.setText(String.valueOf(present));
        tvAbsent.setText(String.valueOf(absent));
        tvRate.setText(rate + "%");
    }

    private boolean isPresent(Set<Integer> detected, String numEtud) {
        if (numEtud == null) return false;
        try {
            return detected.contains(Integer.parseInt(numEtud));
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private void addStudentRow(LayoutInflater inflater, String nom, boolean present) {
        View row = inflater.inflate(R.layout.item_session_summary_student, studentsContainer, false);
        ((TextView) row.findViewById(R.id.tv_student_name)).setText(nom);
        TextView status = row.findViewById(R.id.tv_student_status);
        if (present) {
            status.setText(R.string.sc_chip_present);
            status.setBackgroundResource(R.drawable.bg_chip_present);
            status.setTextColor(ContextCompat.getColor(this, R.color.brand_primary));
        } else {
            status.setText(R.string.sc_chip_absent);
            status.setBackgroundResource(R.drawable.bg_chip_absent);
            status.setTextColor(ContextCompat.getColor(this, R.color.brand_error));
        }
        studentsContainer.addView(row);
    }
}
